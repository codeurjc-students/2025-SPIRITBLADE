# Guía Completa de Despliegue en Cloud (Oracle OCI) | SPIRITBLADE

Esta documentación detalla la arquitectura, provisionamiento y despliegue de la infraestructura de SPIRITBLADE en Oracle Cloud Infrastructure (OCI). El proyecto aprovecha la capa gratuita (**Always Free Tier**) de Oracle, utilizando procesadores ARM Ampere para maximizar rendimiento sin costes.

---

## 1. Arquitectura Cloud e Infraestructura

La infraestructura se define como código (IaC) utilizando **Terraform**.

### 1.1 Diagrama de Arquitectura

```mermaid
graph TD
    subgraph OCI_Region[Oracle Cloud Region (eu-madrid-1)]
        IGW[Internet Gateway]
        NAT[NAT Gateway]
        S3[Object Storage (S3 API)]
        DNS[DNS: spiritblade.dev]
        ADB[Oracle Autonomous Database<br/>Always Free · 20 GB · 19c]
        
        subgraph VCN[VCN 10.0.0.0/16]
            subgraph Public_Subnet[Public Subnet]
                Ingress_LB[Ingress Load Balancer<br/>143.47.39.213]
            end
            
            subgraph Private_Subnet_K8s[Private Subnet (K8s Nodes)]
                K8s_Node1[Worker Node 1 (ARM)]
                K8s_Node2[Worker Node 2 (ARM)]
                K8s_Node34[Worker Node 3-4 (ARM, autoscaler)]
                
                Ingress[NGINX Ingress Controller]
                CertMgr[cert-manager + Let's Encrypt]
                Frontend[Frontend Pods<br/>HPA: 1-3 replicas]
                Backend[Backend Pods<br/>HPA: 1-3 replicas]
                Redis[Redis Cluster Helm<br/>HPA: 2-4 replicas]
                Metrics[Metrics Server]
                ClusterAS[Cluster Autoscaler]
            end
        end
    end

    User((Usuario)) --> |HTTPS<br/>spiritblade.dev| DNS
    DNS --> Ingress_LB
    Ingress_LB --> Ingress
    Ingress --> |TLS Termination| Frontend
    CertMgr --> |Gestiona certificados SSL| Ingress
    Frontend --> |HTTPS interno| Backend
    Backend --> |TCPS/1521| ADB
    Backend --> |redis-cluster-master| Redis
    Backend --> |S3 API| S3
    Metrics --> |Métricas CPU/RAM| Backend
    Metrics --> |Métricas CPU/RAM| Frontend
    ClusterAS --> |Escala nodos 2-4| K8s_Node34
    
    K8s_Node1 --> NAT
    K8s_Node2 --> NAT
    K8s_Node34 --> NAT
```

### 1.2 Recursos de Computación (Oracle Ampere A1)

Oracle ofrece gratuitamente hasta 4 OCPUs y 24 GB de RAM en instancias ARM. SPIRITBLADE distribuye estos recursos de la siguiente manera:

| Componente | Tipo de Recurso | Instancia (Shape) | OCPUs | RAM | Disco | SO |
|------------|-----------------|-------------------|-------|-----|-------|----|
| **Cluster K8s (Nodo 1)** | OKE Node | `VM.Standard.A1.Flex` | 1 | 6 GB | 50 GB | Oracle Linux 8 (ARM64) |
| **Cluster K8s (Nodo 2)** | OKE Node | `VM.Standard.A1.Flex` | 1 | 6 GB | 50 GB | Oracle Linux 8 (ARM64) |
| **Cluster K8s (Nodo 3-4)** | OKE Node (autoscaler) | `VM.Standard.A1.Flex` | 1 c/u | 6 GB c/u | 50 GB c/u | Oracle Linux 8 (ARM64) |
| **Base de Datos** | Autonomous Database | `Always Free (ADB)` | — | — | 20 GB | Oracle DB 19c (gestionado) |
| **Total máximo (4 nodos)** | - | - | **4** | **24 GB** | **200 GB + 20 GB ADB** | - |

> **Nota:** ADB Always Free usa su propia cuota independiente — **no consume OCPUs ARM**. Los 4 OCPUs ARM quedan 100% disponibles para nodos OKE.

### 1.3 Componentes de Kubernetes

**Ingress Controller (NGINX)**:
- Gestiona el tráfico HTTPS entrante desde el dominio `spiritblade.dev`
- LoadBalancer público en OCI: `143.47.39.213`
- Terminación TLS con certificados de Let's Encrypt

