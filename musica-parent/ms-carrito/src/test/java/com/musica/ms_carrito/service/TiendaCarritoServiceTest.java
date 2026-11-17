package com.musica.ms_carrito.service;

import com.musica.ms_carrito.dto.TiendaCarritoItemDTO;
import com.musica.ms_carrito.model.Carrito;
import com.musica.ms_carrito.model.CarritoItem;
import com.musica.ms_carrito.repository.CarritoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TiendaCarritoServiceTest {
    @Test void carritoVacioYGuardadoPorUsuario() {
        var repo=mock(CarritoRepository.class);
        when(repo.findByClienteId(-4L)).thenReturn(Optional.empty());
        var service=new TiendaCarritoService(repo);
        assertEquals(List.of(), service.obtener(4L));
        var items=List.of(new TiendaCarritoItemDTO(6L,2));
        assertEquals(items,service.guardar(4L,items));
        var captor=org.mockito.ArgumentCaptor.forClass(Carrito.class);
        verify(repo).save(captor.capture());
        assertEquals(-4L,captor.getValue().getClienteId());
        assertEquals(2,captor.getValue().getItems().get(0).getCantidad());
    }
    @Test void agregarMismoProductoDosVecesActualizaFilaSinDuplicarla() {
        var repo=mock(CarritoRepository.class);
        var carrito=new Carrito();
        carrito.setId(11L);
        carrito.setClienteId(-4L);
        var item=new CarritoItem();
        item.setId(20L);
        item.setCarrito(carrito);
        item.setProductoId(6L);
        item.setCantidad(1);
        carrito.getItems().add(item);
        when(repo.findByClienteId(-4L)).thenReturn(Optional.of(carrito));
        var service=new TiendaCarritoService(repo);

        assertEquals(List.of(new TiendaCarritoItemDTO(6L,2)),
            service.guardar(4L,List.of(new TiendaCarritoItemDTO(6L,2))));
        assertEquals(1,carrito.getItems().size());
        assertSame(item,carrito.getItems().get(0));
        assertEquals(20L,carrito.getItems().get(0).getId());
        assertEquals(2,carrito.getItems().get(0).getCantidad());

        service.guardar(4L,List.of(new TiendaCarritoItemDTO(6L,3)));
        assertEquals(1,carrito.getItems().size());
        assertSame(item,carrito.getItems().get(0));
        assertEquals(3,carrito.getItems().get(0).getCantidad());
        verify(repo,times(2)).save(carrito);
    }

    @Test void eliminarUnProductoConservaLosDemas() {
        var repo=mock(CarritoRepository.class);
        var carrito=new Carrito();
        carrito.setClienteId(-4L);
        var primero=new CarritoItem();
        primero.setProductoId(6L);primero.setCantidad(1);primero.setCarrito(carrito);
        var segundo=new CarritoItem();
        segundo.setProductoId(7L);segundo.setCantidad(2);segundo.setCarrito(carrito);
        carrito.getItems().add(primero);carrito.getItems().add(segundo);
        when(repo.findByClienteId(-4L)).thenReturn(Optional.of(carrito));
        new TiendaCarritoService(repo).guardar(4L,List.of(new TiendaCarritoItemDTO(7L,4)));
        assertEquals(1,carrito.getItems().size());
        assertSame(segundo,carrito.getItems().get(0));
        assertEquals(4,segundo.getCantidad());
    }

    @Test void noAceptaDuplicadosNiCantidadesNegativas() {
        var service=new TiendaCarritoService(mock(CarritoRepository.class));
        assertThrows(ResponseStatusException.class, ()->service.guardar(4L,List.of(new TiendaCarritoItemDTO(1L,1),new TiendaCarritoItemDTO(1L,1))));
        assertThrows(ResponseStatusException.class, ()->service.guardar(4L,List.of(new TiendaCarritoItemDTO(1L,-1))));
    }
}
