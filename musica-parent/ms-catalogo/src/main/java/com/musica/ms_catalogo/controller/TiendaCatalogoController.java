package com.musica.ms_catalogo.controller;

import com.musica.ms_catalogo.model.Catalogo;
import com.musica.ms_catalogo.service.TiendaCatalogoService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name="Productos SCAMusica",description="CRUD del catálogo de productos y descuentos en MySQL")
public class TiendaCatalogoController {
    private final TiendaCatalogoService s;
    private final com.musica.ms_catalogo.service.ImagenProductoService imagenes;
    public TiendaCatalogoController(TiendaCatalogoService s,com.musica.ms_catalogo.service.ImagenProductoService imagenes){this.s=s;this.imagenes=imagenes;}
    @PostMapping(value="/api/v1/productos/imagenes",consumes="multipart/form-data")
    public Map<String,String> subirImagen(@RequestParam("archivo") org.springframework.web.multipart.MultipartFile archivo){return imagenes.subir(archivo);}
    @GetMapping("/api/v1/productos/imagenes/{nombre:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> imagen(@PathVariable String nombre){return imagenes.obtener(nombre);}
    @GetMapping("/api/v1/productos") public List<Catalogo> listar(){return s.listar();}
    @GetMapping("/api/v1/productos/{codigo}") public Catalogo uno(@PathVariable String codigo){return s.porCodigo(codigo);}
    @PostMapping("/api/v1/productos") public ResponseEntity<Catalogo> crear(@RequestBody Catalogo p){return ResponseEntity.status(201).body(s.guardar(p,null));}
    @PutMapping("/api/v1/productos/{codigo}") public Catalogo editar(@PathVariable String codigo,@RequestBody Catalogo p){return s.guardar(p,codigo);}
    @DeleteMapping("/api/v1/productos/{codigo}") public ResponseEntity<Void> eliminar(@PathVariable String codigo){s.eliminar(codigo);return ResponseEntity.noContent().build();}
    @PostMapping("/api/v1/productos/descontar-lote") public void descontar(@RequestBody List<Map<String,Object>> lineas){s.descontarLote(lineas);}
}
