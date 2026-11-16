# Ejecutar en PowerShell desde la carpeta descomprimida: .\iniciar.ps1
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'No se encontro Docker. Instala y abre Docker Desktop antes de ejecutar este script.'
}
docker compose version | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Docker Compose no responde. Inicia Docker Desktop.' }

function New-Secret([int]$bytes) {
    $buffer = New-Object byte[] $bytes
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($buffer) } finally { $rng.Dispose() }
    return [System.BitConverter]::ToString($buffer).Replace('-', '').ToLowerInvariant()
}

$envFile = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    $vars = @{
        DB_USER = 'scamusica'
        DB_PASSWORD = (New-Secret 24)
        MYSQL_ROOT_PASSWORD = (New-Secret 24)
        JWT_SECRET = (New-Secret 32)
        INTERNAL_KEY = (New-Secret 32)
        # Contraseña fácil solo para las cuentas de demostración locales.
        DEMO_ADMIN_PASSWORD = 'scamusica123'
        DEMO_VENDEDOR_PASSWORD = 'scamusica123'
        DEMO_CLIENTE_PASSWORD = 'scamusica123'
    }
    @(
      'DB_USER=' + $vars.DB_USER
      'DB_PASSWORD=' + $vars.DB_PASSWORD
      'MYSQL_ROOT_PASSWORD=' + $vars.MYSQL_ROOT_PASSWORD
      'JWT_SECRET=' + $vars.JWT_SECRET
      'INTERNAL_KEY=' + $vars.INTERNAL_KEY
      'DEMO_ADMIN_PASSWORD=' + $vars.DEMO_ADMIN_PASSWORD
      'DEMO_VENDEDOR_PASSWORD=' + $vars.DEMO_VENDEDOR_PASSWORD
      'DEMO_CLIENTE_PASSWORD=' + $vars.DEMO_CLIENTE_PASSWORD
    ) | Set-Content -LiteralPath $envFile -Encoding ASCII
    Write-Host 'Claves de infraestructura aleatorias creadas en .env (no compartas ese archivo).' -ForegroundColor Green
    Write-Host 'Cuentas demo (para entrar a la tienda):'
    Write-Host ('  admin@duoc.cl -> ' + $vars.DEMO_ADMIN_PASSWORD)
    Write-Host ('  vendedor@duoc.cl -> ' + $vars.DEMO_VENDEDOR_PASSWORD)
    Write-Host ('  cliente@gmail.com -> ' + $vars.DEMO_CLIENTE_PASSWORD)
} else {
    Write-Host '.env ya existe: se conserva sin modificar contrasenas ni claves.'
    Write-Host 'Las contrasenas demo existentes se conservan. Para cambiarlas a scamusica123 ejecuta .\actualizar-claves-demo.ps1.'
}

Write-Host 'Construyendo e iniciando SCAMusica...' -ForegroundColor Cyan
docker compose up --build -d
if ($LASTEXITCODE -ne 0) { throw 'Docker Compose no pudo iniciar SCAMusica. Revisa docker compose logs.' }
docker compose ps
Write-Host ''
Write-Host 'Tienda:  http://localhost:5173'
Write-Host 'Swagger: http://localhost:8080/swagger-ui.html'
Write-Host 'Eureka:  http://localhost:8761'
Write-Host 'MySQL:   localhost:3307 (solo desde este PC)'
Write-Host 'Si algun servicio no esta healthy, revisa: docker compose logs -f --tail=100'
