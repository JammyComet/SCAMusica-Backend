package com.musica.ms_catalogo.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name="ms-categorias")
public interface CategoriasClient {
 @GetMapping("/api/v1/categorias/{id}") java.util.Map<String,Object> porId(@PathVariable("id") Long id);
 default boolean existe(Long id){try{return porId(id)!=null;}catch(feign.FeignException.NotFound e){return false;}}
}
