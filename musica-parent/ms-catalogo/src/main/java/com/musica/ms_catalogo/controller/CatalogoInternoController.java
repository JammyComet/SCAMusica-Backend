package com.musica.ms_catalogo.controller;

import com.musica.ms_catalogo.model.Catalogo;
import com.musica.ms_catalogo.service.TiendaCatalogoService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;

/** Solo para ms-pedido, por la red Docker. No se publica en el Gateway ni Swagger. */
@RestController
@Hidden
@RequestMapping("/internal/catalogo")
public class CatalogoInternoController {
    private final TiendaCatalogoService catalogo;
    public CatalogoInternoController(TiendaCatalogoService catalogo) { this.catalogo=catalogo; }
    @GetMapping("/productos/{codigo}") public Catalogo producto(@PathVariable String codigo) { return catalogo.porCodigo(codigo); }
    @PostMapping("/reservar-lote/{clave}") public void reservar(@PathVariable String clave,@RequestBody List<Map<String,Object>> lineas) {
        catalogo.reservarLote(clave,lineas);
    }
}
