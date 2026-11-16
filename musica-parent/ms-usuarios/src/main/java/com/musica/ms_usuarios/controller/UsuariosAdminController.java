package com.musica.ms_usuarios.controller;
import com.musica.ms_usuarios.service.UsuariosAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/usuarios") @Tag(name="Administración de usuarios")
public class UsuariosAdminController {
 private final UsuariosAdminService s;
 public UsuariosAdminController(UsuariosAdminService s){this.s=s;}
 @GetMapping public List<Map<String,Object>> listar(@RequestHeader("Authorization") String jwt){return s.listar(jwt);}
 @PostMapping public ResponseEntity<Map<String,Object>> crear(@RequestHeader("Authorization") String jwt,@RequestBody Map<String,Object> u){return ResponseEntity.status(201).body(s.crear(jwt,u));}
 @PutMapping("/{id}") public Map<String,Object> editar(@RequestHeader("Authorization") String jwt,@PathVariable Long id,@RequestBody Map<String,Object> u){return s.actualizar(jwt,id,u);}
 @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@RequestHeader("Authorization") String jwt,@PathVariable Long id){s.eliminar(jwt,id);return ResponseEntity.noContent().build();}
}
