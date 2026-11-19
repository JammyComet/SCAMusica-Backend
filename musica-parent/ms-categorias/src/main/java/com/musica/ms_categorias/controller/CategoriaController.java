package com.musica.ms_categorias.controller;
import com.musica.ms_categorias.model.Categoria;
import com.musica.ms_categorias.service.CategoriaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/categorias") @Tag(name="Categorías del catálogo")
public class CategoriaController {
 private final CategoriaService s;
 public CategoriaController(CategoriaService s){this.s=s;}
 @GetMapping public List<Categoria> listar(){return s.listar();}
 @PostMapping public ResponseEntity<Categoria> crear(@RequestBody Categoria c){return ResponseEntity.status(201).body(s.crear(c));}
 @PutMapping("/{id}") public Categoria editar(@PathVariable Long id,@RequestBody Categoria c){return s.editar(id,c);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id){s.eliminar(id);return ResponseEntity.noContent().build();}
}
