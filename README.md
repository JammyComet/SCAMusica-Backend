# SCAMusica — Backend y Docker

Backend desarrollado con **Java 21, Spring Boot y MySQL**. La ejecución completa del proyecto se realiza mediante Docker Compose.

## Requisitos

- Docker Desktop iniciado (con Docker Compose).
- PowerShell en Windows.
- La carpeta `Tienda-SCAMusic-main` debe estar junto a `musica-parent`, dentro de `SCAMusica-Backend`.

## Levantar el proyecto

Abre PowerShell en la carpeta **`SCAMusica-Backend`** y ejecuta:

```powershell
.\iniciar.ps1
```

Este script crea el archivo `.env` si no existe, construye las imágenes y levanta frontend, backend y MySQL. En los siguientes arranques conserva el `.env` existente.

También puedes ejecutar manualmente:

```powershell
docker compose up -d --build --remove-orphans
```

**Importante:** el comando manual necesita que el `.env` ya esté configurado; la primera vez utiliza `iniciar.ps1`.

## Direcciones locales

| Servicio | Dirección |
|---|---|
| Tienda React | http://localhost:5173 |
| Swagger / API Gateway | http://localhost:8080/swagger-ui.html |
| Eureka Server | http://localhost:8761 |
| MySQL (desde Windows) | `localhost:3307` |

Las **cuentas de prueba** y los **comandos de pruebas del frontend** están en `Tienda-SCAMusic-main/README.md`.

## Comandos útiles

Desde la carpeta raíz:

```powershell
docker compose ps                  # Ver estado de los contenedores
docker compose logs --tail=100     # Revisar errores
docker compose restart             # Reiniciar servicios
.\detener.ps1                      # Detener conservando los datos
```

Si necesitas restablecer las claves demo locales a `scamusica123`:

```powershell
.\actualizar-claves-demo.ps1
```

Este último comando modifica las contraseñas demo y cierra sus sesiones anteriores.

## Pruebas del backend

Desde `musica-parent`, con **Java 21 y Maven** instalados:

```powershell
mvn verify
```

También se ejecutan las pruebas JUnit durante la construcción Docker del backend (`mvn verify` en su Dockerfile).

## Organización básica

- `musica-parent/`: `ms-auth`, `ms-catalogo`, `ms-categorias`, `ms-carrito`, `ms-pedido` y `ms-reportes`, además de API Gateway y Eureka.
- `compose.yaml`: configuración de los contenedores.
- `musica-parent/Dockerfile`: construcción del backend.
- `Tienda-SCAMusic-main/Dockerfile`: construcción del frontend.

**No compartas `.env`**: contiene claves privadas. Los datos de MySQL y las imágenes se conservan en volúmenes Docker. **No uses `docker compose down -v`** si deseas conservarlos.
