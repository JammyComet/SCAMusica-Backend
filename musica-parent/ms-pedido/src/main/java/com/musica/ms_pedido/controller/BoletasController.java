package com.musica.ms_pedido.controller;

import com.musica.ms_pedido.service.TiendaOrdenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.musica.ms_pedido.dto.PedidoRespuestaDoc;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name="Boletas pagadas")
public class BoletasController {
    private final TiendaOrdenService ordenes;
    public BoletasController(TiendaOrdenService ordenes) { this.ordenes=ordenes; }
    @Operation(summary="Consultar las boletas de las compras pagadas (administrador)")
    @ApiResponse(responseCode="200", content=@Content(array=@ArraySchema(schema=@Schema(implementation=PedidoRespuestaDoc.class))))
    @GetMapping public List<Map<String,Object>> todas() { return pagadas(ordenes.listar()); }
    @Operation(summary="Consultar las boletas del cliente autenticado")
    @ApiResponse(responseCode="200", content=@Content(array=@ArraySchema(schema=@Schema(implementation=PedidoRespuestaDoc.class))))
    @GetMapping("/mias") public List<Map<String,Object>> mias(@RequestAttribute("authId") Long clienteId) {
        return pagadas(ordenes.mias(clienteId));
    }
    private List<Map<String,Object>> pagadas(List<Map<String,Object>> lista) {
        return lista.stream().filter(p -> "Pagada".equals(p.get("estado")) && p.get("boletaNumero")!=null).toList();
    }
}
