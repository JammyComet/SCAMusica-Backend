-- SCAMusica: bases del modelo React, sin almacenar copias innecesarias.
-- Son diez servicios de negocio; cuatro consumen servicios fuente mediante OpenFeign.
-- Cada servicio con persistencia usa JPA (ddl-auto=update).
CREATE DATABASE IF NOT EXISTS db_usuario CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_catalogo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_categoria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_carrito CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_pedido CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_notificacion CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