**cert-manager**:
- Automatiza la emisión y renovación de certificados SSL/TLS
- Integrado con Let's Encrypt (ACME protocol)
- Gestiona certificados para `spiritblade.dev` y `www.spiritblade.dev`

**Horizontal Pod Autoscaler (HPA)**:
- Backend: 1-3 réplicas (escala al 70% CPU / 80% RAM)
- Frontend: 1-3 réplicas (escala al 70% CPU)
- Redis Cache Replicas: 2-4 réplicas (escala al 70% CPU)
- Requiere Metrics Server para funcionar

**Metrics Server**:
- Recopila métricas de uso de CPU y memoria de los pods
- Esencial para que las reglas HPA de todo el cluster puedan tomar decisiones de escalado

### 1.4 Red (Networking)

La red se configura en una **VCN** (`10.0.0.0/16`) con segmentación estricta para seguridad:

1.  **Public Subnet**: Aloja únicamente los Load Balancers públicos. Permite tráfico entrante desde internet.
2.  **Private Subnet (Nodes)**: Aloja los nodos de Kubernetes. Sin acceso directo desde internet (solo a través de NAT Gateway).
3.  **K8s API Subnet**: Endpoint para la gestión del clúster (permitiendo acceso público o privado según configuración).

> **Nota:** La base de datos ya no reside en la VCN. Oracle Autonomous Database (ADB) es un servicio gestionado con endpoint público (`adb.eu-madrid-1.oraclecloud.com`). La conexión se realiza por TCPS (TLS) desde los pods del backend directamente a internet a través del NAT Gateway.

---

## 2. Almacenamiento y Base de Datos

### 2.1 Base de Datos (Oracle Autonomous Database — Always Free)

SPIRITBLADE usa **Oracle Autonomous Database (ADB) Always Free**, un servicio completamente gestionado que no consume OCPUs ARM del tenant, permitiendo dedicar los 4 OCPUs al cluster Kubernetes.

- **OCID**: `ocid1.autonomousdatabase.oc1.eu-madrid-1.anwwcljrxqno2ria6nikabtbs66bwn2kngbner6wnyqzo3esin7btnyegymq`
- **Versión DB**: Oracle 19c · workload OLTP
- **Almacenamiento**: 20 GB (cuota independiente, no compite con Block Volumes K8s)
- **Endpoint**: Público con ACL (`0.0.0.0/0`), sin private endpoint
- **Seguridad**: Conexión TCPS (TLS obligatorio) · sin mTLS (no requiere wallet)
- **JDBC URL**: `jdbc:oracle:thin:@(description=(retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.eu-madrid-1.oraclecloud.com))(connect_data=(service_name=ge32c8a2145bafd_spiritblade_tp.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))`
- **Credenciales**: Usuario `ADMIN`, contraseña vía variable Terraform `adb_admin_password` y secret K8s `adb-password`.
- **Provisionamiento**: Completamente gestionado por OCI. Terraform define el recurso en `iac/terraform/adb.tf` con `is_free_tier = true` y `lifecycle { prevent_destroy = true }`.

### 2.2 Object Storage (S3 Compatible)

Se utiliza un Bucket de OCI Object Storage para almacenar imágenes de perfil y otros assets.

- **Configuración**: Acceso API S3 habilitado.
- **Visibilidad**: Bucket privado (`NoPublicAccess`).
- **Credenciales**: Terraform genera automáticamente las Customer Secret Keys para que el Backend pueda autenticarse usando protocolo S3.

---

## 3. Guía de Despliegue (Terraform)

El despliegue de infraestructura está totalmente automatizado.

### Requisitos Previos
1.  Cuenta en Oracle Cloud.
2.  OCI CLI instalado y configurado localmente.
3.  Terraform instalado (`>= 1.0`).
4.  Claves SSH (pública/privada) generadas para acceso a instancias.

### Paso 1: Configurar Variables

Navegar a `iac/terraform/` y crear un archivo `terraform.tfvars`:

```hcl
# Credenciales de OCI (Obtener de OCI Console > User Settings)
tenancy_ocid     = "ocid1.tenancy.oc1..aaaa..."
user_ocid        = "ocid1.user.oc1..aaaa..."
fingerprint      = "12:34:56:78:..."
private_key_path = "C:/Users/tu_usuario/.oci/oci_api_key.pem"
region           = "eu-madrid-1" # O tu región preferida

# Configuración del Proyecto
compartment_ocid    = "ocid1.compartment.oc1..aaaa..." # O usar el tenancy_ocid si no tienes compartimentos
public_key_path     = "C:/Users/tu_usuario/.ssh/id_rsa.pub" # Tu clave pública SSH
adb_admin_password  = "TuPasswordSegura123!" # Contraseña ADMIN para Oracle ADB (min 12 chars, mayús+minús+número+especial)
project_name        = "spiritblade"
```

