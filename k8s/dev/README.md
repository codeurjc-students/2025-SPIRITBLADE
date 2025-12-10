# Local Kubernetes Development

This directory contains Kubernetes manifests for deploying the SPIRITBLADE application locally (e.g., using Minikube or Docker Desktop).

## Prerequisites
- Kubernetes cluster (Minikube, Docker Desktop, etc.)
- `kubectl` configured
- Docker

## Build Images
Before deploying, you need to build the local images.

```bash
# Build Backend
docker build -t spiritblade-backend:latest -f docker/Dockerfile.backend .

# Build Frontend
docker build -t spiritblade-frontend:latest -f docker/Dockerfile.frontend .
```

## Deploy
Apply the manifests in the following order:

1. **Secrets:**
   ```bash
   kubectl apply -f k8s/dev/secrets.yaml
   ```

2. **Databases & Storage:**
   ```bash
   kubectl apply -f k8s/dev/mysql-deployment.yaml
   kubectl apply -f k8s/dev/redis-deployment.yaml
   kubectl apply -f k8s/dev/minio-deployment.yaml
   ```

3. **Application:**
   ```bash
   kubectl apply -f k8s/dev/backend-deployment.yaml
   kubectl apply -f k8s/dev/frontend-deployment.yaml
   ```

## Access
- **Frontend:** http://localhost:80 (or load balancer IP)
- **MinIO Console:** http://localhost:9001
- **Backend API:** https://localhost:443 (internal)

## Cleanup
```bash
kubectl delete -f k8s/dev/
```
