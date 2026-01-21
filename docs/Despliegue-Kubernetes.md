# Guía de Despliegue Rápido - Spiritblade en Kubernetes

Guía paso a paso para desplegar Spiritblade en desarrollo y producción con Oracle Cloud.

## 🚀 Despliegue en Desarrollo (5 minutos)

### 1. Construir Imágenes

```bash
# Backend
cd backend
./mvnw clean package -DskipTests
docker build -t spiritblade-backend:dev .

# Frontend
cd ../frontend
npm install && npm run build
docker build -t spiritblade-frontend:dev .
```

### 2. Configurar Secrets

```bash
# Editar con tus API keys
nano k8s/dev/secrets.yaml
```

### 3. Desplegar

```bash
cd ../k8s/dev

# Crear namespace y secrets
kubectl apply -f namespace.yaml
kubectl apply -f secrets.yaml

# Desplegar servicios
kubectl apply -f mysql-deployment.yaml
kubectl apply -f redis-deployment.yaml
kubectl apply -f minio-deployment.yaml

# Esperar 2 minutos y desplegar aplicación
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml
```

### 4. Acceder

```bash
# Port-forward frontend
kubectl port-forward -n dev svc/frontend 8080:80

# Abrir: http://localhost:8080
```

---

## ☁️ Despliegue en Oracle Cloud (30 minutos)

### Fase 1: Preparación (10 min)

#### 1.1. Instalar Herramientas

```bash
# OCI CLI
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"

# Terraform (si no lo tienes)
choco install terraform  # Windows
brew install terraform   # macOS
```

#### 1.2. Configurar OCI

```bash
oci setup config
```

Necesitarás:
- Tenancy OCID (en la consola OCI, menú perfil → Tenancy)
- User OCID (menú perfil → User Settings)
- Región (ej: eu-frankfurt-1)
- Generar API key (User Settings → API Keys → Add API Key)

#### 1.3. Crear terraform.tfvars

```bash
cd iac/terraform
nano terraform.tfvars
```

Contenido mínimo:

```hcl
tenancy_ocid        = "ocid1.tenancy.oc1..aaaaaaaa..."
user_ocid           = "ocid1.user.oc1..aaaaaaaa..."
fingerprint         = "xx:xx:xx:..."
private_key_path    = "~/.oci/oci_api_key.pem"
region              = "eu-frankfurt-1"
compartment_ocid    = "ocid1.compartment.oc1..aaaaaaaa..."
node_image_id       = "ocid1.image.oc1.eu-frankfurt-1.aaaaaaaa..."
public_key_path     = "~/.ssh/id_rsa.pub"
mysql_root_password = "TuPasswordSeguro123!"
```

Para obtener `node_image_id`:

```bash
oci compute image list \
  --compartment-id <TU_COMPARTMENT_OCID> \
  --operating-system "Oracle Linux" \
  --shape "VM.Standard.A1.Flex" \
  --query 'data[?contains("display-name", `aarch64`)] | [0]."id"' \
  --raw-output
```

### Fase 2: Crear Infraestructura (15 min)

```bash
cd iac/terraform

# Inicializar
terraform init

# Verificar plan
terraform plan

# Crear infraestructura (tarda ~15 min)
terraform apply
# Escribir: yes

# Guardar outputs
terraform output > ../../outputs.txt
```

### Fase 3: Desplegar Aplicación (5 min)

#### 3.1. Configurar kubectl

```bash
# Obtener kubeconfig
oci ce cluster create-kubeconfig \
  --cluster-id $(terraform output -raw oke_cluster_id) \
  --file $HOME/.kube/config \
  --region $(terraform output -raw region) \
  --token-version 2.0.0 \
  --kube-endpoint PUBLIC_ENDPOINT

# Verificar
kubectl get nodes
```

#### 3.2. Construir imágenes ARM64

```bash
cd ../../

# Backend
cd backend
./mvnw clean package -DskipTests
docker buildx build --platform linux/arm64 \
  -t <TU_DOCKER_HUB>/spiritblade-backend:prod-arm64 \
  --push .

# Frontend
cd ../frontend
npm install && npm run build
docker buildx build --platform linux/arm64 \
  -t <TU_DOCKER_HUB>/spiritblade-frontend:prod-arm64 \
  --push .
```

#### 3.3. Crear Secrets

```bash
cd ../k8s/prod

# Obtener valores de Terraform
MYSQL_IP=$(cd ../../iac/terraform && terraform output -raw mysql_private_ip)
S3_ENDPOINT=$(cd ../../iac/terraform && terraform output -raw s3_compatible_endpoint)
S3_ACCESS=$(cd ../../iac/terraform && terraform output -raw s3_access_key)
S3_SECRET=$(cd ../../iac/terraform && terraform output -raw s3_secret_key)

# Crear secrets
cat > secrets-real.yaml <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: spiritblade-secrets
  namespace: prod
type: Opaque
stringData:
  mysql-root-password: "$(cd ../../iac/terraform && terraform output -raw mysql_root_password)"
  oci-storage-access-key: "$S3_ACCESS"
  oci-storage-secret-key: "$S3_SECRET"
  jwt-secret: "$(openssl rand -base64 64)"
  riot-api-key: "TU_RIOT_API_KEY"
  google-ai-api-key: "TU_GOOGLE_AI_KEY"
  ssl-password: "TuSSLPassword123"
EOF
```

