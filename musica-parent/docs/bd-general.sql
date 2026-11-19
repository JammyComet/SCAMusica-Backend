-- SCAMusica: bases del modelo React, sin almacenar copias innecesarias.
-- Seis servicios de negocio; cuatro antiguos intermediarios se integraron con los propietarios de los datos.
-- Cada servicio con persistencia usa JPA (ddl-auto=update).
CREATE DATABASE IF NOT EXISTS db_usuario CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_catalogo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_categoria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_carrito CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_pedido CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
