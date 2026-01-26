#!/bin/bash
# Script de Despliegue Rápido - Spiritblade Dev Environment

set -e

echo "=================================="
echo "SPIRITBLADE - Despliegue Dev"
echo "=================================="
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir en verde
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Función para imprimir en amarillo
print_info() {
    echo -e "${YELLOW}➜ $1${NC}"
}

# Función para imprimir en rojo
print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Verificar que kubectl está instalado
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl no está instalado"
    exit 1
fi

# Verificar que docker está instalado
if ! command -v docker &> /dev/null; then
    print_error "docker no está instalado"
    exit 1
fi

print_info "Verificando cluster de Kubernetes..."
if ! kubectl cluster-info &> /dev/null; then
    print_error "No hay conexión con el cluster de Kubernetes"
    print_info "Asegúrate de tener Minikube, Docker Desktop o Kind ejecutándose"
    exit 1
fi
print_success "Cluster de Kubernetes activo"

# Construir imágenes
print_info "Construyendo imágenes Docker..."

print_info "Construyendo Backend..."
cd "$(dirname "$0")/../backend"
./mvnw clean package -DskipTests
docker build -t spiritblade-backend:dev .
print_success "Backend construido"

print_info "Construyendo Frontend..."
cd ../frontend
npm install
npm run build
docker build -t spiritblade-frontend:dev .
print_success "Frontend construido"

# Desplegar en Kubernetes
print_info "Desplegando en Kubernetes..."
cd ../k8s/dev

print_info "Creando namespace..."
kubectl apply -f namespace.yaml
print_success "Namespace creado"

print_info "Aplicando secrets..."
kubectl apply -f secrets.yaml
print_success "Secrets aplicados"

print_info "Desplegando MySQL..."
kubectl apply -f mysql-deployment.yaml
print_success "MySQL desplegado"

print_info "Desplegando Redis..."
kubectl apply -f redis-deployment.yaml
print_success "Redis desplegado"

print_info "Desplegando MinIO..."
kubectl apply -f minio-deployment.yaml
print_success "MinIO desplegado"

print_info "Esperando a que las bases de datos estén listas (puede tardar 2-3 minutos)..."
kubectl wait --for=condition=ready pod -l app=mysql -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=minio -n dev --timeout=300s
print_success "Bases de datos listas"

print_info "Desplegando Backend..."
kubectl apply -f backend-deployment.yaml
print_success "Backend desplegado"

print_info "Desplegando Frontend..."
kubectl apply -f frontend-deployment.yaml
print_success "Frontend desplegado"

print_info "Esperando a que la aplicación esté lista..."
kubectl wait --for=condition=ready pod -l app=backend -n dev --timeout=300s
kubectl wait --for=condition=ready pod -l app=frontend -n dev --timeout=300s
print_success "Aplicación lista"

echo ""
echo "=================================="
echo "✓ DESPLIEGUE COMPLETADO"
echo "=================================="
echo ""
echo "Para acceder a la aplicación:"
echo ""
echo "1. Frontend:"
echo "   kubectl port-forward -n dev svc/frontend 8080:80"
echo "   Luego abre: http://localhost:8080"
echo ""
echo "2. Backend API:"
echo "   kubectl port-forward -n dev svc/backend 8443:443"
echo "   Luego abre: https://localhost:8443"
echo ""
echo "3. MinIO Console (opcional):"
echo "   kubectl port-forward -n dev svc/minio 9001:9001"
echo "   Luego abre: http://localhost:9001"
echo ""
echo "Para ver los logs:"
echo "   kubectl logs -f -l app=backend -n dev"
echo "   kubectl logs -f -l app=frontend -n dev"
echo ""
echo "Para ver todos los pods:"
echo "   kubectl get pods -n dev"
echo ""
