package com.musica.ms_reportes.controller;
import com.musica.ms_reportes.service.ReportesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/reportes") @Tag(name="Reportes de ventas y dashboard")
public class ReportesController {
 private final ReportesService s;
 public ReportesController(ReportesService s){this.s=s;}
 @GetMapping("/resumen") public Map<String,Object> resumen(@RequestHeader("Authorization") String jwt){return s.resumen(jwt);}
}
