package com.musica.ms_boletas.service;
import com.musica.ms_boletas.client.PedidoBoletaClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BoletasServiceTest {
 @Test void clienteNoPuedeConsultarUnaBoletaAjena(){
  var pedido=mock(PedidoBoletaClient.class);
  when(pedido.mias("Bearer jwt")).thenReturn(List.of(Map.of("boletaNumero","BOL-1","estado","Pagada")));
  var s=new BoletasService(pedido);
  assertThrows(ResponseStatusException.class,()->s.detalle("Bearer jwt","BOL-2","CLIENTE"));
  verify(pedido,never()).todas(anyString());
 }
 @Test void noExponeOrdenesNoPagadas(){
  var pedido=mock(PedidoBoletaClient.class);
  when(pedido.todas("Bearer jwt")).thenReturn(List.of(Map.of("boletaNumero","BOL-1","estado","Pagada"),Map.of("estado","Pendiente")));
  assertEquals(1,new BoletasService(pedido).todas("Bearer jwt").size());
 }
}
