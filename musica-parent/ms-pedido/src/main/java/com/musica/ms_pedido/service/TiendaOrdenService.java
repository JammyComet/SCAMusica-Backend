package com.musica.ms_pedido.service;

import com.musica.ms_pedido.client.CatalogoClient;
import com.musica.ms_pedido.dto.TiendaOrdenDTO;
import com.musica.ms_pedido.dto.TiendaProductoDTO;
import com.musica.ms_pedido.model.Pedido;
import com.musica.ms_pedido.model.DetallePedido;
import com.musica.ms_pedido.repository.PedidoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TiendaOrdenService {
    private final PedidoRepository repo;
    private final CatalogoClient catalogo;
    public TiendaOrdenService(PedidoRepository repo,CatalogoClient catalogo){this.repo=repo;this.catalogo=catalogo;}
    @Transactional(readOnly=true) public List<Map<String,Object>> listar(){return repo.findAll().stream().filter(p->p.getCodigo()!=null).map(this::respuesta).toList();}
    @Transactional(readOnly=true) public List<Map<String,Object>> mias(String correo){return repo.findAll().stream().filter(p->correo.equalsIgnoreCase(p.getClienteCorreo()==null?"":p.getClienteCorreo())).map(this::respuesta).toList();}
    @Transactional(readOnly=true) public Map<String,Object> obtener(String codigo,String correo,String rol){
        Pedido p=repo.findAll().stream().filter(o->codigo.equals(o.getCodigo())).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if("CLIENTE".equals(rol) && !correo.equalsIgnoreCase(p.getClienteCorreo())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return respuesta(p);
    }
    @Transactional
    public Map<String,Object> crear(TiendaOrdenDTO d,String correo,Long authId){
        if(d==null || d.cliente()==null || d.entrega()==null || d.productos()==null || d.productos().isEmpty() ||
            d.entrega().region()==null || d.entrega().comuna()==null || d.entrega().direccion()==null || d.entrega().direccion().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Dirección y productos obligatorios");
        if(!correo.equalsIgnoreCase(d.cliente().correo())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No puedes comprar para otra cuenta");
        Pedido p=new Pedido();p.setClienteId(authId);p.setClienteCorreo(correo);
        p.setClienteRun(d.cliente().run());p.setClienteNombre(d.cliente().nombre());p.setClienteTelefono(d.cliente().telefono());
        p.setEntregaRegion(d.entrega().region());p.setEntregaComuna(d.entrega().comuna());
        p.setMetodoEnvio("DESPACHO_DOMICILIO");p.setDireccionEntrega(d.entrega().direccion());
        p.setFechaPedido(LocalDate.now());p.setEstado("Pagada");p.setCostoEnvio(0.0);
        p.setDetalles(new ArrayList<>());
        double total=0;
        for(TiendaOrdenDTO.Linea l:d.productos()){
            if(l.codigo()==null || l.cantidad()<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cantidad inválida");
            TiendaProductoDTO producto=catalogo.obtenerProductoTienda(l.codigo());
            if(producto==null || !"Activo".equals(producto.getEstado()) || producto.getStock()==null || producto.getStock()<l.cantidad()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Producto sin stock: "+l.codigo());
            int descuento=producto.getDescuento()==null?0:Math.max(0,Math.min(100,producto.getDescuento()));
            double precio=Math.round(producto.getPrecio()*(100-descuento)/100.0);
            DetallePedido det=new DetallePedido();det.setPedido(p);det.setProductoId(producto.getId());
            det.setCodigo(l.codigo());det.setNombre(producto.getNombre());det.setArtista(producto.getArtista());
            det.setPrecioOriginal(producto.getPrecio().doubleValue());det.setDescuento(descuento);
            det.setPrecioUnitario(precio);det.setCantidad(l.cantidad());det.setSubtotal(precio*l.cantidad());
            p.getDetalles().add(det);total+=det.getSubtotal();
        }
        p.setSubtotalProductos(total);p.setTotalPago(total);
        Pedido guardado=repo.saveAndFlush(p);
        guardado.setCodigo("ORD-"+String.format("%04d",guardado.getId()));
        guardado.setBoletaNumero("BOL-"+String.format("%04d",guardado.getId()));
        // Se comprueba y descuenta el stock dentro de una transacción de ms-catalogo.
        // Si ese servicio rechaza la compra, el pedido local se revierte.
        catalogo.descontarStockTienda(d.productos(),java.util.Objects.requireNonNull(System.getenv("INTERNAL_KEY"),"Configura INTERNAL_KEY"));
        return respuesta(repo.save(guardado));
    }
    private Map<String,Object> respuesta(Pedido p){
        Map<String,Object> r=new LinkedHashMap<>();
        r.put("codigo",p.getCodigo());r.put("boletaNumero",p.getBoletaNumero());
        r.put("fecha",p.getFechaPedido()==null?null:p.getFechaPedido().toString());
        Map<String,Object> c=new LinkedHashMap<>();c.put("run",p.getClienteRun());c.put("nombre",p.getClienteNombre());c.put("correo",p.getClienteCorreo());
        c.put("telefono",p.getClienteTelefono());c.put("region",p.getEntregaRegion());c.put("comuna",p.getEntregaComuna());c.put("direccion",p.getDireccionEntrega());
        r.put("cliente",c);
        r.put("entrega",Map.of("region",p.getEntregaRegion()==null?"":p.getEntregaRegion(),"comuna",p.getEntregaComuna()==null?"":p.getEntregaComuna(),"direccion",p.getDireccionEntrega()));
        List<Map<String,Object>> productos=new ArrayList<>();
        if(p.getDetalles()!=null) for(DetallePedido l:p.getDetalles()){
            Map<String,Object> item=new LinkedHashMap<>();item.put("codigo",l.getCodigo());item.put("nombre",l.getNombre());item.put("artista",l.getArtista());
            item.put("precioOriginal",l.getPrecioOriginal());item.put("descuento",l.getDescuento());item.put("precio",l.getPrecioUnitario());item.put("cantidad",l.getCantidad());item.put("subtotal",l.getSubtotal());
            productos.add(item);
        }
        r.put("productos",productos);r.put("total",p.getTotalPago());r.put("estado",p.getEstado());return r;
    }
}
