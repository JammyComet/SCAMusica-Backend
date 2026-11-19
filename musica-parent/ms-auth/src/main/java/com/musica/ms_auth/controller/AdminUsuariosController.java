package com.musica.ms_auth.controller;

import com.musica.ms_auth.dto.TiendaUsuarioDTO;
import com.musica.ms_auth.service.TiendaUsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Gestión de usuarios sin un servicio intermediario. */
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@Tag(name="Administración de usuarios")
public class AdminUsuariosController {
    private final TiendaUsuarioService usuarios;
    public AdminUsuariosController(TiendaUsuarioService usuarios) { this.usuarios = usuarios; }
    @GetMapping public List<TiendaUsuarioDTO> listar() { return usuarios.listar(); }
    @PostMapping public ResponseEntity<TiendaUsuarioDTO> crear(@RequestBody TiendaUsuarioDTO usuario) {
        return ResponseEntity.status(201).body(usuarios.crear(usuario, false));
    }
    @PutMapping("/{id}") public TiendaUsuarioDTO editar(@PathVariable Long id, @RequestBody TiendaUsuarioDTO usuario) {
        return usuarios.actualizar(id, usuario, false, null);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarios.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
