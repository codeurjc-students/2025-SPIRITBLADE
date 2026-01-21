# Spiritblade - Infraestructura Kubernetes

## 📋 Resumen Ejecutivo

Se ha implementado una infraestructura completa de Kubernetes para Spiritblade con dos entornos:

- **Desarrollo (Local)**: Para pruebas rápidas en Minikube/Docker Desktop
- **Producción (Oracle Cloud)**: Despliegue en la nube usando Oracle Cloud Free Tier

## 🎯 Objetivos Cumplidos

### ✅ Primera Parte: Manifiestos K8s para Dev
- [x] Backend (Deployment + Service)
- [x] Frontend (Deployment + Service)
- [x] MySQL (Deployment + Service + PVC)
- [x] Redis (Deployment + Service + PVC)
- [x] MinIO (Deployment + Service + PVC)

### ✅ Segunda Parte: Infraestructura en Oracle Cloud
- [x] OKE (Oracle Kubernetes Engine) - Reemplazo de AWS EKS
- [x] MySQL en Compute Instance - Reemplazo de AWS RDS
- [x] Object Storage - Reemplazo de AWS S3
- [x] Terraform para IaC - Mejor que CloudFormation

### ✅ Tercera Parte: Manifiestos K8s para Prod
- [x] Backend con alta disponibilidad (2 réplicas)
- [x] Frontend con Load Balancer
- [x] Redis con StatefulSet y persistencia
- [x] Integración con servicios de Oracle Cloud

## 🚀 Quick Start

### Desarrollo (Local)
```bash
# PowerShell
cd k8s/dev
.\deploy.ps1

# Bash/Linux/Mac
cd k8s/dev
./deploy.sh

# Acceder
kubectl port-forward -n dev svc/frontend 8080:80
# Abrir: http://localhost:8080
```

### Producción (Oracle Cloud)
```bash
# 1. Configurar Terraform
cd iac/terraform
nano terraform.tfvars  # Añadir tus credenciales de OCI

# 2. Desplegar infraestructura
terraform init
terraform apply

# 3. Configurar kubectl
oci ce cluster create-kubeconfig --cluster-id $(terraform output -raw oke_cluster_id) --file ~/.kube/config

# 4. Desplegar aplicación
cd ../../k8s/prod
# Editar backend-deployment.yaml con IPs de Terraform
kubectl apply -f namespace.yaml
kubectl apply -f secrets.yaml
kubectl apply -f redis-statefulset.yaml
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml

# 5. Obtener URL
kubectl get svc frontend -n prod
```

## 📁 Estructura de Archivos

```
k8s/
├── dev/                           # Entorno de desarrollo
│   ├── namespace.yaml
│   ├── secrets.yaml
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   ├── mysql-deployment.yaml
│   ├── redis-deployment.yaml
│   ├── minio-deployment.yaml
│   ├── deploy.sh                  # Script de despliegue (Linux/Mac)
│   ├── deploy.ps1                 # Script de despliegue (Windows)
│   ├── cleanup.sh                 # Script de limpieza (Linux/Mac)
│   └── cleanup.ps1                # Script de limpieza (Windows)
│
├── prod/                          # Entorno de producción
│   ├── namespace.yaml
│   ├── secrets.yaml
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   └── redis-statefulset.yaml
│
├── README.md                      # Documentación completa
├── IMPLEMENTACION.md              # Resumen de implementación
└── .gitignore

iac/
└── terraform/                     # Infraestructura como código
    ├── variables.tf               # Variables y outputs
    ├── network-updated.tf         # VCN, subnets, security
    ├── oke-updated.tf             # Cluster Kubernetes
    ├── mysql-updated.tf           # Base de datos MySQL
    ├── storage-updated.tf         # Object Storage
    ├── mysql-init.sh              # Script de inicialización MySQL
    ├── README.md                  # Guía de Terraform
    └── .gitignore

docs/
└── Despliegue-Kubernetes.md       # Guía rápida de despliegue
```

## 💰 Costos

