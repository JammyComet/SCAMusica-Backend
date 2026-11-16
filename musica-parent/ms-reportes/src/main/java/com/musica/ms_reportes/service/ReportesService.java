package com.musica.ms_reportes.service;
import com.musica.ms_reportes.client.ReportesPedidoClient;
import com.musica.ms_reportes.client.ReportesProductoClient;
import com.musica.ms_reportes.client.ReportesUsuarioClient;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;
@Service
public class ReportesService {
 private final ReportesPedidoClient pedidos;
 private final ReportesProductoClient productos;
 private final ReportesUsuarioClient usuarios;
 public ReportesService(ReportesPedidoClient pedidos,ReportesProductoClient productos,ReportesUsuarioClient usuarios){this.pedidos=pedidos;this.productos=productos;this.usuarios=usuarios;}
 public Map<String,Object> resumen(String jwt){
   List<Map<String,Object>> ordenes=pedidos.listar(jwt).stream().filter(o->"Pagada".equals(o.get("estado"))).toList();
   List<Map<String,Object>> catalogo=productos.listar();
   long nUsuarios=usuarios.listar(jwt).size();
   double ventas=ordenes.stream().mapToDouble(o->num(o.get("total"))).sum();
   long unidades=ordenes.stream().flatMap(o->detalle(o).stream()).mapToLong(i->(long)num(i.get("cantidad"))).sum();
   long activos=catalogo.stream().filter(p->"Activo".equals(p.get("estado"))).count();
   long criticos=catalogo.stream().filter(p->p.get("stock") instanceof Number && p.get("stockCritico") instanceof Number)
    .filter(p->num(p.get("stock"))<=num(p.get("stockCritico"))).count();
   Map<String,Object> resumen=new LinkedHashMap<>();
   resumen.put("ventasTotales",ventas);resumen.put("ordenesPagadas",ordenes.size());
   resumen.put("unidadesVendidas",unidades);resumen.put("productosActivos",activos);
   resumen.put("stockCritico",criticos);resumen.put("usuarios",nUsuarios);
   return resumen;
 }
 @SuppressWarnings("unchecked") private List<Map<String,Object>> detalle(Map<String,Object> o){
    Object d=o.get("productos");return d instanceof List<?> ? (List<Map<String,Object>>)d : List.of();
 }
 private double num(Object value){return value instanceof Number n?n.doubleValue():0.0;}
}
