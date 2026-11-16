# Pruebas sin alterar productos, perfiles ni contraseñas; el login crea sesiones de prueba.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
function Probar($nombre, [scriptblock]$accion) {
    try { & $accion; Write-Host "OK  $nombre" -ForegroundColor Green }
    catch { Write-Host "ERROR  $nombre : $($_.Exception.Message)" -ForegroundColor Red; $script:fallos++ }
}
function BuscarVariable([string]$nombre) {
    $linea = Get-Content -LiteralPath '.env' | Where-Object { $_ -match "^$nombre=" } | Select-Object -Last 1
    if (-not $linea) { throw "Falta $nombre en .env" }
    return ($linea -split '=',2)[1]
}
function Login([string]$correo,[string]$clave) {
    return Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body (@{ username=$correo; password=$clave } | ConvertTo-Json)
}
$script:fallos = 0
if (-not (Test-Path -LiteralPath '.env')) { throw 'No existe .env. Copia el .env del proyecto Docker anterior.' }
$productos = @(); $categorias = @()
Probar 'La tienda React responde' {
    $web = Invoke-WebRequest -UseBasicParsing 'http://localhost:5173/'
    if ($web.StatusCode -ne 200) { throw 'La tienda no responde con HTTP 200' }
}
Probar 'Swagger documenta JWT Bearer' {
    $documento = Invoke-RestMethod 'http://localhost:8080/v3/api-docs/ms-catalogo'
    if (-not $documento.components.securitySchemes.bearerAuth) { throw 'Falta bearerAuth en Swagger' }
}
Probar 'Catalogo REST responde'  { $script:productos = @(Invoke-RestMethod 'http://localhost:8080/api/v1/productos'); if ($productos.Count -eq 0) { throw 'No hay productos cargados' } }
Probar 'Categorias REST responde' { $script:categorias = @(Invoke-RestMethod 'http://localhost:8080/api/v1/categorias'); if ($categorias.Count -eq 0) { throw 'No hay categorias cargadas' } }
Probar 'Cada producto tiene categoria existente' {
    foreach ($producto in $productos) {
        if (-not @($categorias | Where-Object { $_.id -eq $producto.categoriaId }).Count) { throw "Producto sin categoria: $($producto.codigo)" }
    }
}
$cliente = $null
Probar 'Login cliente y JWT 5 minutos' {
    $script:cliente = Login 'cliente@gmail.com' (BuscarVariable 'DEMO_CLIENTE_PASSWORD')
    if (-not $cliente.token -or $cliente.expiresIn -ne 300) { throw 'JWT o duracion incorrecta' }
}
if ($cliente) {
    Probar 'Perfil del cliente autorizado por JWT' {
        $r = Invoke-RestMethod 'http://localhost:8080/api/v1/usuarios/me' -Headers @{Authorization="Bearer $($cliente.token)"}
        if ($r.correo -ne 'cliente@gmail.com') { throw 'La API devolvio otro usuario' }
    }
    Probar 'Cliente NO puede leer la administracion' {
        try {
            Invoke-WebRequest -UseBasicParsing 'http://localhost:8080/api/v1/admin/usuarios' -Headers @{Authorization="Bearer $($cliente.token)"} | Out-Null
            throw 'ERROR: cliente pudo abrir administracion'
        } catch {
            if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 403) { return }
            throw
        }
    }
}
$vendedor = $null
Probar 'Login vendedor' {
    $script:vendedor = Login 'vendedor@duoc.cl' (BuscarVariable 'DEMO_VENDEDOR_PASSWORD')
    if (-not $vendedor.token) { throw 'No se recibio JWT del vendedor' }
}
if ($vendedor) {
    Probar 'El vendedor puede consultar ordenes' {
        $null = Invoke-RestMethod 'http://localhost:8080/api/v1/ordenes' -Headers @{Authorization = "Bearer $($vendedor.token)"}
    }
    Probar 'El vendedor no puede editar productos' {
        try {
            Invoke-WebRequest -UseBasicParsing 'http://localhost:8080/api/v1/productos/NO-EXISTE' -Method PUT -ContentType 'application/json' -Body '{}' -Headers @{Authorization = "Bearer $($vendedor.token)"} | Out-Null
            throw 'El vendedor pudo editar productos'
        } catch {
            if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 403) { return }
            throw
        }
    }
}
Write-Host "Pruebas finalizadas. Fallos: $fallos" -ForegroundColor $(if ($fallos -eq 0) {'Green'} else {'Yellow'})
Write-Host 'Para probar cambio de password: usa Mi perfil con el cliente, guarda, verifica cierre de sesion y vuelve a iniciar con la nueva clave.'
if ($fallos -ne 0) { exit 1 }
