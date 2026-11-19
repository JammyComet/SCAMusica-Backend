# Endpoints utilizados por SCAMusica (React manda)

API Gateway: `http://localhost:8080` · Swagger: `http://localhost:8080/swagger-ui.html`.

En el código React, las llamadas HTTP están centralizadas en `Tienda-SCAMusic-main/src/data/storage.js`; la portada cargada se consulta desde `src/utils/format.js`.

| Servicio | Método | Ruta pública | Uso comprobado en React |
|---|---|---|---|
| ms-auth | POST | `/api/v1/auth/login` | Iniciar sesión |
| ms-auth | POST | `/api/v1/auth/refresh` | Renovación JWT / F5 |
| ms-auth | POST | `/api/v1/auth/logout` | Cerrar sesión |
| ms-auth | POST | `/api/v1/usuarios/registro` | Registro de cliente |
| ms-auth | GET | `/api/v1/usuarios/me` | Recuperar sesión y perfil |
| ms-auth | PUT | `/api/v1/usuarios/me` | Editar perfil/cambiar contraseña |
| ms-auth | GET | `/api/v1/admin/usuarios` | Listado de usuarios del administrador y KPIs |
| ms-auth | POST | `/api/v1/admin/usuarios` | Crear usuario desde el administrador |
| ms-auth | PUT | `/api/v1/admin/usuarios/{id}` | Editar usuario o desactivar |
| ms-auth | DELETE | `/api/v1/admin/usuarios/{id}` | Eliminar usuario de prueba |
| ms-auth | POST | `/api/v1/contactos` | Formulario Contacto; **no existe GET** porque React no lee mensajes |
| ms-catalogo | GET | `/api/v1/productos` | Catálogo, filtros, detalle y vendedor (React utiliza colección) |
| ms-catalogo | POST | `/api/v1/productos` | Crear producto |
| ms-catalogo | PUT | `/api/v1/productos/{codigo}` | Editar producto |
| ms-catalogo | DELETE | `/api/v1/productos/{codigo}` | Eliminar producto |
| ms-catalogo | POST | `/api/v1/productos/imagenes` | Subir portada desde PC |
| ms-catalogo | GET | `/api/v1/productos/imagenes/{nombre}` | Mostrar portada subida |
| ms-catalogo | GET | `/api/v1/inventario/criticos` | Stock crítico del administrador |
| ms-categorias | GET | `/api/v1/categorias` | Home, filtros y administración |
| ms-categorias | POST | `/api/v1/categorias` | Crear categoría |
| ms-categorias | PUT | `/api/v1/categorias/{id}` | Editar categoría vacía |
| ms-categorias | DELETE | `/api/v1/categorias/{id}` | Eliminar categoría vacía |
| ms-carrito | GET | `/api/v1/carritos` | Recuperar carrito del cliente |
| ms-carrito | PUT | `/api/v1/carritos` | Guardar cantidad y productos |
| ms-pedido | GET | `/api/v1/ordenes` | Administración y vendedor; detalles se leen de lista |
| ms-pedido | GET | `/api/v1/ordenes/mias` | Compras del cliente |
| ms-pedido | POST | `/api/v1/ordenes` | Checkout / generar orden y boleta |
| ms-pedido | GET | `/api/v1/boletas` | Boletas de administrador; detalle se lee de lista |
| ms-pedido | GET | `/api/v1/boletas/mias` | Mis boletas; detalle se lee de lista |
| ms-reportes | GET | `/api/v1/reportes/resumen` | Panel y reportes administrativos |

La operación `POST /api/v1/ordenes` requiere la cabecera `Idempotency-Key` (UUID generado por React).
El servidor guarda primero un pedido **Pendiente**, reserva el stock de forma idempotente y
marca la orden **Pagada**. Una tarea de conciliación reintenta las operaciones pendientes
con la misma clave si se interrumpe la comunicación. En Swagger hay que enviar esa cabecera al probar compras.

## Operaciones internas no publicadas en Swagger

Estas operaciones **sí tienen relación funcional con React**, porque permiten crear compras y validar categorías; solo las llaman otros microservicios mediante Feign. Requieren `X-Internal-Key` y **no están en Gateway**:

- `GET /internal/catalogo/productos/{codigo}`: Pedido consulta un producto para validar precio y stock.
- `POST /internal/catalogo/reservar-lote/{clave}`: Pedido reserva/descuenta una sola vez el stock por clave de compra; reintentos con la misma clave no vuelven a descontar.
- `GET /internal/categorias/{id}`: Catálogo comprueba que exista la categoría del producto.

Para calcular el resumen de Reportes se reutilizan los endpoints públicos protegidos del administrador y los de listado de productos/órdenes; no se crean rutas adicionales.

## Eliminaciones tras cotejar controlador por controlador

- `GET /api/v1/contactos`: ninguna pantalla recibe mensajes; se conserva solo el POST del formulario. Los mensajes nuevos se guardan en `db_usuario.contacto` con `nombre`, `correo`, `comentario`, `fecha`. Los datos antiguos no se borran.
- `GET /api/v1/usuarios`, `POST /api/v1/usuarios`, `PUT /api/v1/usuarios/{id}`, `DELETE /api/v1/usuarios/{id}`: duplicados de las rutas de administración. Reportes ahora utiliza la ruta ya existente `/api/v1/admin/usuarios`.
- `GET /api/v1/ordenes/{codigo}`, `GET /api/v1/boletas/{numero}`: React ya muestra esos detalles con las listas recuperadas y no invoca estas rutas.
- Las tres rutas de consulta y reserva entre microservicios se trasladaron a `/internal`, con clave interna y sin acceso por Gateway.

Los seis servicios restantes tienen llamadas directas desde React; Gateway y Eureka coordinan la comunicación y no son microservicios de negocio adicionales.

**Nota:** las rutas REST eliminadas ya no están disponibles para clientes externos; si otro cliente las usaba tendría que adaptarse. Para revisar seguridad, GET público de catálogo y categorías se mantiene; usuarios, productos administrativos, compras, carrito y reportes conservan sus restricciones JWT.
