package com.musica.ms_inventario.controller;
import com.musica.ms_inventario.service.InventarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/inventario") @Tag(name="Inventario y stock crítico")
public class InventarioController {
 private final InventarioService s;
 public InventarioController(InventarioService s){this.s=s;}
 @GetMapping("/criticos") public List<Map<String,Object>> criticos(){return s.criticos();}
}
