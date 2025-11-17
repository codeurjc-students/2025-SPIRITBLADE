# Script to run Maven tests with environment variables loaded from .env file
# Usage: .\run-tests.ps1

$envFile = Join-Path $PSScriptRoot "..\.env"

if (-Not (Test-Path $envFile)) {
    Write-Host "Error: .env file not found at $envFile" -ForegroundColor Red
    Write-Host "Please create a .env file based on .env.example" -ForegroundColor Yellow
    exit 1
}

Write-Host "Loading environment variables from .env file..." -ForegroundColor Cyan

# Read and set environment variables
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    
    # Skip empty lines and comments
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    
    # Parse key=value
    if ($line -match "^([^=]+)=(.*)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        
        # Remove quotes if present
        $value = $value -replace '^["'']|["'']$', ''
        
        # Set environment variable for this process
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
        Write-Host "  Set $key" -ForegroundColor Gray
    }
}

Write-Host "`nEnvironment variables loaded successfully!" -ForegroundColor Green
Write-Host "Running Maven tests...`n" -ForegroundColor Cyan

# Run Maven tests
mvn test

Write-Host "`nTests execution completed." -ForegroundColor Cyan
