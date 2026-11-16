package com.musica.ms_notificacion.controller;

import com.musica.ms_notificacion.model.Notificacion;
import com.musica.ms_notificacion.service.TiendaContactoService;
import com.musica.ms_notificacion.dto.TiendaContactoDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/contactos")
@Tag(name="Contacto",description="Mensajes enviados por el formulario de React")
public class TiendaContactoController {
    private final TiendaContactoService service;
    public TiendaContactoController(TiendaContactoService service){this.service=service;}
    @PostMapping public ResponseEntity<Void> enviar(@RequestBody TiendaContactoDTO c){service.enviar(c);return ResponseEntity.status(201).build();}
    @GetMapping public List<Notificacion> listar(){return service.listar();}
}
