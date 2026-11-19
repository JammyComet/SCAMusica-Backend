package com.musica.ms_pedido.controller;
import com.musica.ms_pedido.service.TiendaOrdenService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoletasControllerTest {
  @Test void devuelveSoloComprasPagadas() {
    var ordenes=mock(TiendaOrdenService.class);
    when(ordenes.mias(7L)).thenReturn(List.of(
      Map.of("estado","Pagada","boletaNumero","BOL-0001"),
      Map.of("estado","Pendiente","boletaNumero","BOL-0002")));
    var boletas=new BoletasController(ordenes);
    assertEquals(1,boletas.mias(7L).size());
  }
}
