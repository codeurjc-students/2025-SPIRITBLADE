# Despliegue de Spiritblade en Kubernetes

Esta guía cubre el despliegue completo de Spiritblade tanto en entornos de desarrollo (local/dev) como en producción (Oracle Cloud).

## Tabla de Contenidos

1. [Arquitectura](#arquitectura)
2. [Despliegue en Desarrollo](#despliegue-en-desarrollo)
3. [Despliegue en Producción](#despliegue-en-producción)
4. [Gestión y Mantenimiento](#gestión-y-mantenimiento)

---

## Arquitectura

### Entorno de Desarrollo
```
┌─────────────────────────────────────────────────┐
│           Kubernetes Cluster (Local)            │
├─────────────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌──────────┐      │
│  │ Frontend │  │ Backend  │  │  Redis   │      │
│  │  (1pod)  │  │  (1pod)  │  │  (1pod)  │      │
│  └──────────┘  └──────────┘  └──────────┘      │
│                                                  │
│  ┌──────────┐  ┌──────────┐                    │
│  │  MySQL   │  │  MinIO   │                    │
│  │  (1pod)  │  │  (1pod)  │                    │
│  └──────────┘  └──────────┘                    │
└─────────────────────────────────────────────────┘
```

### Entorno de Producción (Oracle Cloud)
```
┌──────────────────────────────────────────────────────────────┐
│                    Oracle Cloud (OCI)                         │
├──────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────┐  │
│  │         OKE Cluster (Kubernetes)                       │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐            │  │
│  │  │ Frontend │  │ Backend  │  │  Redis   │            │  │
│  │  │ (2 pods) │  │ (2 pods) │  │(StatefulS│            │  │
│  │  └──────────┘  └──────────┘  └──────────┘            │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │    MySQL     │  │    Object    │                         │
│  │  (Compute)   │  │   Storage    │                         │
│  │  VM.A1.Flex  │  │ (S3-compat)  │                         │
│  └──────────────┘  └──────────────┘                         │
└──────────────────────────────────────────────────────────────┘
```

---

## Despliegue en Desarrollo

### Prerrequisitos

- Docker Desktop o Minikube instalado
- kubectl instalado y configurado
- Imágenes Docker construidas localmente

### 1. Construir Imágenes Docker

#### Backend
```bash
cd backend
./mvnw clean package -DskipTests
docker build -t spiritblade-backend:dev .
```

#### Frontend
```bash
cd frontend
npm install
npm run build
docker build -t spiritblade-frontend:dev .
```

### 2. Crear el Namespace

```bash
kubectl apply -f k8s/dev/namespace.yaml
```

### 3. Configurar Secrets

Edita el archivo `k8s/dev/secrets.yaml` con tus valores reales:

```bash
# Editar el archivo con tus credenciales
nano k8s/dev/secrets.yaml

# Aplicar los secrets
kubectl apply -f k8s/dev/secrets.yaml
```

### 4. Desplegar los Servicios

```bash
# Desplegar MySQL
kubectl apply -f k8s/dev/mysql-deployment.yaml

# Desplegar Redis
kubectl apply -f k8s/dev/redis-deployment.yaml

# Desplegar MinIO
kubectl apply -f k8s/dev/minio-deployment.yaml

# Esperar a que las bases de datos estén listas
kubectl wait --for=condition=ready pod -l app=mysql -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=minio -n dev --timeout=300s

# Desplegar Backend
kubectl apply -f k8s/dev/backend-deployment.yaml

# Desplegar Frontend
kubectl apply -f k8s/dev/frontend-deployment.yaml
```

### 5. Verificar el Despliegue

```bash
# Ver todos los pods
kubectl get pods -n dev

# Ver los servicios
kubectl get svc -n dev

# Ver logs del backend
kubectl logs -f -l app=backend -n dev

# Ver logs del frontend
kubectl logs -f -l app=frontend -n dev
```

### 6. Acceder a la Aplicación

#### Usando Port-Forward

```bash
# Frontend (en una terminal)
kubectl port-forward -n dev svc/frontend 8080:80

# Backend (en otra terminal)
kubectl port-forward -n dev svc/backend 8443:443

# MinIO Console (opcional, para ver los archivos)
kubectl port-forward -n dev svc/minio 9001:9001
```

Luego accede a:
- Frontend: http://localhost:8080
- Backend API: https://localhost:8443
- MinIO Console: http://localhost:9001

#### Usando NodePort

Si tu cluster soporta NodePort:

```bash
# Obtener el puerto del frontend
kubectl get svc frontend -n dev

# Acceder usando la IP del nodo y el puerto NodePort
# Por ejemplo: http://<NODE_IP>:<NODEPORT>
```

### 7. Inicializar MinIO Bucket

```bash
# Crear el bucket en MinIO
kubectl exec -n dev -it $(kubectl get pod -n dev -l app=minio -o jsonpath='{.items[0].metadata.name}') -- /bin/sh

# Dentro del pod de MinIO:
mc alias set local http://localhost:9000 minioadmin minioadmin123
mc mb local/spiritblade-uploads
exit
```

---

## Despliegue en Producción

### Prerrequisitos

- Cuenta de Oracle Cloud Infrastructure (OCI)
- OCI CLI instalado y configurado
- Terraform instalado (v1.0+)
- kubectl instalado
- Imágenes Docker construidas para ARM64

### 1. Preparar Imágenes para ARM64

Oracle Cloud utiliza procesadores ARM (Ampere), por lo que necesitas construir imágenes para ARM64:

```bash
# Backend
cd backend
./mvnw clean package -DskipTests
docker buildx build --platform linux/arm64 -t <YOUR_REPO>/spiritblade-backend:prod-arm64 --push .

# Frontend
cd frontend
npm install
npm run build
docker buildx build --platform linux/arm64 -t <YOUR_REPO>/spiritblade-frontend:prod-arm64 --push .
```

### 2. Configurar Oracle Cloud CLI

```bash
# Instalar OCI CLI
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"

# Configurar OCI CLI
oci setup config
```

Necesitarás:
- Tu Tenancy OCID
- Tu User OCID
- Tu región (ej: eu-frankfurt-1)
- Generar una API Key

### 3. Configurar Variables de Terraform

Crea un archivo `terraform.tfvars` en `iac/terraform/`:

```hcl
# iac/terraform/terraform.tfvars

# Credenciales de OCI
tenancy_ocid     = "ocid1.tenancy.oc1..aaaaaaaaxxxxx"
user_ocid        = "ocid1.user.oc1..aaaaaaaaxxxxx"
fingerprint      = "xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx:xx"
private_key_path = "~/.oci/oci_api_key.pem"
region           = "eu-frankfurt-1"
compartment_ocid = "ocid1.compartment.oc1..aaaaaaaaxxxxx"

# Configuración del proyecto
project_name = "spiritblade"
vcn_cidr     = "10.0.0.0/16"

# Node image (buscar en la consola de OCI para tu región)
# Debe ser Oracle Linux 8 o 9 para ARM64 (aarch64)
node_image_id = "ocid1.image.oc1.eu-frankfurt-1.aaaaaaaaxxxxx"

# SSH Key para acceso a instancias
public_key_path = "~/.ssh/id_rsa.pub"

# Contraseña de MySQL
mysql_root_password = "TU_CONTRASEÑA_SEGURA_AQUI"

# Código de región para Object Storage
oci_region_shortcode = "fra" # fra para Frankfurt, iad para Ashburn, etc.
```

### 4. Desplegar Infraestructura con Terraform

```bash
cd iac/terraform

# Inicializar Terraform
terraform init

# Ver el plan de ejecución
terraform plan

# Aplicar la configuración (creará toda la infraestructura)
terraform apply

# Guardar los outputs importantes
terraform output > outputs.txt
```

Este proceso creará:
- VCN (Virtual Cloud Network) con subnets públicas y privadas
- Cluster de Kubernetes (OKE) con 2 nodos ARM
- Instancia de Compute con MySQL
- Object Storage bucket (compatible con S3)
- Customer Secret Keys para acceso S3

### 5. Configurar kubectl para OKE

```bash
# Obtener el comando del output de Terraform o usar:
export CLUSTER_ID=$(terraform output -raw oke_cluster_id)
export REGION=$(terraform output -raw region)

# Crear kubeconfig
oci ce cluster create-kubeconfig \
  --cluster-id $CLUSTER_ID \
  --file $HOME/.kube/config \
  --region $REGION \
  --token-version 2.0.0 \
  --kube-endpoint PUBLIC_ENDPOINT

# Verificar conexión
kubectl get nodes
```

### 6. Configurar Secrets de Producción

Obtén los valores de los outputs de Terraform:

```bash
# Ver outputs
terraform output

# Crear archivo de secrets con valores reales
cat > k8s/prod/secrets-real.yaml <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: spiritblade-secrets
  namespace: prod
type: Opaque
stringData:
  mysql-root-password: "$(terraform output -raw mysql_root_password)"
  oci-storage-access-key: "$(terraform output -raw s3_access_key)"
  oci-storage-secret-key: "$(terraform output -raw s3_secret_key)"
  jwt-secret: "$(openssl rand -base64 64)"
  riot-api-key: "TU_RIOT_API_KEY"
  google-ai-api-key: "TU_GOOGLE_AI_KEY"
  ssl-password: "TU_SSL_PASSWORD"
EOF
```

### 7. Actualizar Manifiestos de Producción

Edita `k8s/prod/backend-deployment.yaml` y actualiza:

```yaml
# Línea ~25: IP privada de MySQL
- name: SPRING_DATASOURCE_URL
  value: "jdbc:mysql://<MYSQL_PRIVATE_IP>:3306/spiritblade?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"

# Línea ~44: Endpoint de Object Storage
- name: MINIO_ENDPOINT
  value: "https://<NAMESPACE>.compat.objectstorage.<REGION>.oraclecloud.com"
```

Obtén estos valores:
```bash
# IP privada de MySQL
terraform output -raw mysql_private_ip

# Endpoint de Object Storage
terraform output -raw s3_compatible_endpoint
```

### 8. Desplegar en Producción

```bash
# Crear namespace
kubectl apply -f k8s/prod/namespace.yaml

# Aplicar secrets
kubectl apply -f k8s/prod/secrets-real.yaml

# Desplegar Redis
kubectl apply -f k8s/prod/redis-statefulset.yaml

# Esperar a que Redis esté listo
kubectl wait --for=condition=ready pod -l app=redis -n prod --timeout=300s

# Desplegar Backend
kubectl apply -f k8s/prod/backend-deployment.yaml

# Desplegar Frontend
kubectl apply -f k8s/prod/frontend-deployment.yaml
```

### 9. Verificar Despliegue en Producción

```bash
# Ver todos los recursos
kubectl get all -n prod

# Ver logs
kubectl logs -f -l app=backend -n prod
kubectl logs -f -l app=frontend -n prod

# Describir el servicio del frontend para obtener la IP del Load Balancer
kubectl describe svc frontend -n prod
```

### 10. Obtener la URL de la Aplicación

```bash
# Obtener la IP pública del Load Balancer
kubectl get svc frontend -n prod -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

# La aplicación estará disponible en:
# http://<LOAD_BALANCER_IP>
```

### 11. Configurar DNS (Opcional)

Si tienes un dominio, apunta tu registro A al Load Balancer:

```
Type: A
Name: spiritblade.tudominio.com
Value: <LOAD_BALANCER_IP>
TTL: 300
```

---

## Gestión y Mantenimiento

### Monitoreo

```bash
# Ver estado general
kubectl get pods -n prod --watch

# Ver uso de recursos
kubectl top nodes
kubectl top pods -n prod

# Ver eventos
kubectl get events -n prod --sort-by='.lastTimestamp'
```

### Logs

```bash
# Logs de todos los pods de backend
kubectl logs -f -l app=backend -n prod --tail=100

# Logs de un pod específico
kubectl logs -f <POD_NAME> -n prod

# Logs anteriores (si el pod se reinició)
kubectl logs --previous <POD_NAME> -n prod
```

### Escalado

```bash
# Escalar backend
kubectl scale deployment backend -n prod --replicas=3

# Escalar frontend
kubectl scale deployment frontend -n prod --replicas=3

# Ver el estado del escalado
kubectl get deployment -n prod
```

### Actualizaciones

#### Rolling Update del Backend

```bash
# Construir nueva imagen
docker buildx build --platform linux/arm64 -t <YOUR_REPO>/spiritblade-backend:v1.1-arm64 --push .

# Actualizar el deployment
kubectl set image deployment/backend backend=<YOUR_REPO>/spiritblade-backend:v1.1-arm64 -n prod

# Ver el progreso
kubectl rollout status deployment/backend -n prod

# Si algo sale mal, revertir
kubectl rollout undo deployment/backend -n prod
```

### Backup de MySQL

```bash
# Conectar a la instancia de MySQL
ssh opc@$(terraform output -raw mysql_public_ip)

# Dentro de la instancia:
mysqldump -u root -p spiritblade > backup_$(date +%Y%m%d).sql

# Copiar backup a Object Storage
oci os object put --bucket-name spiritblade-bucket --file backup_$(date +%Y%m%d).sql --name backups/mysql/backup_$(date +%Y%m%d).sql
```

### Backup de Redis

```bash
# Redis hace backups automáticos, pero puedes forzar uno:
kubectl exec -n prod redis-0 -- redis-cli BGSAVE

# Copiar el dump a un lugar seguro
kubectl cp prod/redis-0:/data/dump.rdb ./redis-backup.rdb
```

### Limpieza de Recursos

```bash
# Eliminar despliegue de dev
kubectl delete namespace dev

# Eliminar despliegue de prod
kubectl delete namespace prod

# Destruir infraestructura de Oracle Cloud
cd iac/terraform
terraform destroy
```

### Solución de Problemas

#### Backend no puede conectar a MySQL

```bash
# Verificar que MySQL esté corriendo
ssh opc@$(terraform output -raw mysql_public_ip)
sudo systemctl status mysqld

# Verificar conectividad desde un pod
kubectl run -it --rm debug --image=mysql:8.0 --restart=Never -n prod -- \
  mysql -h <MYSQL_PRIVATE_IP> -u root -p
```

#### Backend no puede acceder a Object Storage

```bash
# Verificar que las credenciales sean correctas
kubectl exec -it -n prod <BACKEND_POD> -- env | grep MINIO

# Probar acceso manualmente
kubectl exec -it -n prod <BACKEND_POD> -- curl -I <OBJECT_STORAGE_ENDPOINT>
```

#### Pods en estado CrashLoopBackOff

```bash
# Ver los logs del pod
kubectl logs <POD_NAME> -n prod --previous

# Describir el pod para ver eventos
kubectl describe pod <POD_NAME> -n prod

# Verificar recursos disponibles
kubectl top nodes
```

---

## Costos en Oracle Cloud (Free Tier)

Oracle Cloud ofrece un Free Tier generoso que incluye:

- **Compute**: 4 OCPUs ARM Ampere (VM.Standard.A1.Flex) - **GRATIS SIEMPRE**
- **Block Storage**: 200 GB - **GRATIS SIEMPRE**
- **Object Storage**: 20 GB - **GRATIS SIEMPRE**
- **Load Balancer**: 1 instancia - **GRATIS SIEMPRE**
- **Outbound Data Transfer**: 10 TB/mes - **GRATIS SIEMPRE**

Nuestra configuración usa:
- 2 OCPUs para OKE (2 nodos × 1 OCPU)
- 1 OCPU para MySQL
- 1 OCPU restante libre
- ~150 GB de Block Storage
- ~10 GB de Object Storage

**Todo dentro del Free Tier** ✅

---

## Referencias

- [Oracle Cloud Infrastructure Documentation](https://docs.oracle.com/en-us/iaas/Content/home.htm)
- [OKE Documentation](https://docs.oracle.com/en-us/iaas/Content/ContEng/home.htm)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)
- [Terraform OCI Provider](https://registry.terraform.io/providers/oracle/oci/latest/docs)
