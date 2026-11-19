# SCAMusica — Backend

Backend de la tienda SCAMusica desarrollado en Java 21, Spring Boot 3.4.5 y Maven multimódulo.

## Estructura

- `musica-parent/`: 6 microservicios de negocio, API Gateway y Eureka Server.
- `compose.yaml`: orquestación de los servicios, MySQL y frontend.
- `docker/mysql/`: inicialización de permisos de MySQL.
- `musica-parent/docs/bd-general.sql`: definición de bases de datos.
- `musica-parent/docs/endpoints.md`: rutas REST y permisos.

## Ejecución con Docker

Requisito: Docker Desktop en ejecución, con Docker Compose disponible.

El frontend es un repositorio independiente y debe estar clonado en `Tienda-SCAMusic-main/`, directamente dentro de esta carpeta, porque `compose.yaml` usa esa ruta como contexto de compilación.

En PowerShell, desde la raíz de este repositorio:

```powershell
.\iniciar.ps1
docker compose ps
```

En la primera ejecución, `iniciar.ps1` crea un `.env` local con secretos de infraestructura. En ejecuciones posteriores conserva el archivo existente. No publiques `.env` ni las credenciales que contiene. `.env.example` solo documenta las variables requeridas.

Accesos locales:

- Tienda: http://localhost:5173
- API Gateway y Swagger: http://localhost:8080/swagger-ui.html
- Eureka: http://localhost:8761
- MySQL (acceso local): localhost:3307

## Pruebas y mantenimiento

```powershell
.\verificar-scamusica.ps1
.\detener.ps1
```

La imagen Docker del backend ejecuta `mvn verify` para compilar **y correr JUnit**: si falla una prueba, Docker detiene el build. Las compras usan una clave idempotente y conciliación de pedidos pendientes para recuperar cortes entre el stock y la orden.

`actualizar-claves-demo.ps1` permite restablecer las contraseñas de las cuentas de demostración **solo en el entorno local**. El JWT de acceso caduca a los 5 minutos y la sesión utiliza renovación mediante cookie HttpOnly.

MySQL y las portadas subidas desde el administrador utilizan volúmenes persistentes independientes (`mysql_data` y `catalogo_images`). No ejecutes `docker compose down -v` si necesitas conservar los datos. Si reutilizas una base anterior, conserva su `.env` original.

## Servicios de negocio

Se mantienen seis servicios: `ms-auth` (usuarios y contacto), `ms-catalogo` (productos e inventario), `ms-categorias`, `ms-carrito`, `ms-pedido` (órdenes y boletas) y `ms-reportes`. Las rutas públicas de React se conservaron.

Si antes ejecutabas la versión con 10 servicios, usa `docker compose up -d --build --remove-orphans`: los cuatro contenedores retirados se eliminan sin borrar los volúmenes ni las compras. Conserva el `.env` anterior.
