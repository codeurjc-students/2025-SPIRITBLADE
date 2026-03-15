param(
    [switch]$MonitorOnly
)

# ==============================================================================
# SPIRITBLADE - Monitor de Escalado 
# ==============================================================================

if (-Not $MonitorOnly) {
    Write-Host "Iniciando ventana de monitoreo..." -ForegroundColor Cyan
    $runPath = (Get-Location).Path
    
    # Run the window in C:\ to safely avoid missing context directory bugs,
    # because kubectl already has %USERPROFILE%/.kube/config globally.
    Start-Process powershell -ArgumentList "-NoProfile -NoExit -Command `"cd F:\ ; & '$runPath\watch-scaling.ps1' -MonitorOnly`""
    
    Write-Host "`n> Lanzando test de carga (Artillery)...`n" -ForegroundColor Yellow
    cmd.exe /c "npx artillery run artillery-config.yaml"
    exit
}

$interval = 5  
$host.UI.RawUI.WindowTitle = "SPIRITBLADE Monitor"

function Write-Header($text) {
    Write-Host "`n--------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "  $text  $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Cyan
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
}

while ($true) {
    Write-Host "`n=======================================================" -ForegroundColor Magenta
    Write-Host "       NUEVA ACTUALIZACION: $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Magenta
    Write-Host "=======================================================" -ForegroundColor Magenta

    Write-Header "NODOS (Autoscaler: min=2, max=4)"
    kubectl get nodes --no-headers | ForEach-Object {
        $parts = $_ -split '\s+'
        $name = $parts[0]
        $status = if ($parts[1] -eq "Ready") { "[OK] Ready" } else { "[!!] $($parts[1])" }
        Write-Host ("  {0,-35} {1,-10}" -f $name, $status) -ForegroundColor White
    }
    $nodeCount = (kubectl get nodes --no-headers 2>$null | Measure-Object -Line).Lines
    Write-Host "`n  > Total nodos: $nodeCount / 4" -ForegroundColor $(if ($nodeCount -gt 2) { "Green" } else { "Gray" })

    Write-Header "PODS prod (HPA: max=5)"
    kubectl get pods -n prod --no-headers | ForEach-Object {
        $parts = $_ -split '\s+'
        if ($parts.Length -ge 5) {
            $name   = $parts[0]; $ready = $parts[1]; $status = $parts[2]; $age = $parts[4]
            $color  = if ($status -eq "Running") { "Green" } elseif ($status -eq "Pending") { "Yellow" } else { "Red" }
            Write-Host ("  {0,-40} {1,-6} " -f $name, $ready) -NoNewline
            Write-Host ("{0,-15}" -f $status) -ForegroundColor $color -NoNewline
            Write-Host ("age: {0}" -f $age)
        }
    }

    Write-Header "HPA ESTADO"
    kubectl get hpa -n prod --no-headers | ForEach-Object {
        $parts = $_.Trim() -split '\s{2,}'
        if ($parts.Length -ge 6) {
            $name = $parts[0]; $targets = $parts[2]; $max = $parts[4]; $replicas = $parts[5]
            $color = if ([int]$replicas -gt 1) { "Green" } else { "Gray" }
            Write-Host ("  {0,-20} replicas: {1,-5} max: {2,-5} targets: {3}" -f $name, $replicas, $max, $targets) -ForegroundColor $color
        }
    }

    Write-Header "USO ACTUAL CPU/RAM (metrics-server)"
    try {
        kubectl top pods -n prod --no-headers 2>$null | ForEach-Object {
            $parts = $_ -split '\s+'
            if ($parts.Length -ge 3) {
                $name = $parts[0]; $cpu = $parts[1]; $mem = $parts[2]
                $cpuVal = 0; if ($cpu -match '\d+') { $cpuVal = [int]($cpu -replace 'm','') }
                $color = if ($cpuVal -gt 420) { "Red" } elseif ($cpuVal -gt 250) { "Yellow" } else { "Green" }
                Write-Host ("  {0,-40} " -f $name) -NoNewline
                Write-Host ("CPU: {0,-8} " -f $cpu) -ForegroundColor $color -NoNewline
                Write-Host ("RAM: {0}" -f $mem)
            }
        }
    } catch { Write-Host "  (Obteniendo metricas...)" -ForegroundColor DarkGray }

    Write-Header "USO NODO"
    try {
        kubectl top nodes --no-headers 2>$null | ForEach-Object {
            $parts = $_ -split '\s+'
            if ($parts.Length -ge 5) {
                $name = $parts[0]; $cpu = $parts[1]; $cpuPct = $parts[2]; $mem = $parts[3]; $memPct = $parts[4]
                Write-Host ("  {0,-35} CPU: {1,-5} ({2,-4}) RAM: {3,-8} ({4})" -f $name, $cpu, $cpuPct, $mem, $memPct)
            }
        }
    } catch { Write-Host "  (Obteniendo metricas...)" -ForegroundColor DarkGray }

    $pending = kubectl get pods -n prod --no-headers 2>$null | Where-Object { $_ -match "Pending" }
    if ($pending) { Write-Host "`n  [!]  PODS PENDING -> Autoscaler anadiendo nodo..." -ForegroundColor Yellow }

    Write-Host "`n  Proxima actualizacion en $interval segundos...  (Ctrl+C para salir)" -ForegroundColor DarkGray
    Start-Sleep -Seconds $interval
}