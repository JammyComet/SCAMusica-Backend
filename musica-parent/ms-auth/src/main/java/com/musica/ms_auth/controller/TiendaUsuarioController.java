package com.musica.ms_auth.controller;

import com.musica.ms_auth.dto.TiendaUsuarioDTO;
import com.musica.ms_auth.service.TiendaUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name="Usuarios de SCAMusica",description="Registro de clientes y datos de la sesión actual")
public class TiendaUsuarioController {
    private final TiendaUsuarioService service;
    public TiendaUsuarioController(TiendaUsuarioService service) {this.service=service;}
    @PostMapping("/registro") public ResponseEntity<TiendaUsuarioDTO> registro(@RequestBody TiendaUsuarioDTO d) {return ResponseEntity.status(201).body(service.crear(d,true));}
    @GetMapping("/me") public TiendaUsuarioDTO me(@RequestHeader("Authorization") String auth) {return service.dto(service.desdeToken(auth));}
    @PutMapping("/me") public TiendaUsuarioDTO perfil(@RequestHeader("Authorization") String auth,@RequestBody TiendaUsuarioDTO d) {
        return service.actualizar(service.desdeToken(auth).getId(),d,true,auth);
    }
}
