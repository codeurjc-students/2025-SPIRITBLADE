# Load .env file and run Spring Boot application
# This script loads environment variables from ../.env and runs mvn spring-boot:run

$envFile = Join-Path $PSScriptRoot "../.env"

if (-not (Test-Path $envFile)) {
    Write-Host "ERROR: .env file not found at $envFile" -ForegroundColor Red
    exit 1
}

Write-Host "Loading environment variables from .env..." -ForegroundColor Green

# Read .env and set environment variables
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    # Skip empty lines and comments
    if ($line -and -not $line.StartsWith('#')) {
        # Split on first = only
        $parts = $line -split '=', 2
        if ($parts.Length -eq 2) {
            $key = $parts[0].Trim()
            $value = $parts[1].Trim()
            # Set environment variable for this process
            [Environment]::SetEnvironmentVariable($key, $value, [EnvironmentVariableTarget]::Process)
            Write-Host "  $key = $value" -ForegroundColor Gray
        }
    }
}

Write-Host "`nStarting Spring Boot application..." -ForegroundColor Green
mvn spring-boot:run
