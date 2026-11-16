package com.musica.ms_reportes.client;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
@FeignClient(name="ms-catalogo")
public interface ReportesProductoClient { @GetMapping("/api/v1/productos") List<Map<String,Object>> listar(); }
