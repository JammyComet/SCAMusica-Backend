#!/bin/sh
set -eu
# El script se ejecuta solo en la inicialización del volumen (MySQL oficial).
# El usuario de la aplicación tiene permisos sobre las seis BD, no globales.
if [ -z "${MYSQL_USER:-}" ] || [ -z "${MYSQL_PASSWORD:-}" ]; then
  echo 'Faltan MYSQL_USER/MYSQL_PASSWORD' >&2
  exit 1
fi
mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
GRANT ALL PRIVILEGES ON db_usuario.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_catalogo.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_categoria.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_carrito.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_pedido.* TO '${MYSQL_USER}'@'%';
GRANT ALL PRIVILEGES ON db_notificacion.* TO '${MYSQL_USER}'@'%';
FLUSH PRIVILEGES;
SQL
