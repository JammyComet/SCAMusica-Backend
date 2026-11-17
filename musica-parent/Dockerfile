# Única imagen para los 10 microservicios, Eureka y API Gateway.
# Maven compila el proyecto multi-módulo tal como está estructurado en musica-parent.
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -ntp -Dmaven.test.skip=true package && \
    mkdir -p /runtime && \
    for module in eureka-server api-gateway ms-auth ms-usuarios ms-catalogo ms-categorias ms-carrito ms-pedido ms-boletas ms-notificacion ms-inventario ms-reportes; do \
      jar="/workspace/$module/target/$module-0.0.1-SNAPSHOT.jar"; \
      if [ ! -f "$jar" ]; then echo "ERROR: No se genero $jar" >&2; exit 1; fi; \
      cp "$jar" "/runtime/$module.jar"; \
    done

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S scamusica && adduser -S -G scamusica scamusica && \
    mkdir -p /app/uploads/productos && \
    chown -R scamusica:scamusica /app/uploads
COPY --from=build --chown=scamusica:scamusica /runtime/ /app/
USER scamusica
# La orden java -jar se define en docker-compose.yml para cada módulo.
