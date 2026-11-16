package com.musica.ms_boletas.service;
import com.musica.ms_boletas.client.PedidoBoletaClient;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
@Service
public class BoletasService {
 private final PedidoBoletaClient pedidos;
 public BoletasService(PedidoBoletaClient pedidos){this.pedidos=pedidos;}
 public List<Map<String,Object>> todas(String jwt){return soloPagadas(pedidos.todas(jwt));}
 public List<Map<String,Object>> mias(String jwt){return soloPagadas(pedidos.mias(jwt));}
 public Map<String,Object> detalle(String jwt,String numero,String rol){
    List<Map<String,Object>> lista="ADMIN".equals(rol)?todas(jwt):mias(jwt);
    return lista.stream().filter(b->numero.equals(b.get("boletaNumero"))).findFirst()
      .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Boleta no encontrada"));
 }
 private List<Map<String,Object>> soloPagadas(List<Map<String,Object>> ordenes){
  return ordenes.stream().filter(p->"Pagada".equals(p.get("estado")) && p.get("boletaNumero")!=null).toList();
 }
}