### Paso 2: Inicializar y Aplicar

```bash
cd iac/terraform
terraform init
terraform apply
```

Revisar el plan y confirmar con `yes`. Este proceso tardará entre 10 y 20 minutos mientras provisiona la VCN, el Cluster OKE y la instancia MySQL.

### Paso 3: Obtener Outputs Críticos

Al finalizar, Terraform mostrará información vital. Guárdala o recupérala con `terraform output`:

- **Kubeconfig Command**: Comando para configurar `kubectl` localmente.
- **ADB JDBC URL**: URL de conexión TCPS a Oracle ADB (necesaria para `k8s/prod/backend-deployment.yaml`).
- **S3 Credentials**: Access Key, Secret Key y Endpoint para Object Storage (necesario para `k8s/prod/secrets.yaml`).

```bash
# Obtener la URL JDBC de ADB
terraform output adb_jdbc_url

# Obtener el OCID de ADB
terraform output adb_ocid
```

---

## 4. Configuración de Kubernetes y Aplicación

Una vez la infraestructura "física" está lista, se despliega la aplicación de software.

### Paso 1: Conectar al Cluster

Ejecuta el comando proporcionado por el output de Terraform (ejemplo):
```bash
oci ce cluster create-kubeconfig --cluster-id ocid1.cluster.oc1... --file $HOME/.kube/config --region eu-madrid-1 --token-version 2.0.0 --kube-endpoint PUBLIC_ENDPOINT
```

Verifica conexión: `kubectl get nodes` (deben aparecer 2 nodos Ready).

### Paso 2: Configurar Secretos

Edita `k8s/prod/secrets.yaml` utilizando los valores obtenidos de Terraform. **IMPORTANTE**: Todos los valores deben estar en **base64**.

**Codificar valores en PowerShell**:
```powershell
echo -n 'tu_valor' | ForEach-Object { [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($_)) }
```

**Ejemplo de secrets.yaml**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: spiritblade-secrets
  namespace: prod
type: Opaque
data:
  # Oracle ADB admin password (base64 encoded)
  adb-password: unacontraseñaenbase64==
  
  # OCI Object Storage credentials (base64 encoded)
  oci-storage-access-key: unaClaveDeAccesoEnBase64==
  oci-storage-secret-key: unaClaveSecretaEnBase64==
  
  # Riot Games API Key (base64 encoded)
  riot-api-key: unaRiotApiKeyEnBase64==
  
  # Google AI API Key (base64 encoded)
  google-ai-api-key: unaGoogleAiApiKeyEnBase64==
  
  # SSL Password para keystore.jks (base64 encoded)
  ssl-password: unaSslPasswordEnBase64==
```

### Paso 3: Instalar Componentes de Infraestructura

**a) Instalar Metrics Server (requerido para HPA y VPA)**:
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

**b) Instalar Vertical Pod Autoscaler (requerido para Redis)**:
En OKE no viene por defecto. Ejecuta en una terminal bash:
```bash
git clone https://github.com/kubernetes/autoscaler.git
cd autoscaler/vertical-pod-autoscaler
./hack/vpa-up.sh
```

**c) Instalar cert-manager (gestión automática de certificados SSL)**:
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.2/cert-manager.yaml
```

**d) Instalar NGINX Ingress Controller**:
```bash
# Instalar el controlador oficial
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/cloud/deploy.yaml

# IMPORTANTE (OCI Free Tier): Configurar el Load Balancer en modo Flexible (Gratis)
# Si no haces esto, OCI creará uno de 100Mbps de pago.
kubectl annotate service ingress-nginx-controller -n ingress-nginx \
  service.beta.kubernetes.io/oci-load-balancer-shape="flexible" \
  service.beta.kubernetes.io/oci-load-balancer-shape-flex-min="10" \
  service.beta.kubernetes.io/oci-load-balancer-shape-flex-max="10" \
  --overwrite
```

Espera a que el Ingress Controller obtenga una IP externa:
```bash
kubectl get svc -n ingress-nginx -w
```
Anota la **EXTERNAL-IP** (ejemplo: `143.47.39.213`).

### Paso 4: Configurar DNS

En tu proveedor de DNS (ej. name.com, Cloudflare, etc.):

