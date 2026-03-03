# ==============================================================================
# SPIRITBLADE - Monitor de Escalado en Tiempo Real
# Ejecutar ANTES de lanzar el test de Artillery para ver el escalado en vivo.
#
# Uso: .\watch-scaling.ps1
# Requiere: kubectl configurado + acceso al cluster OKE
# ==============================================================================

$interval = 5  # segundos entre cada refresco

function Write-Header($text) {
    Write-Host "`n══════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "  $text  $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "══════════════════════════════════════════════" -ForegroundColor Cyan
}

Write-Host "Iniciando monitor de escalado... (Ctrl+C para detener)" -ForegroundColor Yellow
Write-Host "Abre una segunda terminal y lanza: npx artillery run artillery-config.yaml`n" -ForegroundColor Yellow

while ($true) {
    Clear-Host

    # ---- NODOS K8s (Cluster Autoscaler) --------------------------------
    Write-Header "NODOS DEL CLUSTER (Cluster Autoscaler: min=2, max=4)"
    kubectl get nodes -o wide --no-headers | ForEach-Object {
        $parts = $_ -split '\s+'
        $status = if ($parts[1] -eq "Ready") { "✅ Ready" } else { "❌ $($parts[1])" }
        Write-Host "  $($parts[0])  $status  CPU:$($parts[3])  RAM:$($parts[4])" -ForegroundColor White
    }
    $nodeCount = (kubectl get nodes --no-headers 2>$null | Measure-Object -Line).Lines
    Write-Host "  → Total nodos: $nodeCount / 4" -ForegroundColor $(if ($nodeCount -gt 2) { "Green" } else { "Gray" })

    # ---- PODS en producción (HPA) --------------------------------------
    Write-Header "PODS namespace:prod (HPA backend: min=1, max=3)"
    kubectl get pods -n prod --no-headers | ForEach-Object {
        $parts = $_ -split '\s+'
        $ready  = $parts[1]
        $status = $parts[2]
        $color  = if ($status -eq "Running") { "Green" } elseif ($status -eq "Pending") { "Yellow" } else { "Red" }
        Write-Host "  $($parts[0])  $ready  " -NoNewline
        Write-Host "$status" -ForegroundColor $color -NoNewline
        Write-Host "  age:$($parts[4])"
    }

    # ---- HPA estado ----------------------------------------------------
    Write-Header "HORIZONTAL POD AUTOSCALER"
    kubectl get hpa -n prod --no-headers | ForEach-Object {
        $parts = $_ -split '\s+'
        # NAME  REFERENCE  TARGETS  MINPODS  MAXPODS  REPLICAS  AGE
        Write-Host ("  {0,-20} replicas:{1}/{2}  cpu:{3}" -f $parts[0], $parts[6], $parts[5], $parts[2]) `
            -ForegroundColor $(if ([int]$parts[6] -gt 1) { "Green" } else { "Gray" })
    }

    # ---- Uso de CPU/RAM por pod ----------------------------------------
    Write-Header "USO ACTUAL CPU/RAM (metrics-server)"
    try {
        kubectl top pods -n prod --no-headers 2>$null | ForEach-Object {
            $parts = $_ -split '\s+'
            $cpu = $parts[1]
            $mem = $parts[2]
            $cpuVal = [int]($cpu -replace 'm','')
            $color = if ($cpuVal -gt 420) { "Red" } elseif ($cpuVal -gt 250) { "Yellow" } else { "Green" }
            Write-Host "  $($parts[0])  " -NoNewline
            Write-Host "CPU:$cpu" -ForegroundColor $color -NoNewline
            Write-Host "  RAM:$mem"
        }
    } catch {
        Write-Host "  (metrics-server no disponible)" -ForegroundColor DarkGray
    }

    # ---- Nodos CPU/RAM -------------------------------------------------
    Write-Header "USO ACTUAL CPU/RAM POR NODO"
    try {
        kubectl top nodes --no-headers 2>$null | ForEach-Object {
            $parts = $_ -split '\s+'
            Write-Host "  $($parts[0])  CPU:$($parts[1]) ($($parts[2]))  RAM:$($parts[3]) ($($parts[4]))"
        }
    } catch {
        Write-Host "  (metrics-server no disponible)" -ForegroundColor DarkGray
    }

    # ---- Pods Pending (indica que el Autoscaler debería actuar) --------
    $pending = kubectl get pods -n prod --no-headers 2>$null | Where-Object { $_ -match "Pending" }
    if ($pending) {
        Write-Host "`n  ⚠️  PODS PENDING → Cluster Autoscaler añadiendo nodo..." -ForegroundColor Yellow
    }

    Write-Host "`n  Próxima actualización en $interval segundos...  (Ctrl+C para salir)" -ForegroundColor DarkGray
    Start-Sleep -Seconds $interval
}
