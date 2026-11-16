# API REST SCAMusica — 10 servicios de negocio

Gateway local: `http://localhost:8080`. Todas las APIs REST consumidas por React se encuentran
aquí; cada servicio mantiene su Swagger en `/v3/api-docs`. Swagger centralizado en
`http://localhost:8080/swagger-ui.html`.

| Microservicio | Endpoint | Permiso | Pantalla |
|---|---|---|---|
| ms-auth | `POST /api/v1/auth/login` | Público | Login |
| ms-auth | `POST /api/v1/usuarios/registro` | Público | Registro |
| ms-auth | `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout` | Cookie HttpOnly + header X-Requested-With | Renovar/cerrar sesión |
| ms-auth | `GET/PUT /api/v1/usuarios/me` | JWT | Perfil/sesión |
| ms-usuarios | `GET/POST /api/v1/admin/usuarios`, `PUT/DELETE /api/v1/admin/usuarios/{id}` | Admin | Usuarios |
| ms-catalogo | `GET /api/v1/productos`, `GET /api/v1/productos/{codigo}` | Público | Catálogo y ofertas |
| ms-catalogo | `POST /api/v1/productos`, `PUT/DELETE /api/v1/productos/{codigo}` | Admin | Productos |
| ms-categorias | `GET /api/v1/categorias`, `GET /api/v1/categorias/{id}` | Público | Categorías |
| ms-categorias | `POST /api/v1/categorias`, `PUT/DELETE /api/v1/categorias/{id}` | Admin | Categorías |
| ms-carrito | `GET/PUT /api/v1/carritos` | Cliente | Carrito |
| ms-pedido | `GET /api/v1/ordenes` | Admin/Vendedor | Órdenes |
| ms-pedido | `GET /api/v1/ordenes/mias`, `POST /api/v1/ordenes` | Cliente | Compras |
| ms-pedido | `GET /api/v1/ordenes/{codigo}` | Dueño/Admin/Vendedor | Orden detalle |
| ms-boletas | `GET /api/v1/boletas` | Admin | Todas las boletas |
| ms-boletas | `GET /api/v1/boletas/mias`, `GET /api/v1/boletas/{numero}` | Cliente (solo suyas) | Mis boletas |
| ms-notificacion | `POST /api/v1/contactos` | Público | Contacto |
| ms-notificacion | `GET /api/v1/contactos` | Admin | Contactos (API) |
| ms-inventario | `GET /api/v1/inventario/criticos` | Admin | Reportes stock crítico |
| ms-reportes | `GET /api/v1/reportes/resumen` | Admin | Panel y reportes |

## Notas de integración

- React lee estos endpoints por `fetch` desde `src/data/storage.js` y no los duplica en navegador.
- `ms-catalogo` valida la categoría de un producto consultando `ms-categorias` por Feign.
- `ms-categorias` impide eliminar categorías usadas en productos consultando el catálogo.
- `ms-usuarios` delega el CRUD al servicio propietario `ms-auth` con el mismo JWT; las contraseñas
  permanecen solo en el backend y se almacenan con BCrypt.
- `ms-boletas` consulta exclusivamente órdenes pagadas y restringe al cliente a sus compras.
- `ms-inventario` consulta el stock del catálogo y `ms-reportes` resume ventas, usuarios y stock.
- El stock se descuenta internamente desde `ms-pedido` a `ms-catalogo` usando
  `POST /api/v1/productos/descontar-lote` y la cabecera técnica `X-Internal-Key`.

Las cuentas de prueba deben activarse explícitamente con contraseñas propias; no existen secretos por defecto. La ejecución real
contra MySQL y las pruebas de integración quedan pendientes de validación fuera de este entorno.
