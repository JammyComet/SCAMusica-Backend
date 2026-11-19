package com.musica.ms_reportes.controller;
import com.musica.ms_reportes.service.ReportesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import com.musica.ms_reportes.dto.ResumenReporteDoc;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/reportes") @Tag(name="Reportes de ventas y dashboard")
public class ReportesController {
 private final ReportesService s;
 public ReportesController(ReportesService s){this.s=s;}
 @Operation(summary="Indicadores del panel y reportes del administrador")
 @ApiResponse(responseCode="200", content=@Content(schema=@Schema(implementation=ResumenReporteDoc.class)))
 @GetMapping("/resumen") public Map<String,Object> resumen(@RequestHeader("Authorization") String jwt){return s.resumen(jwt);}
}
