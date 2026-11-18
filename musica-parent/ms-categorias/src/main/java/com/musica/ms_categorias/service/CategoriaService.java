package com.musica.ms_categorias.service;
import com.musica.ms_categorias.client.ProductoClient;
import com.musica.ms_categorias.model.Categoria;
import com.musica.ms_categorias.repository.CategoriaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class CategoriaService {
 private final CategoriaRepository repository;
 private final ProductoClient productos;
 public CategoriaService(CategoriaRepository repository,ProductoClient productos){this.repository=repository;this.productos=productos;}
 public List<Categoria> listar(){return repository.findAll();}
 public Categoria uno(Long id){return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Categoría no encontrada"));}
 public Categoria crear(Categoria data){
   String nombre=nombreValido(data);
   if(repository.existsByNombreIgnoreCase(nombre))throw new ResponseStatusException(HttpStatus.CONFLICT,"Categoría duplicada");
   Categoria c=new Categoria();c.setNombre(nombre);return repository.save(c);
 }
 public Categoria editar(Long id,Categoria data){
   Categoria c=uno(id);String nombre=nombreValido(data);
   if(tieneProductosAsociados(id))
       throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede editar una categoría con productos asociados");
   if(!c.getNombre().equalsIgnoreCase(nombre)&&repository.existsByNombreIgnoreCase(nombre))throw new ResponseStatusException(HttpStatus.CONFLICT,"Categoría duplicada");
   c.setNombre(nombre);return repository.save(c);
 }
 public void eliminar(Long id){
   Categoria cat=uno(id);
   if(tieneProductosAsociados(id))
       throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede eliminar una categoría con productos asociados");
   repository.delete(cat);
 }
 private boolean tieneProductosAsociados(Long id){
   // La restricción se comprueba aquí, no solo en React: protege también las llamadas REST directas.
   // Si el catálogo no está disponible, Feign propaga el error y no se modifica la categoría.
   return productos.listar().stream().anyMatch(p->p.get("categoriaId") instanceof Number n && id.longValue()==n.longValue());
 }
 private String nombreValido(Categoria c){
   if(c==null||c.getNombre()==null||c.getNombre().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Nombre obligatorio");
   return c.getNombre().trim();
 }
 @Bean public CommandLineRunner cargarIniciales(ObjectMapper objectMapper){return args->{
    // Los datos de ejemplo se cargan solo en una BD nueva: al reiniciar no se revierten
    // los nombres modificados ni reaparecen las categorías eliminadas.
    if(repository.count()>0)return;
    List<Categoria> categorias=objectMapper.readValue(new ClassPathResource("tienda-categorias.json").getInputStream(),new TypeReference<List<Categoria>>(){});
    for(Categoria c:categorias)if(!repository.existsByNombreIgnoreCase(c.getNombre())){
       Categoria nuevo=new Categoria();nuevo.setNombre(c.getNombre());repository.save(nuevo);
    }
 };}
}
