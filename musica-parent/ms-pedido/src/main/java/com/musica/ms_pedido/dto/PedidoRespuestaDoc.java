package com.musica.ms_pedido.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Esquema OpenAPI exacto de la respuesta de órdenes y boletas (no altera el JSON). */
@Schema(name="OrdenBoleta",description="Compra pagada devuelta por SCAMusica")
public record PedidoRespuestaDoc(
    @Schema(example="ORD-0001") String codigo,
    @Schema(example="BOL-0001") String boletaNumero,
    @Schema(example="2026-11-18") String fecha,
    Cliente cliente, Entrega entrega, List<Linea> productos,
    @Schema(example="29990") Double total,
    @Schema(example="Pagada") String estado
) {
    public record Cliente(String run,String nombre,String correo,String telefono,String region,String comuna,String direccion) {}
    public record Entrega(String region,String comuna,String direccion) {}
    public record Linea(String codigo,String nombre,String artista,Double precioOriginal,Integer descuento,Double precio,Integer cantidad,Double subtotal) {}
}
