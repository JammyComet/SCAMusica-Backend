package com.musica.ms_pedido.controller;

import com.musica.ms_pedido.dto.TiendaOrdenDTO;
import com.musica.ms_pedido.service.TiendaOrdenService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/ordenes")
@Tag(name="Boletas de SCAMusica",description="Compras y boletas de React; precios y stock validados en servidor")
public class TiendaOrdenController {
    private final TiendaOrdenService s;
    public TiendaOrdenController(TiendaOrdenService s){this.s=s;}
    @GetMapping public List<Map<String,Object>> listar(){return s.listar();}
    @GetMapping("/mias") public List<Map<String,Object>> mias(@RequestAttribute("authUser") String user){return s.mias(user);}
    @GetMapping("/{codigo}") public Map<String,Object> detalle(@PathVariable String codigo,@RequestAttribute("authUser") String user,@RequestAttribute("authRole") String role){return s.obtener(codigo,user,role);}
    @PostMapping public ResponseEntity<Map<String,Object>> comprar(@RequestBody TiendaOrdenDTO pedido,@RequestAttribute("authUser") String user,@RequestAttribute("authId") Long id){return ResponseEntity.status(201).body(s.crear(pedido,user,id));}
}
