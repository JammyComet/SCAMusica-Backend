package com.musica.ms_catalogo.controller;

import com.musica.ms_catalogo.model.Catalogo;
import com.musica.ms_catalogo.service.TiendaCatalogoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventario")
@Tag(name="Inventario y stock crítico")
public class InventarioController {
    private final TiendaCatalogoService catalogo;
    public InventarioController(TiendaCatalogoService catalogo) {this.catalogo=catalogo;}
    @GetMapping("/criticos") public List<Catalogo> criticos() {
        return catalogo.listar().stream()
          .filter(p -> p.getStock()!=null && p.getStockCritico()!=null && p.getStock()<=p.getStockCritico())
          .sorted(java.util.Comparator.comparing(Catalogo::getStock)).toList();
    }
}
