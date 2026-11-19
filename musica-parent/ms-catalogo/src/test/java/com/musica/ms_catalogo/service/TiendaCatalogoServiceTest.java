package com.musica.ms_catalogo.service;

import com.musica.ms_catalogo.model.Catalogo;
import com.musica.ms_catalogo.repository.CatalogoRepository;
import com.musica.ms_catalogo.client.CategoriasClient;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TiendaCatalogoServiceTest {
    @Test void noDescuentaStockInsuficiente() {
        var productos=mock(CatalogoRepository.class);
        var categorias=mock(CategoriasClient.class);
        var service=new TiendaCatalogoService(productos,categorias,mock(com.musica.ms_catalogo.repository.StockReservaRepository.class));
        var producto=new Catalogo();producto.setCodigo("DIS-001");producto.setEstado("Activo");producto.setStock(1);
        when(productos.bloquearCodigo("DIS-001")).thenReturn(Optional.of(producto));
        assertThrows(ResponseStatusException.class,()->service.descontarLote(List.of(Map.of("codigo","DIS-001","cantidad",2))));
        assertEquals(1,producto.getStock());
        verify(productos,never()).save(any());
    }
    @Test void descuentaStockAgrupadoParaEvitarSobreventa() {
        var productos=mock(CatalogoRepository.class);
        var categorias=mock(CategoriasClient.class);
        var service=new TiendaCatalogoService(productos,categorias,mock(com.musica.ms_catalogo.repository.StockReservaRepository.class));
        var producto=new Catalogo();producto.setCodigo("DIS-001");producto.setEstado("Activo");producto.setStock(8);
        when(productos.bloquearCodigo("DIS-001")).thenReturn(Optional.of(producto));
        service.descontarLote(List.of(Map.of("codigo","DIS-001","cantidad",2),Map.of("codigo","DIS-001","cantidad",3)));
        assertEquals(3,producto.getStock());
        verify(productos,times(1)).save(producto);
    }
    @Test void rechazaCantidadDecimalSinTruncar() {
        var productos=mock(CatalogoRepository.class);
        var service=new TiendaCatalogoService(productos,mock(CategoriasClient.class),mock(com.musica.ms_catalogo.repository.StockReservaRepository.class));
        assertThrows(ResponseStatusException.class,()->service.descontarLote(List.of(Map.of("codigo","DIS-001","cantidad",1.5))));
        verify(productos,never()).bloquearCodigo(anyString());
    }
    @Test void noPermiteCrearProductoSinArtista() {
        var productos=mock(CatalogoRepository.class);
        var service=new TiendaCatalogoService(productos,mock(CategoriasClient.class),mock(com.musica.ms_catalogo.repository.StockReservaRepository.class));
        Catalogo p=new Catalogo();p.setCodigo("DIS-100");p.setNombre("Disco de prueba");p.setStock(5);
        p.setCategoriaId(1L);p.setFormato("Vinilo");p.setEstado("Activo");
        assertThrows(ResponseStatusException.class,()->service.guardar(p,null));
        verify(productos,never()).save(any());
    }
    @Test void reservaUnaVezLaMismaCompraAunqueLleveDosSolicitudes() {
        var productos=mock(CatalogoRepository.class);
        var reservas=mock(com.musica.ms_catalogo.repository.StockReservaRepository.class);
        var service=new TiendaCatalogoService(productos,mock(CategoriasClient.class),reservas);
        var producto=new Catalogo();producto.setCodigo("DIS-001");producto.setEstado("Activo");producto.setStock(5);
        when(productos.bloquearCodigo("DIS-001")).thenReturn(Optional.of(producto));
        String clave="7-123e4567-e89b-12d3-a456-426614174000";
        var lineas=List.<Map<String,Object>>of(Map.of("codigo","DIS-001","cantidad",2));
        when(reservas.findById(clave)).thenReturn(Optional.empty()).thenReturn(
            Optional.of(new com.musica.ms_catalogo.model.StockReserva(clave,"7:DIS-001:2;")));
        service.reservarLote(clave,lineas);
        service.reservarLote(clave,lineas);
        assertEquals(3,producto.getStock());
        verify(productos,times(1)).save(producto);
        verify(reservas,times(1)).saveAndFlush(any());
    }
    @Test void mismaClaveNoAceptaOtrosProductos() {
        var productos=mock(CatalogoRepository.class);
        var reservas=mock(com.musica.ms_catalogo.repository.StockReservaRepository.class);
        var service=new TiendaCatalogoService(productos,mock(CategoriasClient.class),reservas);
        String clave="7-123e4567-e89b-12d3-a456-426614174000";
        when(reservas.findById(clave)).thenReturn(Optional.of(new com.musica.ms_catalogo.model.StockReserva(clave,"7:DIS-001:2;")));
        assertThrows(ResponseStatusException.class,()->service.reservarLote(clave,List.of(Map.of("codigo","DIS-001","cantidad",3))));
        verify(productos,never()).bloquearCodigo(anyString());
    }
}
