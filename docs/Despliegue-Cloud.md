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
        
        subgraph VCN[VCN 10.0.0.0/16]
            subgraph Public_Subnet[Public Subnet]
                LB[Load Balancer]
            end
            
            subgraph Private_Subnet_K8s[Private Subnet (K8s Nodes)]
                K8s_Node1[Worker Node 1 (ARM)]
                K8s_Node2[Worker Node 2 (ARM)]
                
                Redis[Redis Service]
                Backend[Backend Pods]
                Frontend[Frontend Pods]
            end
            
            subgraph Private_Subnet_DB[Private Subnet (Database)]
                MySQL_VM[MySQL Compute Instance (ARM)]
            end
        end
    end

    User((Usuario)) --> |HTTPS| LB
    LB --> Frontend
    Frontend --> Backend
    Backend --> |Private IP| MySQL_VM
    Backend --> |Private Network| Redis
    Backend --> |HTTPS| S3
    
    K8s_Node1 --> NAT
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

### 1.3 Red (Networking)

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

Edita `k8s/prod/secrets.yaml` utilizando los valores obtenidos de Terraform:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: spiritblade-secrets
type: Opaque
stringData:
  # Base de Datos (IP privada de la instancia MySQL creada por Terraform)
  DB_HOST: "10.0.X.X"
  DB_PORT: "3306"
  DB_NAME: "spiritblade"
  DB_USER: "root"
  DB_PASSWORD: "TuPasswordSegura123!" # La misma que en terraform.tfvars

  # Riot API
  RIOT_API_KEY: "RGAPI-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"

  # JWT
  JWT_SECRET: "TuClaveSecretaJWTMuyLargaYSegura..."

  # S3 / Object Storage (Outputs de Terraform)
  MINIO_ENDPOINT: "https://{namespace}.compat.objectstorage.{region}.oraclecloud.com"
  MINIO_ACCESS_KEY: "{s3_access_key}"
  MINIO_SECRET_KEY: "{s3_secret_key}"
  MINIO_BUCKET_NAME: "spiritblade-bucket"
```

### Paso 3: Desplegar Manifiestos

```bash
cd k8s/prod
kubectl apply -f namespace.yaml
kubectl apply -f secrets.yaml
kubectl apply -f mysql-external-service.yaml # Define el servicio para la IP externa de MySQL
kubectl apply -f .
```

> **Nota sobre MySQL**: Dado que MySQL corre en una VM fuera del cluster (pero en la misma VCN), usamos un Recurso `Service` de tipo `ExternalName` o `ClusterIP` con Endpoints manuales para mapear la IP privada de la VM al nombre DNS `mysql` dentro de Kubernetes.

---

## 5. Mantenimiento y Operaciones

### Acceso SSH a Nodos y DB

Como las instancias están en subredes privadas, no puedes hacer SSH directo.
**Solución**: Usar una instancia "Bastion" o configurar temporalmente una IP pública en una instancia efímera, o usar el servicio **OCI Bastion**.

### Actualización de Imágenes

El pipeline de CI/CD (GitHub Actions) construye y publica imágenes multi-arquitectura (`linux/amd64` y `linux/arm64`). Para actualizar en producción:

```bash
kubectl rollout restart deployment/spiritblade-backend -n spiritblade
kubectl rollout restart deployment/spiritblade-frontend -n spiritblade
```

### Limpieza de Recursos (Destrucción)

Para eliminar toda la infraestructura y detener la facturación (aunque sea gratuito, limpia el tenant):

```bash
cd iac/terraform
terraform destroy
```
