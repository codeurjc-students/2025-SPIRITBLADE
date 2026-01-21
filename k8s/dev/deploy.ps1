# Script de Despliegue Rápido - Spiritblade Dev Environment (PowerShell)

$ErrorActionPreference = "Stop"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "SPIRITBLADE - Despliegue Dev" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

function Print-Success {
    param($Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Print-Info {
    param($Message)
    Write-Host "➜ $Message" -ForegroundColor Yellow
}

function Print-Error {
    param($Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

# Verificar kubectl
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Print-Error "kubectl no está instalado"
    exit 1
}

# Verificar docker
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Print-Error "docker no está instalado"
    exit 1
}

Print-Info "Verificando cluster de Kubernetes..."
try {
    kubectl cluster-info | Out-Null
    Print-Success "Cluster de Kubernetes activo"
} catch {
    Print-Error "No hay conexión con el cluster de Kubernetes"
    Print-Info "Asegúrate de tener Docker Desktop ejecutándose"
    exit 1
}

# Construir imágenes
Print-Info "Construyendo imágenes Docker..."

Print-Info "Construyendo Backend..."
Push-Location "$PSScriptRoot\..\..\backend"
./mvnw.cmd clean package -DskipTests
docker build -t spiritblade-backend:dev .
Print-Success "Backend construido"

Print-Info "Construyendo Frontend..."
Pop-Location
Push-Location "$PSScriptRoot\..\..\frontend"
npm install
npm run build
docker build -t spiritblade-frontend:dev .
Print-Success "Frontend construido"

# Desplegar en Kubernetes
Print-Info "Desplegando en Kubernetes..."
Pop-Location
Push-Location "$PSScriptRoot"

Print-Info "Creando namespace..."
kubectl apply -f namespace.yaml
Print-Success "Namespace creado"

Print-Info "Aplicando secrets..."
kubectl apply -f secrets.yaml
Print-Success "Secrets aplicados"

Print-Info "Desplegando MySQL..."
kubectl apply -f mysql-deployment.yaml
Print-Success "MySQL desplegado"

Print-Info "Desplegando Redis..."
kubectl apply -f redis-deployment.yaml
Print-Success "Redis desplegado"

Print-Info "Desplegando MinIO..."
kubectl apply -f minio-deployment.yaml
Print-Success "MinIO desplegado"

Print-Info "Esperando a que las bases de datos estén listas (puede tardar 2-3 minutos)..."
kubectl wait --for=condition=ready pod -l app=mysql -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=minio -n dev --timeout=300s
Print-Success "Bases de datos listas"

Print-Info "Desplegando Backend..."
kubectl apply -f backend-deployment.yaml
Print-Success "Backend desplegado"

Print-Info "Desplegando Frontend..."
kubectl apply -f frontend-deployment.yaml
Print-Success "Frontend desplegado"

Print-Info "Esperando a que la aplicación esté lista..."
kubectl wait --for=condition=ready pod -l app=backend -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=frontend -n dev --timeout=300s
Print-Success "Aplicación lista"

Pop-Location

Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "✓ DESPLIEGUE COMPLETADO" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""
Write-Host "Para acceder a la aplicación:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Frontend:" -ForegroundColor Yellow
Write-Host "   kubectl port-forward -n dev svc/frontend 8080:80"
Write-Host "   Luego abre: http://localhost:8080"
Write-Host ""
Write-Host "2. Backend API:" -ForegroundColor Yellow
Write-Host "   kubectl port-forward -n dev svc/backend 8443:443"
Write-Host "   Luego abre: https://localhost:8443"
Write-Host ""
Write-Host "3. MinIO Console (opcional):" -ForegroundColor Yellow
Write-Host "   kubectl port-forward -n dev svc/minio 9001:9001"
Write-Host "   Luego abre: http://localhost:9001"
Write-Host ""
Write-Host "Para ver los logs:" -ForegroundColor Cyan
Write-Host "   kubectl logs -f -l app=backend -n dev"
Write-Host "   kubectl logs -f -l app=frontend -n dev"
Write-Host ""
Write-Host "Para ver todos los pods:" -ForegroundColor Cyan
Write-Host "   kubectl get pods -n dev"
Write-Host ""
