package com.musica.ms_reportes.client;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
@FeignClient(name="ms-pedido")
public interface ReportesPedidoClient {
 @GetMapping("/api/v1/ordenes") List<Map<String,Object>> listar(@RequestHeader("Authorization") String jwt);
}
