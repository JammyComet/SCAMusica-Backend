# Detiene y elimina los contenedores, pero CONSERVA los datos MySQL.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
docker compose down
if ($LASTEXITCODE -ne 0) { throw 'No se pudo detener la aplicacion.' }
Write-Host 'SCAMusica detenida. Las bases de datos siguen guardadas en el volumen Docker.'
