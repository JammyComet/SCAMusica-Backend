package com.musica.ms_boletas.controller;
import com.musica.ms_boletas.service.BoletasService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/boletas") @Tag(name="Boletas pagadas")
public class BoletasController {
 private final BoletasService s;
 public BoletasController(BoletasService s){this.s=s;}
 @GetMapping public List<Map<String,Object>> todas(@RequestHeader("Authorization") String jwt){return s.todas(jwt);}
 @GetMapping("/mias") public List<Map<String,Object>> mias(@RequestHeader("Authorization") String jwt){return s.mias(jwt);}
 @GetMapping("/{numero}") public Map<String,Object> detalle(@RequestHeader("Authorization") String jwt,@PathVariable String numero,@RequestAttribute("authRole") String rol){return s.detalle(jwt,numero,rol);}
}
