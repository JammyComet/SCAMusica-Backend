package com.musica.ms_usuarios.client;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name="ms-auth")
public interface AuthUsuariosClient {
 @GetMapping("/api/v1/usuarios") List<Map<String,Object>> listar(@RequestHeader("Authorization") String token);
 @PostMapping("/api/v1/usuarios") Map<String,Object> crear(@RequestHeader("Authorization") String token,@RequestBody Map<String,Object> usuario);
 @PutMapping("/api/v1/usuarios/{id}") Map<String,Object> editar(@RequestHeader("Authorization") String token,@PathVariable("id") Long id,@RequestBody Map<String,Object> usuario);
 @DeleteMapping("/api/v1/usuarios/{id}") void eliminar(@RequestHeader("Authorization") String token,@PathVariable("id") Long id);
}
