# SOLO ENTORNO LOCAL DE PRUEBAS. No usar credenciales compartidas en producción.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not (Test-Path -LiteralPath '.env')) { throw 'Falta .env. Ejecuta primero .\iniciar.ps1.' }
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Inicia Docker Desktop.' }

$envPath = Join-Path $PSScriptRoot '.env'
$original = Get-Content -LiteralPath $envPath -Raw
$contenido = $original
foreach ($nombre in @('DEMO_ADMIN_PASSWORD','DEMO_VENDEDOR_PASSWORD','DEMO_CLIENTE_PASSWORD')) {
    if ($contenido -match "(?m)^$nombre=") {
        $contenido = [regex]::Replace($contenido, "(?m)^$nombre=.*$", "$nombre=scamusica123")
    } else {
        $contenido += "`n$nombre=scamusica123"
    }
}
Write-Host 'Esto restablecera SOLO las 3 cuentas de demostracion (no borra productos, compras ni usuarios nuevos).' -ForegroundColor Yellow
$confirmacion = Read-Host 'Escribe SI para confirmar'
if ($confirmacion -cne 'SI') { Write-Host 'Cancelado sin cambios.'; exit 0 }
Set-Content -LiteralPath $envPath -Value $contenido -Encoding ASCII
try {
    # El reinicio temporal lee las contrasenas nuevas y actualiza el hash BCrypt en MySQL.
    $env:RESET_DEMO_PASSWORDS = 'true'
    docker compose up -d --no-deps --force-recreate ms-auth
    if ($LASTEXITCODE -ne 0) { throw 'Docker no pudo ejecutar el restablecimiento.' }
    $salud = 'starting'
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 3
        $containerId = docker compose ps -q ms-auth
        if (-not $containerId) { continue }
        $salud = docker inspect --format '{{.State.Health.Status}}' $containerId 2>$null
        if ($salud -eq 'healthy') { break }
        if ($salud -eq 'unhealthy') { break }
    }
    if ($salud -ne 'healthy') { throw 'ms-auth no termino correctamente. Usa docker compose logs --tail=100 ms-auth.' }
    Write-Host 'Contrasenas de demostracion actualizadas en MySQL.' -ForegroundColor Green
}
finally {
    # Evita volver a sobrescribir una contraseña cambiada por el usuario en el siguiente reinicio.
    $env:RESET_DEMO_PASSWORDS = 'false'
    docker compose up -d --no-deps --force-recreate ms-auth
    Remove-Item Env:RESET_DEMO_PASSWORDS -ErrorAction SilentlyContinue
}
Write-Host 'Listo: admin@duoc.cl / vendedor@duoc.cl / cliente@gmail.com -> scamusica123' -ForegroundColor Green
Write-Host 'Vuelve a iniciar sesion: se invalidaron las sesiones de demostracion anteriores.'