#### 3.4. Actualizar Manifiestos

Edita `backend-deployment.yaml`:

```yaml
# Línea 20: Tu imagen
image: <TU_DOCKER_HUB>/spiritblade-backend:prod-arm64

# Línea 27: IP de MySQL
value: "jdbc:mysql://PEGAR_MYSQL_IP_AQUI:3306/spiritblade?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"

# Línea 44: Endpoint S3
value: "PEGAR_S3_ENDPOINT_AQUI"
```

Valores:
```bash
echo "MySQL IP: $MYSQL_IP"
echo "S3 Endpoint: $S3_ENDPOINT"
```

Edita `frontend-deployment.yaml`:

```yaml
# Línea 18: Tu imagen
image: <TU_DOCKER_HUB>/spiritblade-frontend:prod-arm64
```

#### 3.5. Desplegar

```bash
# Namespace y secrets
kubectl apply -f namespace.yaml
kubectl apply -f secrets-real.yaml

# Redis
kubectl apply -f redis-statefulset.yaml
kubectl wait --for=condition=ready pod -l app=redis -n prod --timeout=300s

# Backend y Frontend
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml
```

#### 3.6. Obtener URL

```bash
# Esperar a que el Load Balancer esté listo (~3 min)
kubectl get svc frontend -n prod --watch

# Obtener IP pública
kubectl get svc frontend -n prod -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

# Tu aplicación estará en: http://<IP_PUBLICA>
```

---

## ✅ Verificación

### Desarrollo

```bash
kubectl get pods -n dev
# Todos deben estar Running

kubectl logs -f -l app=backend -n dev
# Ver logs del backend

curl http://localhost:8080
# Debe devolver el frontend
```

### Producción

```bash
kubectl get pods -n prod
# Todos deben estar Running

kubectl get svc frontend -n prod
# Debe tener EXTERNAL-IP asignada

curl http://<EXTERNAL-IP>
# Debe devolver el frontend
```

---

## 🐛 Solución Rápida de Problemas

### Backend no inicia

```bash
# Ver logs
kubectl logs -l app=backend -n prod --tail=100

# Problema común: MySQL no accesible
# Verificar:
ssh opc@$(cd iac/terraform && terraform output -raw mysql_public_ip)
sudo systemctl status mysqld
```

### Frontend no carga

```bash
# Verificar servicio
kubectl get svc frontend -n prod

# Verificar pods
kubectl get pods -l app=frontend -n prod

# Ver logs
kubectl logs -l app=frontend -n prod
```

### Redis no conecta

```bash
# Verificar Redis
kubectl exec -it -n prod redis-0 -- redis-cli ping
# Debe responder: PONG
```

### Object Storage falla

```bash
# Verificar credenciales
kubectl get secret spiritblade-secrets -n prod -o yaml | grep storage

# Probar acceso
aws s3 ls --endpoint-url $(cd iac/terraform && terraform output -raw s3_compatible_endpoint) \
  --profile oci-s3
```

---

## 🗑️ Limpieza

### Desarrollo

```bash
kubectl delete namespace dev
```

### Producción

```bash
# Eliminar aplicación
kubectl delete namespace prod

# Eliminar infraestructura Oracle Cloud
cd iac/terraform
terraform destroy
# Escribir: yes
```

---

## 📊 Monitoreo Básico

```bash
# Ver todo
kubectl get all -n prod

# Ver uso de recursos
kubectl top nodes
kubectl top pods -n prod

# Ver logs en tiempo real
kubectl logs -f -l app=backend -n prod

# Ver eventos
kubectl get events -n prod --sort-by='.lastTimestamp'
```

---

## 🔄 Actualización de Aplicación

```bash
# 1. Construir nueva imagen
docker buildx build --platform linux/arm64 \
  -t <REPO>/spiritblade-backend:v1.1-arm64 \
  --push .

# 2. Actualizar deployment
kubectl set image deployment/backend \
  backend=<REPO>/spiritblade-backend:v1.1-arm64 \
  -n prod

# 3. Ver progreso
kubectl rollout status deployment/backend -n prod

# 4. Si hay problema, revertir
kubectl rollout undo deployment/backend -n prod
```

---

## 💰 Costos (Oracle Cloud Free Tier)

- ✅ 2 nodos OKE (2 OCPUs ARM) - **GRATIS**
- ✅ 1 instancia MySQL (1 OCPU ARM) - **GRATIS**
- ✅ 150 GB almacenamiento - **GRATIS**
- ✅ 1 Load Balancer - **GRATIS**
- ✅ 10 TB transferencia/mes - **GRATIS**

**Total: $0/mes** 🎉

---

## 📚 Más Información

- Documentación completa: [k8s/README.md](../k8s/README.md)
- Configuración Terraform: [iac/terraform/README.md](../iac/terraform/README.md)
- Documentación OCI: https://docs.oracle.com/en-us/iaas/
- Documentación Kubernetes: https://kubernetes.io/docs/
