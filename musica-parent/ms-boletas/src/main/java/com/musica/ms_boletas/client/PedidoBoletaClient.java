package com.musica.ms_boletas.client;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
@FeignClient(name="ms-pedido")
public interface PedidoBoletaClient {
 @GetMapping("/api/v1/ordenes") List<Map<String,Object>> todas(@RequestHeader("Authorization") String jwt);
 @GetMapping("/api/v1/ordenes/mias") List<Map<String,Object>> mias(@RequestHeader("Authorization") String jwt);
}
