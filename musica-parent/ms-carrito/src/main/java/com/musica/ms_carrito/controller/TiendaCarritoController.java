package com.musica.ms_carrito.controller;

import com.musica.ms_carrito.service.TiendaCarritoService;
import com.musica.ms_carrito.dto.TiendaCarritoItemDTO;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/carritos")
@Tag(name="Carrito React",description="Carrito de cliente autenticado persistido en MySQL")
public class TiendaCarritoController {
    private final TiendaCarritoService service;
    public TiendaCarritoController(TiendaCarritoService service){this.service=service;}
    @GetMapping public List<TiendaCarritoItemDTO> obtener(@RequestAttribute("authId") Long usuarioId){return service.obtener(usuarioId);}
    @PutMapping public List<TiendaCarritoItemDTO> guardar(@RequestAttribute("authId") Long usuarioId,@RequestBody List<TiendaCarritoItemDTO> datos){return service.guardar(usuarioId,datos);}
}
