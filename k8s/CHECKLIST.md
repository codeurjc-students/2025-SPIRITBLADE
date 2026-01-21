# Lista de Verificación de Despliegue - Spiritblade

## ✅ Desarrollo

### 1. Pre-requisitos
```bash
# Verificar Docker
docker --version

# Verificar kubectl
kubectl version --client

# Verificar cluster
kubectl cluster-info
```

### 2. Despliegue
```bash
# Ejecutar script de despliegue
cd k8s/dev
./deploy.sh  # Linux/Mac
# o
.\deploy.ps1  # Windows
```

### 3. Verificación
```bash
# Ver todos los pods (deben estar Running)
kubectl get pods -n dev

# Ver servicios
kubectl get svc -n dev

# Ver PVCs
kubectl get pvc -n dev

# Verificar logs del backend
kubectl logs -l app=backend -n dev --tail=20

# Verificar logs del frontend
kubectl logs -l app=frontend -n dev --tail=20
```

### 4. Acceso
```bash
# Terminal 1: Port-forward Frontend
kubectl port-forward -n dev svc/frontend 8080:80

# Terminal 2: Port-forward Backend (opcional)
kubectl port-forward -n dev svc/backend 8443:443

# Terminal 3: Port-forward MinIO Console (opcional)
kubectl port-forward -n dev svc/minio 9001:9001

# Abrir navegador
# Frontend: http://localhost:8080
# Backend API: https://localhost:8443/actuator/health
# MinIO Console: http://localhost:9001
```

### 5. Pruebas Funcionales
```bash
# Probar backend health check
curl -k https://localhost:8443/actuator/health

# Probar frontend
curl http://localhost:8080

# Conectar a MySQL
kubectl exec -it -n dev $(kubectl get pod -n dev -l app=mysql -o jsonpath='{.items[0].metadata.name}') -- mysql -u root -p

# Probar Redis
kubectl exec -it -n dev $(kubectl get pod -n dev -l app=redis -o jsonpath='{.items[0].metadata.name}') -- redis-cli ping
```

---

## ☁️ Producción (Oracle Cloud)

### 1. Pre-requisitos
```bash
# Verificar OCI CLI
oci --version

# Verificar Terraform
terraform --version

# Verificar autenticación OCI
oci iam region list
```

### 2. Configuración
```bash
cd iac/terraform

# Crear terraform.tfvars con tus credenciales
nano terraform.tfvars

# Verificar configuración
terraform validate
```

### 3. Despliegue de Infraestructura
```bash
# Inicializar Terraform
terraform init

# Ver plan
terraform plan

# Aplicar (crear infraestructura)
terraform apply
# Escribir: yes
# Esperar ~15-20 minutos

# Guardar outputs
terraform output > ../../outputs.txt
```

### 4. Verificación de Infraestructura
```bash
# Verificar VCN
terraform output vcn_id

# Verificar Cluster OKE
terraform output oke_cluster_id

# Verificar MySQL
terraform output mysql_private_ip
terraform output mysql_public_ip

# Verificar Object Storage
terraform output object_storage_namespace
terraform output s3_compatible_endpoint
```

### 5. Configurar kubectl
```bash
# Obtener kubeconfig
oci ce cluster create-kubeconfig \
  --cluster-id $(terraform output -raw oke_cluster_id) \
  --file $HOME/.kube/config \
  --region $(terraform output -raw region) \
  --token-version 2.0.0 \
  --kube-endpoint PUBLIC_ENDPOINT

# Verificar conexión
kubectl get nodes
# Debe mostrar 2 nodos en Ready
```

### 6. Construir Imágenes ARM64
```bash
cd ../../

# Backend
cd backend
./mvnw clean package -DskipTests
docker buildx build --platform linux/arm64 \
  -t <TU_DOCKERHUB>/spiritblade-backend:prod-arm64 \
  --push .

# Frontend
cd ../frontend
npm install
npm run build
docker buildx build --platform linux/arm64 \
  -t <TU_DOCKERHUB>/spiritblade-frontend:prod-arm64 \
  --push .
```