### Oracle Cloud Free Tier (Permanente)
- **OKE Cluster**: Panel de control GRATIS
- **2 Nodos ARM (Ampere A1)**: 2 OCPUs, 12GB RAM - GRATIS
- **MySQL Instance (ARM)**: 1 OCPU, 6GB RAM - GRATIS
- **Block Storage**: 150GB de 200GB - GRATIS
- **Object Storage**: Hasta 20GB - GRATIS
- **Load Balancer**: 1 instancia - GRATIS
- **Outbound Transfer**: 10TB/mes - GRATIS

**Total Mensual: $0** 🎉

Quedan disponibles: **1 OCPU y 6GB RAM** para otros usos.

## 📊 Servicios Desplegados

### Desarrollo
| Servicio | Réplicas | Recursos | Puerto |
|----------|----------|----------|--------|
| Backend | 1 | 512Mi-2Gi, 250m-1000m | 443 |
| Frontend | 1 | 128Mi-512Mi, 100m-500m | 80 |
| MySQL | 1 | 256Mi-1Gi, 250m-1000m | 3306 |
| Redis | 1 | 128Mi-512Mi, 100m-500m | 6379 |
| MinIO | 1 | 256Mi-1Gi, 250m-1000m | 9000, 9001 |

### Producción
| Servicio | Réplicas | Recursos | Tipo |
|----------|----------|----------|------|
| Backend | 2 | 1Gi-4Gi, 500m-2000m | ClusterIP |
| Frontend | 2 | 256Mi-1Gi, 200m-1000m | LoadBalancer |
| Redis | 1 (StatefulSet) | Variable | Headless |
| MySQL | 1 (Compute) | 6GB RAM, 1 OCPU | Externo |
| Object Storage | - | S3-compatible | Externo |

## 🔧 Comandos Útiles

### Ver Estado
```bash
# Desarrollo
kubectl get all -n dev
kubectl get pods -n dev

# Producción
kubectl get all -n prod
kubectl get pods -n prod
```

### Ver Logs
```bash
# Backend
kubectl logs -f -l app=backend -n dev
kubectl logs -f -l app=backend -n prod

# Frontend
kubectl logs -f -l app=frontend -n dev
kubectl logs -f -l app=frontend -n prod
```

### Acceder a Pods
```bash
# MySQL
kubectl exec -it -n dev <mysql-pod> -- mysql -u root -p

# Redis
kubectl exec -it -n dev <redis-pod> -- redis-cli
kubectl exec -it -n prod redis-0 -- redis-cli

# MinIO
kubectl exec -it -n dev <minio-pod> -- mc alias set local http://localhost:9000 minioadmin minioadmin123
```

### Escalado
```bash
# Escalar backend en producción
kubectl scale deployment backend -n prod --replicas=3

# Escalar frontend en producción
kubectl scale deployment frontend -n prod --replicas=3
```

### Actualización
```bash
# Rolling update del backend
kubectl set image deployment/backend backend=<nueva-imagen> -n prod
kubectl rollout status deployment/backend -n prod

# Rollback si hay problemas
kubectl rollout undo deployment/backend -n prod
```

## 🐛 Solución de Problemas

### Backend no arranca
```bash
# Ver logs
kubectl logs -l app=backend -n prod --tail=100

# Verificar secrets
kubectl get secret spiritblade-secrets -n prod -o yaml

# Verificar conectividad a MySQL
kubectl run -it --rm mysql-test --image=mysql:8.0 -n prod -- \
  mysql -h <MYSQL_IP> -u root -p
```

### Frontend no carga
```bash
# Verificar servicio
kubectl get svc frontend -n prod

# Ver logs
kubectl logs -l app=frontend -n prod

# Describir pod
kubectl describe pod -l app=frontend -n prod
```

### Object Storage no funciona
```bash
# Verificar credenciales
cd iac/terraform
terraform output s3_access_key
terraform output s3_secret_key
terraform output s3_compatible_endpoint

# Actualizar secrets en K8s
kubectl edit secret spiritblade-secrets -n prod
```

## 📚 Documentación Adicional

- **[README Completo](./README.md)**: Guía detallada con todos los pasos
- **[Guía de Terraform](../iac/terraform/README.md)**: Configuración de infraestructura
- **[Guía Rápida](../docs/Despliegue-Kubernetes.md)**: Despliegue en 30 minutos
- **[Resumen de Implementación](./IMPLEMENTACION.md)**: Detalles técnicos

