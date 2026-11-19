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
        var pedido=new Pedido();pedido.setCodigo("ORD-0001");pedido.setClienteId(12L);pedido.setClienteCorreo("cliente@ejemplo.com");
        pedido.setEstado("Pagada");pedido.setDetalles(List.of());pedido.setTotalPago(0.0);
        when(repo.findAll()).thenReturn(List.of(pedido,new Pedido()));
        when(repo.findByClienteIdOrderByIdDesc(12L)).thenReturn(List.of(pedido));
        var service=new TiendaOrdenService(repo,mock(CatalogoClient.class),mock(PedidoTransacciones.class));
        assertEquals(1,service.listar().size());
        // Una orden incompleta no debe romper la lista de admin ni mis boletas.
        @SuppressWarnings("unchecked")
        var entrega=(java.util.Map<String,Object>)service.listar().get(0).get("entrega");
        assertEquals("",entrega.get("region"));
        assertEquals("",entrega.get("comuna"));
        assertEquals("",entrega.get("direccion"));
        assertEquals(1,service.mias(12L).size());
        assertEquals(0,service.mias(99L).size());
    }
    @Test void correoReutilizadoNoExponeBoletasDeOtraCuenta() {
        var repo=mock(PedidoRepository.class);
        var vieja=new Pedido(); vieja.setClienteId(10L); vieja.setClienteCorreo("reutilizado@gmail.com");
        vieja.setEstado("Pagada"); vieja.setDetalles(List.of()); vieja.setCodigo("ORD-0010");
        when(repo.findByClienteIdOrderByIdDesc(11L)).thenReturn(List.of());
        var service=new TiendaOrdenService(repo,mock(CatalogoClient.class),mock(PedidoTransacciones.class));
        assertTrue(service.mias(11L).isEmpty());
        verify(repo).findByClienteIdOrderByIdDesc(11L);
    }
    @Test void rechazaPedidosSinProductos() {
        var service=new TiendaOrdenService(mock(PedidoRepository.class),mock(CatalogoClient.class),mock(PedidoTransacciones.class));
        assertThrows(ResponseStatusException.class,()->service.crear(null,"cliente@ejemplo.com",1L,"123e4567-e89b-12d3-a456-426614174000"));
    }
    @Test void repetirCompraPagadaDevuelveMismaBoletaSinPedirStock() {
        var repo=mock(PedidoRepository.class);
        var catalogo=mock(CatalogoClient.class);
        var tx=mock(PedidoTransacciones.class);
        var service=new TiendaOrdenService(repo,catalogo,tx);
        var orden=new com.musica.ms_pedido.dto.TiendaOrdenDTO(
          new com.musica.ms_pedido.dto.TiendaOrdenDTO.Cliente("11111111","Cliente","cliente@ejemplo.com","","RM","Santiago","Calle 123"),
          new com.musica.ms_pedido.dto.TiendaOrdenDTO.Entrega("RM","Santiago","Calle 123"),
          List.of(new com.musica.ms_pedido.dto.TiendaOrdenDTO.Linea("DIS-001",1)));
        String clave="123e4567-e89b-12d3-a456-426614174000";
        String firma=org.springframework.test.util.ReflectionTestUtils.invokeMethod(service,"huella",orden);
        var pagada=new Pedido(); pagada.setId(42L);pagada.setClaveSolicitud("7-"+clave);
        pagada.setHuellaSolicitud(firma);pagada.setEstado("Pagada");pagada.setCodigo("ORD-0042");
        pagada.setBoletaNumero("BOL-0042");pagada.setDetalles(List.of());pagada.setTotalPago(100.0);
        when(tx.porClave("7-"+clave)).thenReturn(java.util.Optional.of(pagada));
        when(tx.porId(42L)).thenReturn(pagada);
        var respuesta=service.crear(orden,"cliente@ejemplo.com",7L,clave);
        assertEquals("BOL-0042",respuesta.get("boletaNumero"));
        verifyNoInteractions(catalogo);
        verify(tx,never()).crearPendiente(any());
        verify(tx,never()).confirmar(anyLong());
    }
    @Test void recuperaPedidoPendienteSinReservarConUnaClaveNueva() {
        var repo=mock(PedidoRepository.class);
        var catalogo=mock(CatalogoClient.class);
        var tx=mock(PedidoTransacciones.class);
        var service=new TiendaOrdenService(repo,catalogo,tx);
        var pendiente=new Pedido();pendiente.setId(8L);
        pendiente.setClaveSolicitud("7-123e4567-e89b-12d3-a456-426614174000");
        pendiente.setEstado("Pendiente");pendiente.setDetalles(List.of());
        when(tx.pendientes()).thenReturn(List.of(pendiente));
        when(tx.porId(8L)).thenReturn(pendiente);
        System.setProperty("scamusic.internal.key","clave-interna-de-prueba");
        try {
            service.conciliarPendientes();
            verify(catalogo).reservarStockTienda(eq(pendiente.getClaveSolicitud()),eq(List.of()),eq("clave-interna-de-prueba"));
            verify(tx).confirmar(8L);
        }finally {System.clearProperty("scamusic.internal.key");}
    }
}