### 7. Preparar Secrets
```bash
cd ../k8s/prod

# Obtener valores de Terraform
cd ../../iac/terraform
export MYSQL_IP=$(terraform output -raw mysql_private_ip)
export S3_ENDPOINT=$(terraform output -raw s3_compatible_endpoint)
export S3_ACCESS_KEY=$(terraform output -raw s3_access_key)
export S3_SECRET_KEY=$(terraform output -raw s3_secret_key)
export MYSQL_PASSWORD=$(terraform output -raw mysql_root_password)

# Mostrar valores
echo "MySQL IP: $MYSQL_IP"
echo "S3 Endpoint: $S3_ENDPOINT"
echo "S3 Access Key: $S3_ACCESS_KEY"

cd ../../k8s/prod

# Crear secrets
cat > secrets-real.yaml <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: spiritblade-secrets
  namespace: prod
type: Opaque
stringData:
  mysql-root-password: "$MYSQL_PASSWORD"
  oci-storage-access-key: "$S3_ACCESS_KEY"
  oci-storage-secret-key: "$S3_SECRET_KEY"
  jwt-secret: "$(openssl rand -base64 64)"
  riot-api-key: "TU_RIOT_API_KEY_AQUI"
  google-ai-api-key: "TU_GOOGLE_AI_KEY_AQUI"
  ssl-password: "TuSSLPassword123"
EOF
```

### 8. Actualizar Manifiestos
```bash
# Editar backend-deployment.yaml
# Cambiar:
# - Línea 20: imagen a <TU_DOCKERHUB>/spiritblade-backend:prod-arm64
# - Línea 27: value a "jdbc:mysql://$MYSQL_IP:3306/spiritblade?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
# - Línea 44: value a "$S3_ENDPOINT"

nano backend-deployment.yaml

# Editar frontend-deployment.yaml
# Cambiar:
# - Línea 18: imagen a <TU_DOCKERHUB>/spiritblade-frontend:prod-arm64

nano frontend-deployment.yaml
```

### 9. Desplegar en Kubernetes
```bash
# Crear namespace
kubectl apply -f namespace.yaml

# Aplicar secrets
kubectl apply -f secrets-real.yaml

# Desplegar Redis
kubectl apply -f redis-statefulset.yaml

# Esperar a que Redis esté listo
kubectl wait --for=condition=ready pod -l app=redis -n prod --timeout=300s

# Desplegar Backend
kubectl apply -f backend-deployment.yaml

# Desplegar Frontend
kubectl apply -f frontend-deployment.yaml

# Esperar a que esté listo
kubectl wait --for=condition=ready pod -l app=backend -n prod --timeout=300s
kubectl wait --for=condition=ready pod -l app=frontend -n prod --timeout=300s
```

### 10. Verificación del Despliegue
```bash
# Ver todos los recursos
kubectl get all -n prod

# Ver pods (deben estar Running)
kubectl get pods -n prod

# Ver servicios
kubectl get svc -n prod

# Obtener IP del Load Balancer
kubectl get svc frontend -n prod -o jsonpath='{.status.loadBalancer.ingress[0].ip}'

# Ver logs del backend
kubectl logs -l app=backend -n prod --tail=50

# Ver logs del frontend
kubectl logs -l app=frontend -n prod --tail=50

# Ver eventos
kubectl get events -n prod --sort-by='.lastTimestamp'
```

### 11. Verificación de MySQL
```bash
# Conectar via SSH a MySQL
ssh opc@$(cd ../../iac/terraform && terraform output -raw mysql_public_ip)

# Dentro de la instancia:
sudo systemctl status mysqld
mysql -u root -p
# Usar password de terraform output mysql_root_password

# En MySQL:
SHOW DATABASES;
USE spiritblade;
SHOW TABLES;
```

### 12. Verificación de Object Storage
```bash
# Configurar AWS CLI (compatible con OCI)
aws configure --profile oci-s3
# Access Key: del output de Terraform
# Secret Key: del output de Terraform

# Listar buckets
aws s3 ls --profile oci-s3 --endpoint-url $(cd ../../iac/terraform && terraform output -raw s3_compatible_endpoint)

# Probar subida de archivo
echo "test" > test.txt
aws s3 cp test.txt s3://spiritblade-bucket/ --profile oci-s3 --endpoint-url $(cd ../../iac/terraform && terraform output -raw s3_compatible_endpoint)
```

