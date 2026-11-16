package com.musica.ms_pedido.dto;

import java.util.List;

public record TiendaOrdenDTO(Cliente cliente, Entrega entrega, List<Linea> productos) {
    public record Cliente(String run,String nombre,String correo,String telefono,String region,String comuna,String direccion) {}
    public record Entrega(String region,String comuna,String direccion) {}
    public record Linea(String codigo,int cantidad) {}
}
