package com.musica.ms_catalogo.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
@FeignClient(name="ms-categorias")
public interface CategoriasClient {
 @GetMapping("/internal/categorias/{id}") java.util.Map<String,Object> porId(
    @PathVariable("id") Long id, @RequestHeader("X-Internal-Key") String internalKey);
 default boolean existe(Long id){try{return porId(id,java.util.Objects.requireNonNull(System.getenv("INTERNAL_KEY"),"Configura INTERNAL_KEY"))!=null;}catch(feign.FeignException.NotFound e){return false;}}
}
