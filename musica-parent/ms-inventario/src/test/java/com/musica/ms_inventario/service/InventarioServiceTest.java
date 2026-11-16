package com.musica.ms_inventario.service;
import com.musica.ms_inventario.client.ProductoInventarioClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InventarioServiceTest {
 @Test void identificaStockCriticoDeProductosReales(){
   var productos=mock(ProductoInventarioClient.class);
   when(productos.listar()).thenReturn(List.of(
      Map.of("codigo","A","stock",1,"stockCritico",3),
      Map.of("codigo","B","stock",8,"stockCritico",3)));
   var s=new InventarioService(productos);
   assertEquals("A",s.criticos().get(0).get("codigo"));
   assertEquals(1,s.criticos().size());
 }
}
