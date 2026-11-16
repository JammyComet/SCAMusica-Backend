package com.musica.ms_pedido.client;

import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "ms-catalogo")
public interface CatalogoClient {
    @org.springframework.web.bind.annotation.GetMapping("/api/v1/productos/{codigo}")
    com.musica.ms_pedido.dto.TiendaProductoDTO obtenerProductoTienda(@org.springframework.web.bind.annotation.PathVariable("codigo") String codigo);
    @org.springframework.web.bind.annotation.PostMapping("/api/v1/productos/descontar-lote")
    void descontarStockTienda(@org.springframework.web.bind.annotation.RequestBody java.util.List<com.musica.ms_pedido.dto.TiendaOrdenDTO.Linea> lineas, @org.springframework.web.bind.annotation.RequestHeader("X-Internal-Key") String internalKey);
}