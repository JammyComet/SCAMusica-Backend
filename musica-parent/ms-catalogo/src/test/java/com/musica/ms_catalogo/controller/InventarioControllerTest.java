package com.musica.ms_catalogo.controller;
import com.musica.ms_catalogo.model.Catalogo;
import com.musica.ms_catalogo.service.TiendaCatalogoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InventarioControllerTest {
 @Test void calculaCriticosDesdeProductosSinServicioIntermediario() {
   var catalogo=mock(TiendaCatalogoService.class);
   var critico=new Catalogo();critico.setStock(2);critico.setStockCritico(5);
   var normal=new Catalogo();normal.setStock(10);normal.setStockCritico(5);
   when(catalogo.listar()).thenReturn(List.of(normal,critico));
   assertEquals(List.of(critico),new InventarioController(catalogo).criticos());
 }
}
