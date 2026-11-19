package com.musica.ms_pedido.controller;

import com.musica.ms_pedido.dto.TiendaOrdenDTO;
import com.musica.ms_pedido.service.TiendaOrdenService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.musica.ms_pedido.dto.PedidoRespuestaDoc;

@RestController
@RequestMapping("/api/v1/ordenes")
@Tag(name="Órdenes y compras",description="Compras del frontend; precios y stock validados en el servidor")
public class TiendaOrdenController {
    private final TiendaOrdenService s;
    public TiendaOrdenController(TiendaOrdenService s){this.s=s;}
    @Operation(summary="Listar órdenes para administrador y vendedor")
    @ApiResponse(responseCode="200", content=@Content(array=@ArraySchema(schema=@Schema(implementation=PedidoRespuestaDoc.class))))
    @GetMapping public List<Map<String,Object>> listar(){return s.listar();}
    @Operation(summary="Consultar las compras del cliente autenticado")
    @ApiResponse(responseCode="200", content=@Content(array=@ArraySchema(schema=@Schema(implementation=PedidoRespuestaDoc.class))))
    @GetMapping("/mias") public List<Map<String,Object>> mias(@RequestAttribute("authId") Long clienteId){return s.mias(clienteId);}
    @Operation(summary="Registrar una compra de la tienda")
    @ApiResponse(responseCode="201", content=@Content(schema=@Schema(implementation=PedidoRespuestaDoc.class)))
    @PostMapping public ResponseEntity<Map<String,Object>> comprar(
        @RequestBody TiendaOrdenDTO pedido,
        @RequestHeader("Idempotency-Key") @io.swagger.v3.oas.annotations.Parameter(description="UUID único por intento de compra. Se reutiliza al reintentar.",required=true) String clave,
        @RequestAttribute("authUser") String user,@RequestAttribute("authId") Long id){
        return ResponseEntity.status(201).body(s.crear(pedido,user,id,clave));
    }
}
