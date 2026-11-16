package com.musica.ms_pedido.dto;

import lombok.Data;
@Data public class TiendaProductoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String artista;
    private Integer precio;
    private Integer descuento;
    private Integer stock;
    private String estado;
}