1. Crea registro **A** para `@` (dominio raíz) → IP del Ingress Controller
2. Crea registro **A** para `www` → IP del Ingress Controller
3. Opcional: Crea registro **A** wildcard `*` → IP del Ingress Controller

Ejemplo para `spiritblade.dev`:
```
Tipo  Nombre  Valor           TTL
A     @       143.47.39.213   300
A     www     143.47.39.213   300
```

Espera 5-10 minutos para propagación DNS.


### Paso 5: Desplegar Manifiestos

```bash
cd k8s/prod
kubectl apply -f namespace.yaml
kubectl apply -f secrets.yaml
kubectl apply -f redis-deployment.yaml
kubectl apply -f redis-vpa.yaml           # Vertical Pod Autoscaler para Redis
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml
kubectl apply -f hpa.yaml                 # Horizontal Pod Autoscaler
kubectl apply -f cluster-autoscaler.yaml  # Autoscaler de nodos OCI
kubectl apply -f letsencrypt-issuer.yaml  # ClusterIssuer para Let's Encrypt
kubectl apply -f ingress.yaml             # Ingress con TLS automático
```

### Paso 7: Verificar Certificado SSL

Verifica que cert-manager emitió el certificado correctamente:
```bash
kubectl get certificate -n prod
kubectl describe certificate spiritblade-tls -n prod
```

Debe mostrar `Ready: True` después de 1-2 minutos. Si hay errores, revisa:
```bash
kubectl get certificaterequest -n prod
kubectl logs -n cert-manager -l app=cert-manager
```

> **Nota sobre ADB**: La base de datos corre en Oracle Autonomous Database Always Free con endpoint público. El backend se conecta directamente por TCPS (TLS, puerto 1521) usando la URL larga definida en la variable de entorno `SPRING_DATASOURCE_URL` del deployment. No se necesita ningún `ExternalName` service adicional.

---

## 5. Mantenimiento y Operaciones

### Acceso a la Aplicación

**URL Producción**: `https://spiritblade.dev`

**Verificar estado del cluster**:
```bash
# Ver todos los pods
kubectl get pods -n prod

# Ver servicios y Load Balancers
kubectl get svc -n prod
kubectl get svc -n ingress-nginx

# Ver estado del HPA
kubectl get hpa -n prod

# Ver métricas en tiempo real
kubectl top nodes
kubectl top pods -n prod
```

### Acceso SSH a Nodos y DB

Como las instancias están en subredes privadas, no puedes hacer SSH directo.
**Solución**: Usar una instancia "Bastion" o configurar temporalmente una IP pública en una instancia efímera, o usar el servicio **OCI Bastion**.

### Actualización de Imágenes

El pipeline de CI/CD (GitHub Actions) construye y publica imágenes multi-arquitectura (`linux/amd64` y `linux/arm64`). Para actualizar en producción:

```bash
kubectl rollout restart deployment backend -n prod
kubectl rollout restart deployment frontend -n prod

# Verificar el rollout
kubectl rollout status deployment backend -n prod
kubectl rollout status deployment frontend -n prod
```

### Renovación de Certificados SSL

Los certificados de Let's Encrypt se renuevan **automáticamente** cada 60 días (expiran a los 90). cert-manager gestiona todo el proceso.

Verificar estado:
```bash
kubectl get certificate -n prod
kubectl describe certificate spiritblade-tls -n prod
```

### Troubleshooting

**Backend no arranca (CrashLoopBackOff)**:
```bash
# Ver logs del pod
kubectl logs -n prod -l app=backend --tail=100

# Verificar secrets
kubectl get secret spiritblade-secrets -n prod -o yaml

# Verificar conectividad Oracle ADB (TCPS puerto 1521)
kubectl exec -n prod <backend-pod> -- curl -v --connect-timeout 5 https://adb.eu-madrid-1.oraclecloud.com
```

**HPA no escala**:
```bash
# Verificar Metrics Server
kubectl get deployment metrics-server -n kube-system
kubectl top pods -n prod  # Debe mostrar CPU/RAM

# Ver eventos del HPA
kubectl describe hpa backend-hpa -n prod
```

**Certificado SSL no se emite**:
```bash
# Ver el estado del certificado
kubectl describe certificate spiritblade-tls -n prod

# Ver logs de cert-manager
kubectl logs -n cert-manager -l app=cert-manager --tail=100

# Verificar que el DNS apunta al Ingress
nslookup spiritblade.dev
```

### Limpieza de Recursos (Destrucción)

Para eliminar toda la infraestructura y detener la facturación (aunque sea gratuito, limpia el tenant):

```bash
cd iac/terraform
terraform destroy
```
