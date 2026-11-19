package com.musica.ms_reportes.client;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
@FeignClient(name="ms-auth")
public interface ReportesUsuarioClient { @GetMapping("/api/v1/admin/usuarios") List<Map<String,Object>> listar(@RequestHeader("Authorization") String jwt); }
