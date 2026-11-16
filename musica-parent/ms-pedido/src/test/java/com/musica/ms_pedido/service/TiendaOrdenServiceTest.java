package com.musica.ms_pedido.service;

import com.musica.ms_pedido.client.CatalogoClient;
import com.musica.ms_pedido.model.Pedido;
import com.musica.ms_pedido.repository.PedidoRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TiendaOrdenServiceTest {
    @Test void listadoSoloIncluyeOrdenesDeLaTiendaReact() {
        var repo=mock(PedidoRepository.class);
        var pedido=new Pedido();pedido.setCodigo("ORD-0001");pedido.setClienteCorreo("cliente@ejemplo.com");
        pedido.setEstado("Pagada");pedido.setDetalles(List.of());pedido.setTotalPago(0.0);
        when(repo.findAll()).thenReturn(List.of(pedido,new Pedido()));
        var service=new TiendaOrdenService(repo,mock(CatalogoClient.class));
        assertEquals(1,service.listar().size());
        assertEquals(1,service.mias("cliente@ejemplo.com").size());
        assertEquals(0,service.mias("otro@ejemplo.com").size());
    }
    @Test void rechazaPedidosSinProductos() {
        var service=new TiendaOrdenService(mock(PedidoRepository.class),mock(CatalogoClient.class));
        assertThrows(ResponseStatusException.class,()->service.crear(null,"cliente@ejemplo.com",1L));
    }
}
