package com.musica.ms_categorias.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
@FeignClient(name="ms-catalogo")
public interface ProductoClient { @GetMapping("/api/v1/productos") List<Map<String,Object>> listar(); }
