#!/bin/bash
# Script para limpiar el entorno de desarrollo

set -e

echo "=================================="
echo "SPIRITBLADE - Limpieza Dev"
echo "=================================="
echo ""

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}➜ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info "Eliminando namespace dev..."
kubectl delete namespace dev --ignore-not-found=true

print_info "Esperando a que se eliminen todos los recursos..."
sleep 5

print_success "Entorno de desarrollo eliminado"
echo ""
echo "Para volver a desplegar, ejecuta:"
echo "  ./deploy.sh"
echo ""
