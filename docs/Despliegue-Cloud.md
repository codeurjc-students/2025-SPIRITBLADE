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
        
        subgraph VCN[VCN 10.0.0.0/16]
            subgraph Public_Subnet[Public Subnet]
                Ingress_LB[Ingress Load Balancer<br/>143.47.39.213]
            end
            
            subgraph Private_Subnet_K8s[Private Subnet (K8s Nodes)]
                K8s_Node1[Worker Node 1 (ARM)]
                K8s_Node2[Worker Node 2 (ARM)]
                
                Ingress[NGINX Ingress Controller]
                CertMgr[cert-manager + Let's Encrypt]
                Frontend[Frontend Pods<br/>HPA: 1-3 replicas]
                Backend[Backend Pods<br/>HPA: 1-3 replicas]
                Redis[Redis StatefulSet]
                Metrics[Metrics Server]
            end
            
            subgraph Private_Subnet_DB[Private Subnet (Database)]
                MySQL_VM[MySQL Compute Instance (ARM)]
            end
        end
    end

    User((Usuario)) --> |HTTPS<br/>spiritblade.dev| DNS
    DNS --> Ingress_LB
    Ingress_LB --> Ingress
    Ingress --> |TLS Termination| Frontend
    CertMgr --> |Gestiona certificados SSL| Ingress
    Frontend --> |HTTPS interno| Backend
    Backend --> |mysql-external| MySQL_VM
    Backend --> |redis-master| Redis
    Backend --> |S3 API| S3
    Metrics --> |Métricas CPU/RAM| Backend
    Metrics --> |Métricas CPU/RAM| Frontend
    
    K8s_Node1 --> NAT
    K8s_Node2 --> NAT
    MySQL_VM --> NAT
```

### 1.2 Recursos de Computación (Oracle Ampere A1)

Oracle ofrece gratuitamente hasta 4 OCPUs y 24 GB de RAM en instancias ARM. SPIRITBLADE distribuye estos recursos de la siguiente manera:

| Componente | Tipo de Recurso | Instancia (Shape) | OCPUs | RAM | Disco | SO |
|------------|-----------------|-------------------|-------|-----|-------|----|
| **Cluster K8s (Nodo 1)** | OKE Node | `VM.Standard.A1.Flex` | 1 | 6 GB | 50 GB | Oracle Linux 8 (ARM64) |
| **Cluster K8s (Nodo 2)** | OKE Node | `VM.Standard.A1.Flex` | 1 | 6 GB | 50 GB | Oracle Linux 8 (ARM64) |
| **Base de Datos** | Compute Instance | `VM.Standard.A1.Flex` | 1 | 6 GB | 100 GB | Oracle Linux 8 (ARM64) |
| **Total Utilizado** | - | - | **3** | **18 GB** | **200 GB** | - |
| **Disponible / Libre** | - | - | 1 | 6 GB | - | - |

> **Nota:** 1 OCPU ARM equivale a 2 vCPUs lógicas.

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
- Requiere Metrics Server para funcionar

**Metrics Server**:
- Recopila métricas de uso de CPU y memoria de los pods
- Esencial para que HPA pueda tomar decisiones de escalado

### 1.4 Red (Networking)

La red se configura en una **VCN** (`10.0.0.0/16`) con segmentación estricta para seguridad:

1.  **Public Subnet**: Aloja únicamente los Load Balancers públicos. Permite tráfico entrante desde internet.
2.  **Private Subnet (Nodes)**: Aloja los nodos de Kubernetes. Sin acceso directo desde internet (solo a través de NAT Gateway).
3.  **Private Subnet (Database)**: Aloja la VM de MySQL. Acceso restringido exclusivamente desde la subnet de K8s y administración interna.
4.  **K8s API Subnet**: Endpoint para la gestión del clúster (permitiendo acceso público o privado según configuración).

---

## 2. Almacenamiento y Base de Datos

### 2.1 Base de Datos (MySQL)

A diferencia de usar el servicio gestionado MDS (que no siempre entra en el Free Tier con suficiente potencia), SPIRITBLADE despliega MySQL 8.0 en una instancia **Compute independiente**.

- **Provisionamiento**: Automático vía `cloud-init` (`mysql-init.sh`).
- **Seguridad**:
    - No tiene IP pública.
    - Acceso SSH restringido a través de la VCN.
    - Puerto 3306 abierto solo para CIDR de la VCN.
    - Contraseña root configurada vía variable Terraform `mysql_root_password`.

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
mysql_root_password = "TuPasswordSegura123!" # Contraseña para la DB
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
- **MySQL Private IP**: IP interna de la base de datos (necesaria para `k8s/prod/secrets.yaml`).
- **S3 Credentials**: Access Key, Secret Key y Endpoint para Object Storage (necesario para `k8s/prod/secrets.yaml`).

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
  # MySQL root password (base64 encoded)
  mysql-root-password: unacontraseñaenbase64==
  
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

**a) Instalar Metrics Server (requerido para HPA)**:
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

**b) Instalar cert-manager (gestión automática de certificados SSL)**:
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.2/cert-manager.yaml
```

**c) Instalar NGINX Ingress Controller**:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.0/deploy/static/provider/cloud/deploy.yaml
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
kubectl apply -f mysql-external-service.yaml  # Mapea MySQL VM a service interno
kubectl apply -f redis-statefulset.yaml
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml
kubectl apply -f hpa.yaml  # Horizontal Pod Autoscaler
kubectl apply -f letsencrypt-issuer.yaml  # ClusterIssuer para Let's Encrypt
kubectl apply -f ingress.yaml  # Ingress con TLS automático
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

> **Nota sobre MySQL**: Dado que MySQL corre en una VM fuera del cluster (pero en la misma VCN), usamos un Recurso `Service` de tipo `ClusterIP` con Endpoints manuales para mapear la IP privada de la VM (ej. `10.0.20.42`) al nombre DNS `mysql-external` dentro de Kubernetes.

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

# Verificar conectividad MySQL
kubectl exec -n prod <backend-pod> -- curl -v telnet://mysql-external:3306
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