### 13. Pruebas Funcionales
```bash
# Obtener IP pública
export LB_IP=$(kubectl get svc frontend -n prod -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo "Load Balancer IP: $LB_IP"

# Probar frontend
curl http://$LB_IP

# Probar backend health (desde dentro del cluster)
kubectl run curl-test --image=curlimages/curl -i --rm --restart=Never -n prod -- \
  curl -k https://backend:443/actuator/health

# Ver métricas del cluster
kubectl top nodes
kubectl top pods -n prod
```

### 14. Monitoreo Continuo
```bash
# Watch pods
kubectl get pods -n prod --watch

# Stream logs del backend
kubectl logs -f -l app=backend -n prod

# Stream logs del frontend
kubectl logs -f -l app=frontend -n prod

# Ver eventos en tiempo real
kubectl get events -n prod --watch
```

---

## 🐛 Checklist de Solución de Problemas

### Backend no inicia
- [ ] Verificar logs: `kubectl logs -l app=backend -n prod`
- [ ] Verificar secrets: `kubectl get secret spiritblade-secrets -n prod`
- [ ] Verificar conectividad a MySQL: `kubectl exec -it -n prod <backend-pod> -- ping <MYSQL_IP>`
- [ ] Verificar variables de entorno: `kubectl exec -it -n prod <backend-pod> -- env | grep SPRING`

### Frontend no carga
- [ ] Verificar pods: `kubectl get pods -l app=frontend -n prod`
- [ ] Verificar servicio: `kubectl get svc frontend -n prod`
- [ ] Verificar Load Balancer: `kubectl describe svc frontend -n prod`
- [ ] Verificar logs: `kubectl logs -l app=frontend -n prod`

### MySQL no accesible
- [ ] SSH a instancia: `ssh opc@<MYSQL_PUBLIC_IP>`
- [ ] Verificar MySQL: `sudo systemctl status mysqld`
- [ ] Verificar firewall: `sudo firewall-cmd --list-all`
- [ ] Verificar Security Group en OCI Console

### Object Storage falla
- [ ] Verificar credenciales en secrets
- [ ] Verificar endpoint en backend deployment
- [ ] Probar con AWS CLI
- [ ] Verificar permisos en OCI Console

### Redis no conecta
- [ ] Verificar pod: `kubectl get pod redis-0 -n prod`
- [ ] Probar ping: `kubectl exec -it redis-0 -n prod -- redis-cli ping`
- [ ] Verificar PVC: `kubectl get pvc -n prod`
- [ ] Ver logs: `kubectl logs redis-0 -n prod`

---

## 📊 Métricas de Éxito

### Desarrollo
- ✅ 5 pods Running en namespace dev
- ✅ Todos los services activos
- ✅ PVCs bound
- ✅ Frontend accesible en localhost:8080
- ✅ Backend health check retorna status UP

### Producción
- ✅ 5 pods Running en namespace prod (2 backend, 2 frontend, 1 redis)
- ✅ Load Balancer tiene IP pública
- ✅ MySQL accesible desde backend
- ✅ Object Storage configurado y accesible
- ✅ Frontend accesible desde internet
- ✅ Backend health check retorna status UP
- ✅ Todos los nodos en estado Ready
- ✅ Sin errores en logs

---

## 🔄 Comandos de Mantenimiento

### Escalado
```bash
# Escalar backend
kubectl scale deployment backend --replicas=3 -n prod

# Escalar frontend
kubectl scale deployment frontend --replicas=3 -n prod
```

### Actualización
```bash
# Rolling update
kubectl set image deployment/backend backend=nueva-imagen:tag -n prod
kubectl rollout status deployment/backend -n prod

# Rollback
kubectl rollout undo deployment/backend -n prod
```

### Backup
```bash
# Backup de MySQL
ssh opc@<MYSQL_IP>
mysqldump -u root -p spiritblade > backup_$(date +%Y%m%d).sql

# Backup de Redis
kubectl exec redis-0 -n prod -- redis-cli BGSAVE
kubectl cp prod/redis-0:/data/dump.rdb ./redis-backup-$(date +%Y%m%d).rdb
```

### Limpieza
```bash
# Limpiar desarrollo
kubectl delete namespace dev

# Limpiar producción (CUIDADO!)
kubectl delete namespace prod

# Destruir infraestructura (CUIDADO!)
cd iac/terraform
terraform destroy
```

---

**Última actualización**: Enero 2026  
**Estado**: ✅ Verificado y funcional
