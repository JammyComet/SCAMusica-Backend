package com.musica.ms_categorias.service;
import com.musica.ms_categorias.client.ProductoClient;
import com.musica.ms_categorias.model.Categoria;
import com.musica.ms_categorias.repository.CategoriaRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CategoriaServiceTest {
 @Test void evitaEliminarCategoriaConProductos(){
   var repo=mock(CategoriaRepository.class);var productos=mock(ProductoClient.class);
   var cat=new Categoria();cat.setId(3L);cat.setNombre("Metal");
   when(repo.findById(3L)).thenReturn(Optional.of(cat));
   when(productos.listar()).thenReturn(List.of(Map.of("categoriaId",3)));
   assertThrows(ResponseStatusException.class,()->new CategoriaService(repo,productos).eliminar(3L));
   verify(repo,never()).delete(any());
 }
 @Test void noAceptaCategoriaDuplicada(){
   var repo=mock(CategoriaRepository.class);var productos=mock(ProductoClient.class);
   when(repo.existsByNombreIgnoreCase("Rock")).thenReturn(true);
   var entrada=new Categoria();entrada.setNombre("Rock");
   assertThrows(ResponseStatusException.class,()->new CategoriaService(repo,productos).crear(entrada));
 }
}
