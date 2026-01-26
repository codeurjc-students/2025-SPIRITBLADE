# Script para limpiar el entorno de desarrollo (PowerShell)

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "SPIRITBLADE - Limpieza Dev" -ForegroundColor Cyan
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

Print-Info "Eliminando namespace dev..."
kubectl delete namespace dev --ignore-not-found=true

Print-Info "Esperando a que se eliminen todos los recursos..."
Start-Sleep -Seconds 5

Print-Success "Entorno de desarrollo eliminado"
Write-Host ""
Write-Host "Para volver a desplegar, ejecuta:" -ForegroundColor Cyan
Write-Host "  .\deploy.ps1"
Write-Host ""
