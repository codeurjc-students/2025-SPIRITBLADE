# retry-terraform.ps1
$maxAttempts = 10000000 
$attempt = 1

while ($attempt -le $maxAttempts) {
    Write-Host "Intento $attempt de $maxAttempts - $(Get-Date)" -ForegroundColor Yellow
    
    terraform apply -auto-approve
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "¡Éxito! Recursos creados" -ForegroundColor Green
        break
    }
    
    Write-Host "Falló. Reintentando en 5 minutos..." -ForegroundColor Red
    Start-Sleep -Seconds 5  # 5 minutos
    $attempt++
}