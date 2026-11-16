package com.musica.ms_auth.controller;

import com.musica.ms_auth.dto.TiendaUsuarioDTO;
import com.musica.ms_auth.service.TiendaUsuarioService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name="Usuarios de SCAMusica",description="CRUD de usuarios, sesión y JWT para frontend React")
public class TiendaUsuarioController {
    private final TiendaUsuarioService service;
    public TiendaUsuarioController(TiendaUsuarioService service) {this.service=service;}
    @GetMapping public List<TiendaUsuarioDTO> listar() {return service.listar();}
    @PostMapping public ResponseEntity<TiendaUsuarioDTO> crear(@RequestBody TiendaUsuarioDTO d) {return ResponseEntity.status(201).body(service.crear(d,false));}
    @PostMapping("/registro") public ResponseEntity<TiendaUsuarioDTO> registro(@RequestBody TiendaUsuarioDTO d) {return ResponseEntity.status(201).body(service.crear(d,true));}
    @GetMapping("/me") public TiendaUsuarioDTO me(@RequestHeader("Authorization") String auth) {return service.dto(service.desdeToken(auth));}
    @PutMapping("/me") public TiendaUsuarioDTO perfil(@RequestHeader("Authorization") String auth,@RequestBody TiendaUsuarioDTO d) {
        return service.actualizar(service.desdeToken(auth).getId(),d,true,auth);
    }
    @PutMapping("/{id}") public TiendaUsuarioDTO actualizar(@PathVariable Long id,@RequestBody TiendaUsuarioDTO d) {return service.actualizar(id,d,false,null);}
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) {service.eliminar(id);return ResponseEntity.noContent().build();}
}
