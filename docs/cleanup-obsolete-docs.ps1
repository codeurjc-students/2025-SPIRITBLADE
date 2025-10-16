# Script para eliminar documentación obsoleta - SPIRITBLADE
# Ejecutar desde la raíz del proyecto: .\docs\cleanup-obsolete-docs.ps1

Write-Host "🗑️  Limpieza de Documentación Obsoleta - SPIRITBLADE" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""

$docsPath = "d:\tfg\2025-SPIRITBLADE\docs"

# Lista de archivos a eliminar
$obsoleteFiles = @(
    "CI-CD-IMPLEMENTATION.md",
    "EXECUTIVE-SUMMARY.md",
    "FINAL-VERIFICATION.md",
    "Project-Status.md",
    "QUICK-START-CICD.md",
    "README.md",
    "RELEASE-PROCESS.md",
    "SETUP-CHECKLIST.md",
    "STORAGE_IMPLEMENTATION_SUMMARY.md.bak",
    "WORKFLOWS-VERIFICATION.md"
)

Write-Host "📋 Archivos a eliminar:" -ForegroundColor Yellow
foreach ($file in $obsoleteFiles) {
    Write-Host "   - $file" -ForegroundColor Gray
}
Write-Host ""

# Confirmar eliminación
$confirm = Read-Host "¿Deseas continuar con la eliminación? (S/N)"

if ($confirm -ne "S" -and $confirm -ne "s") {
    Write-Host "❌ Operación cancelada" -ForegroundColor Red
    exit
}

Write-Host ""
Write-Host "🔄 Eliminando archivos..." -ForegroundColor Green

$eliminatedCount = 0
$errorCount = 0

foreach ($file in $obsoleteFiles) {
    $fullPath = Join-Path $docsPath $file
    
    if (Test-Path $fullPath) {
        try {
            Remove-Item $fullPath -Force
            Write-Host "   ✅ Eliminado: $file" -ForegroundColor Green
            $eliminatedCount++
        }
        catch {
            Write-Host "   ❌ Error al eliminar: $file - $_" -ForegroundColor Red
            $errorCount++
        }
    }
    else {
        Write-Host "   ⚠️  No encontrado: $file (ya eliminado?)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "✅ Limpieza completada" -ForegroundColor Green
Write-Host "   Archivos eliminados: $eliminatedCount" -ForegroundColor Green
Write-Host "   Errores: $errorCount" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Green" })
Write-Host ""
Write-Host "📁 Estructura final de docs/:" -ForegroundColor Cyan
Get-ChildItem $docsPath | Select-Object Name | Format-Table -AutoSize

Write-Host ""
Write-Host "✨ La documentación ha sido reorganizada exitosamente" -ForegroundColor Green
Write-Host "📖 Consulta docs/REORGANIZACION-DOCS.md para más información" -ForegroundColor Cyan
