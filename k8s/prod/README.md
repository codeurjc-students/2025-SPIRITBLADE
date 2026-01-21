# Production Deployment on OCI (Oracle Cloud)

This directory contains the Kubernetes manifests for deploying SPIRITBLADE to the Oracle Container Engine for Kubernetes (OKE).

> [!IMPORTANT]
> **ARM Architecture:** Since we are using free **Ampere A1** instances, the underlying architecture is `linux/arm64` (aarch64). You **MUST** build your Docker images for this architecture, otherwise your pods will crash with `Exec format error`.

## 1. Build & Push Images (Multi-Arch)

You need to use `docker buildx` to build for ARM64.

```bash
# Enable buildx
docker buildx create --use

# Build and Push Backend
docker buildx build --platform linux/amd64,linux/arm64 -t <YOUR_REPO>/spiritblade-backend:prod-arm64 -f docker/Dockerfile.backend . --push

# Build and Push Frontend
docker buildx build --platform linux/amd64,linux/arm64 -t <YOUR_REPO>/spiritblade-frontend:prod-arm64 -f docker/Dockerfile.frontend . --push
```

## 2. Deploy to OKE

Ensure you have your `kubectl` configured for your OKE cluster (see `iac/terraform/README.md`).

1.  **Secrets:**
    You must create the secrets manually or using a sealed secret workflow.
    ```bash
    kubectl create secret generic spiritblade-secrets \
      --from-literal=mysql-root-password=<YOUR_DB_PASS> \
      --from-literal=oci-storage-access-key=<YOUR_OCI_KEY> \
      --from-literal=oci-storage-secret-key=<YOUR_OCI_SECRET> \
      --from-literal=jwt-secret=<YOUR_JWT_SECRET> \
      --from-literal=riot-api-key=<YOUR_KEY> \
      --from-literal=google-ai-api-key=<YOUR_KEY> \
      --from-literal=ssl-password=<YOUR_PASS>
    ```

2.  **Redis:**
    ```bash
    kubectl apply -f k8s/prod/redis-statefulset.yaml
    ```

3.  **Application:**
    Update the `image` fields in the manifests to match your repository.
    Update `SPRING_DATASOURCE_URL` in `backend-deployment.yaml` with your MySQL Compute instance's **Private IP**.
    
    ```bash
    kubectl apply -f k8s/prod/backend-deployment.yaml
    kubectl apply -f k8s/prod/frontend-deployment.yaml
    ```

## 3. Verify

Check the Load Balancer IP created for the frontend:
```bash
kubectl get svc
```
Access that IP in your browser.
