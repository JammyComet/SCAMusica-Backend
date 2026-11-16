package com.musica.ms_inventario.service;
import com.musica.ms_inventario.client.ProductoInventarioClient;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
@Service
public class InventarioService {
 private final ProductoInventarioClient catalogo;
 public InventarioService(ProductoInventarioClient catalogo){this.catalogo=catalogo;}
 public List<Map<String,Object>> criticos(){return catalogo.listar().stream()
  .filter(p->p.get("stockCritico") instanceof Number && p.get("stock") instanceof Number)
  .filter(p->((Number)p.get("stock")).doubleValue()<=((Number)p.get("stockCritico")).doubleValue())
  .sorted((a,b)->Double.compare(((Number)a.get("stock")).doubleValue(),((Number)b.get("stock")).doubleValue())).toList();}
}
