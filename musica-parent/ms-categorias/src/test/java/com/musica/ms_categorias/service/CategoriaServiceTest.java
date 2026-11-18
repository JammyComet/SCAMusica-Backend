package com.musica.ms_categorias.service;
import com.musica.ms_categorias.client.ProductoClient;
import com.musica.ms_categorias.model.Categoria;
import com.musica.ms_categorias.repository.CategoriaRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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
 @Test void impideEditarCategoriaConProductos(){
   var repo=mock(CategoriaRepository.class);var productos=mock(ProductoClient.class);
   var cat=new Categoria();cat.setId(3L);cat.setNombre("Rock");
   var entrada=new Categoria();entrada.setNombre("Jazz");
   when(repo.findById(3L)).thenReturn(Optional.of(cat));
   when(productos.listar()).thenReturn(List.of(Map.of("categoriaId",3)));
   var error=assertThrows(ResponseStatusException.class,()->new CategoriaService(repo,productos).editar(3L,entrada));
   assertEquals(HttpStatus.CONFLICT,error.getStatusCode());
   assertEquals("Rock",cat.getNombre());
   verify(repo,never()).save(any());
 }
 @Test void dejaEditarCategoriaSinProductos(){
   var repo=mock(CategoriaRepository.class);var productos=mock(ProductoClient.class);
   var cat=new Categoria();cat.setId(4L);cat.setNombre("Nuevo");
   var entrada=new Categoria();entrada.setNombre(" Jazz ");
   when(repo.findById(4L)).thenReturn(Optional.of(cat));
   when(productos.listar()).thenReturn(List.of(Map.of("categoriaId",2)));
   when(repo.save(cat)).thenReturn(cat);
   var editada=new CategoriaService(repo,productos).editar(4L,entrada);
   assertEquals("Jazz",editada.getNombre());
   verify(repo).save(cat);
 }
 @Test void dejaEliminarCategoriaSinProductos(){
   var repo=mock(CategoriaRepository.class);var productos=mock(ProductoClient.class);
   var cat=new Categoria();cat.setId(4L);cat.setNombre("Vacía");
   when(repo.findById(4L)).thenReturn(Optional.of(cat));
   when(productos.listar()).thenReturn(List.of());
   new CategoriaService(repo,productos).eliminar(4L);
   verify(repo).delete(cat);
 }

}