## 🎓 Arquitectura

### Desarrollo
```
┌──────────────────────────────────────┐
│   Kubernetes Local (Minikube/K3s)   │
├──────────────────────────────────────┤
│  Frontend (80) ─────┐                │
│                     │                │
│  Backend (443) ─────┼── MySQL (3306) │
│                     │                │
│  Redis (6379) ──────┤                │
│                     │                │
│  MinIO (9000) ──────┘                │
└──────────────────────────────────────┘
```

### Producción (Oracle Cloud)
```
┌────────────────────────────────────────────────┐
│              Internet                          │
└──────────────────┬─────────────────────────────┘
                   │
           ┌───────▼────────┐
           │  Load Balancer │
           │    (OCI LB)    │
           └───────┬────────┘
                   │
┌──────────────────┼─────────────────────────────┐
│  OKE Cluster     │                             │
├──────────────────┼─────────────────────────────┤
│                  │                             │
│  ┌───────────────▼──────────┐                 │
│  │  Frontend (2 pods)       │                 │
│  └───────────────┬──────────┘                 │
│                  │                             │
│  ┌───────────────▼──────────┐                 │
│  │  Backend (2 pods)        │                 │
│  └───────────────┬──────────┘                 │
│                  │                             │
│  ┌───────────────▼──────────┐                 │
│  │  Redis StatefulSet       │                 │
│  └──────────────────────────┘                 │
└─────────────────┬──────────────────────────────┘
                  │
      ┌───────────┴───────────┐
      │                       │
┌─────▼─────┐        ┌────────▼────────┐
│   MySQL   │        │ Object Storage  │
│ (Compute) │        │  (S3-compat)    │
│ A1.Flex   │        │                 │
└───────────┘        └─────────────────┘
```

## ✅ Checklist de Despliegue

### Desarrollo
- [ ] Docker Desktop / Minikube instalado
- [ ] Imágenes construidas (`deploy.sh` o `deploy.ps1`)
- [ ] Secrets configurados
- [ ] Todos los pods en Running
- [ ] Port-forward funcionando
- [ ] Aplicación accesible

### Producción
- [ ] Cuenta de Oracle Cloud creada
- [ ] OCI CLI configurado
- [ ] Terraform instalado
- [ ] `terraform.tfvars` configurado
- [ ] Infraestructura desplegada (`terraform apply`)
- [ ] kubectl configurado para OKE
- [ ] Imágenes ARM64 construidas y subidas
- [ ] Secrets de producción creados
- [ ] Manifiestos actualizados con IPs
- [ ] Aplicación desplegada en K8s
- [ ] Load Balancer tiene IP pública
- [ ] Aplicación accesible desde internet

## 🔒 Seguridad

### Consideraciones Importantes

1. **Secrets**: Usar Sealed Secrets o External Secrets Operator en producción
2. **Network Policies**: Implementar para segmentar tráfico
3. **RBAC**: Configurar roles y permisos adecuados
4. **SSL/TLS**: Certificados válidos (Let's Encrypt con cert-manager)
5. **Firewall**: Configurar Security Lists en OCI apropiadamente
6. **Backup**: Automatizar backups de MySQL y Redis
7. **Monitoring**: Implementar Prometheus + Grafana

## 📞 Soporte

Para problemas o preguntas:
1. Revisar la [documentación completa](./README.md)
2. Verificar los [logs](#ver-logs)
3. Consultar la [guía de solución de problemas](#-solución-de-problemas)

## 📝 Notas Finales

Este despliegue está optimizado para:
- ✅ **Costo cero** usando Oracle Cloud Free Tier
- ✅ **Alta disponibilidad** con réplicas múltiples
- ✅ **Escalabilidad** horizontal automática
- ✅ **Mantenibilidad** con IaC (Terraform)
- ✅ **Portabilidad** (puede migrarse a otras nubes)

---

**Fecha**: Enero 2026  
**Estado**: ✅ Production Ready  
**Mantenedor**: Equipo Spiritblade
