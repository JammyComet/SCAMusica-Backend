package com.musica.ms_carrito.service;

import com.musica.ms_carrito.dto.TiendaCarritoItemDTO;
import com.musica.ms_carrito.model.Carrito;
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
    @Test void noAceptaDuplicadosNiCantidadesNegativas() {
        var service=new TiendaCarritoService(mock(CarritoRepository.class));
        assertThrows(ResponseStatusException.class, ()->service.guardar(4L,List.of(new TiendaCarritoItemDTO(1L,1),new TiendaCarritoItemDTO(1L,1))));
        assertThrows(ResponseStatusException.class, ()->service.guardar(4L,List.of(new TiendaCarritoItemDTO(1L,-1))));
    }
}
