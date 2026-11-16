package com.musica.ms_reportes.service;
import com.musica.ms_reportes.client.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReportesServiceTest {
 @Test void reportaVentasPagadasYUnidades(){
  var pedidos=mock(ReportesPedidoClient.class);var productos=mock(ReportesProductoClient.class);var usuarios=mock(ReportesUsuarioClient.class);
  when(pedidos.listar("Bearer admin")).thenReturn(List.of(
    Map.of("estado","Pagada","total",9000,"productos",List.of(Map.of("cantidad",2))),
    Map.of("estado","Pendiente","total",8000,"productos",List.of(Map.of("cantidad",1)))));
  when(productos.listar()).thenReturn(List.of(Map.of("estado","Activo","stock",1,"stockCritico",2)));
  when(usuarios.listar("Bearer admin")).thenReturn(List.of(Map.of("correo","x@example.com")));
  var resumen=new ReportesService(pedidos,productos,usuarios).resumen("Bearer admin");
  assertEquals(9000.0,resumen.get("ventasTotales"));
  assertEquals(1,resumen.get("ordenesPagadas"));
  assertEquals(2L,resumen.get("unidadesVendidas"));
  assertEquals(1L,resumen.get("stockCritico"));
 }
}
