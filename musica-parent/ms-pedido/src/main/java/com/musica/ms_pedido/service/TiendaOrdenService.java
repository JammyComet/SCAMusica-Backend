package com.musica.ms_pedido.service;

import com.musica.ms_pedido.client.CatalogoClient;
import com.musica.ms_pedido.dto.TiendaOrdenDTO;
import com.musica.ms_pedido.dto.TiendaProductoDTO;
import com.musica.ms_pedido.model.Pedido;
import com.musica.ms_pedido.model.DetallePedido;
import com.musica.ms_pedido.repository.PedidoRepository;
import feign.FeignException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
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
    private final PedidoTransacciones transacciones;

    public TiendaOrdenService(PedidoRepository repo, CatalogoClient catalogo, PedidoTransacciones transacciones) {
        this.repo=repo; this.catalogo=catalogo; this.transacciones=transacciones;
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> listar() {
        return repo.findAll().stream().filter(p->p.getCodigo()!=null && "Pagada".equals(p.getEstado())).map(this::respuesta).toList();
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> mias(Long clienteId) {
        // El correo se puede reutilizar si una cuenta se elimina. El ID nunca:
        // las boletas de otro usuario no deben aparecer en una cuenta nueva.
        return repo.findByClienteIdOrderByIdDesc(clienteId).stream()
          .filter(p->"Pagada".equals(p.getEstado()))
          .map(this::respuesta).toList();
    }

    /** Saga recuperable: primero se confirma un borrador en db_pedido; Catálogo reserva
     * stock de forma idempotente en db_catalogo; finalmente se marca PAGADA.
     * Si se cae el proceso entre ambos commits, la tarea de conciliación recupera
     * el pedido pendiente. Dos POST con la misma clave nunca descuentan dos veces.
     */
    public Map<String,Object> crear(TiendaOrdenDTO d,String correo,Long authId,String claveCliente){
        validar(d,correo);
        if(claveCliente==null || !claveCliente.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-8][0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Idempotency-Key debe ser un UUID");
        String clave=authId+"-"+claveCliente.toLowerCase(java.util.Locale.ROOT);
        String huella=huella(d);
        Pedido p=transacciones.porClave(clave).orElse(null);
        if(p!=null){
            if(!huella.equals(p.getHuellaSolicitud()))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"La solicitud ya se utilizó para otra compra");
        } else {
            Pedido borrador=construirPedido(d,correo,authId);
            borrador.setClaveSolicitud(clave);
            borrador.setHuellaSolicitud(huella);
            try {p=transacciones.crearPendiente(borrador);}
            catch(org.springframework.dao.DataIntegrityViolationException ex){
                // Otro POST concurrente con la misma clave ganó la restricción UNIQUE.
                p=transacciones.porClave(clave).orElseThrow(()->new ResponseStatusException(HttpStatus.CONFLICT,"Compra en curso; reintenta"));
                if(!huella.equals(p.getHuellaSolicitud()))
                    throw new ResponseStatusException(HttpStatus.CONFLICT,"La solicitud ya se utilizó para otra compra");
            }
        }
        if("Pagada".equals(p.getEstado()))return respuesta(transacciones.porId(p.getId()));
        if("Rechazada".equals(p.getEstado()))throw new ResponseStatusException(HttpStatus.CONFLICT,"La compra anterior fue rechazada; revisa el carrito");
        completar(p);
        return respuesta(transacciones.porId(p.getId()));
    }

    private void validar(TiendaOrdenDTO d,String correo){
        if(d==null || d.cliente()==null || d.entrega()==null || d.productos()==null || d.productos().isEmpty() ||
           d.entrega().region()==null || d.entrega().region().isBlank() || d.entrega().comuna()==null || d.entrega().comuna().isBlank() ||
           d.entrega().direccion()==null || d.entrega().direccion().isBlank() || d.cliente().correo()==null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Dirección y productos obligatorios");
        if(!correo.equalsIgnoreCase(d.cliente().correo()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No puedes comprar para otra cuenta");
        // No permitir códigos duplicados: facilita comprobar que la reserva y el pedido coinciden.
        var codigos=new java.util.HashSet<String>();
        for(TiendaOrdenDTO.Linea l:d.productos()) {
            if(l==null || l.codigo()==null || l.codigo().isBlank() || l.cantidad()<=0 || !codigos.add(l.codigo()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Producto repetido o cantidad inválida");
        }
    }
    private String huella(TiendaOrdenDTO d){
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(d.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        }catch(java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
    }
    private Pedido construirPedido(TiendaOrdenDTO d,String correo,Long authId){
        Pedido p=new Pedido();p.setClienteId(authId);p.setClienteCorreo(correo);
        p.setClienteRun(d.cliente().run());p.setClienteNombre(d.cliente().nombre());p.setClienteTelefono(d.cliente().telefono());
        p.setEntregaRegion(d.entrega().region());p.setEntregaComuna(d.entrega().comuna());
        p.setMetodoEnvio("DESPACHO_DOMICILIO");p.setDireccionEntrega(d.entrega().direccion());
        p.setFechaPedido(LocalDate.now());p.setEstado("Pendiente");p.setCostoEnvio(0.0);
        p.setDetalles(new ArrayList<>());
        double total=0;
        for(TiendaOrdenDTO.Linea l:d.productos()){
            TiendaProductoDTO producto=catalogo.obtenerProductoTienda(l.codigo(), claveInterna());
            if(producto==null || !"Activo".equals(producto.getEstado()) || producto.getStock()==null || producto.getStock()<l.cantidad())
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Producto sin stock: "+l.codigo());
            int descuento=producto.getDescuento()==null?0:Math.max(0,Math.min(100,producto.getDescuento()));
            double precio=Math.round(producto.getPrecio()*(100-descuento)/100.0);
            DetallePedido det=new DetallePedido();det.setPedido(p);det.setProductoId(producto.getId());
            det.setCodigo(l.codigo());det.setNombre(producto.getNombre());det.setArtista(producto.getArtista());
            det.setPrecioOriginal(producto.getPrecio().doubleValue());det.setDescuento(descuento);
            det.setPrecioUnitario(precio);det.setCantidad(l.cantidad());det.setSubtotal(precio*l.cantidad());
            p.getDetalles().add(det);total+=det.getSubtotal();
        }
        p.setSubtotalProductos(total);p.setTotalPago(total);
        return p;
    }
    private String claveInterna(){return java.util.Objects.requireNonNull(System.getProperty("scamusic.internal.key",System.getenv("INTERNAL_KEY")),"Configura INTERNAL_KEY");}
    private void completar(Pedido p){
        List<TiendaOrdenDTO.Linea> lineas=p.getDetalles().stream()
            .map(l->new TiendaOrdenDTO.Linea(l.getCodigo(),l.getCantidad())).toList();
        try {
            catalogo.reservarStockTienda(p.getClaveSolicitud(),lineas,claveInterna());
        }catch(FeignException ex){
            if(ex.status()==400 || ex.status()==404 || ex.status()==409){
                transacciones.rechazar(p.getId());
                throw new ResponseStatusException(HttpStatus.CONFLICT,"No se pudo completar la compra por disponibilidad de productos",ex);
            }
            // El servicio pudo descontar el stock antes de cortar la conexión.
            // No liberar ni duplicar aquí: recuperar con la MISMA clave después.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No pudimos confirmar la compra. Revisa tus boletas y vuelve a intentar",ex);
        }
        transacciones.confirmar(p.getId());
    }

    /** Reconcilia operaciones que quedaron entre dos transacciones por caídas de red/servicios. */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 15000, initialDelay = 30000)
    public void conciliarPendientes(){
        for(Pedido p:transacciones.pendientes()){
            try {
                Pedido completo=transacciones.porId(p.getId());
                if("Pendiente".equals(completo.getEstado())) completar(completo);
            }catch(Exception ex){
                // Reintento posterior; no hacer nueva reserva con otra clave.
                System.err.println("Compra pendiente "+p.getId()+": "+ex.getClass().getSimpleName());
            }
        }
    }

    private Map<String,Object> respuesta(Pedido p){
        Map<String,Object> r=new LinkedHashMap<>();
        r.put("codigo",p.getCodigo());r.put("boletaNumero",p.getBoletaNumero());
        r.put("fecha",p.getFechaPedido()==null?null:p.getFechaPedido().toString());
        Map<String,Object> c=new LinkedHashMap<>();c.put("run",p.getClienteRun());c.put("nombre",p.getClienteNombre());c.put("correo",p.getClienteCorreo());
        c.put("telefono",p.getClienteTelefono());c.put("region",p.getEntregaRegion());c.put("comuna",p.getEntregaComuna());c.put("direccion",p.getDireccionEntrega());
        r.put("cliente",c);
        // Map.of no acepta valores null: pedidos historicos o pruebas pueden no tener domicilio.
        // Mantenemos las claves que React necesita, usando cadena vacia en campos opcionales.
        Map<String,Object> entrega=new LinkedHashMap<>();
        entrega.put("region",p.getEntregaRegion()==null?"":p.getEntregaRegion());
        entrega.put("comuna",p.getEntregaComuna()==null?"":p.getEntregaComuna());
        entrega.put("direccion",p.getDireccionEntrega()==null?"":p.getDireccionEntrega());
        r.put("entrega",entrega);
        List<Map<String,Object>> productos=new ArrayList<>();
        if(p.getDetalles()!=null) for(DetallePedido l:p.getDetalles()){
            Map<String,Object> item=new LinkedHashMap<>();item.put("codigo",l.getCodigo());item.put("nombre",l.getNombre());item.put("artista",l.getArtista());
            item.put("precioOriginal",l.getPrecioOriginal());item.put("descuento",l.getDescuento());item.put("precio",l.getPrecioUnitario());item.put("cantidad",l.getCantidad());item.put("subtotal",l.getSubtotal());
            productos.add(item);
        }
        r.put("productos",productos);r.put("total",p.getTotalPago());r.put("estado",p.getEstado());return r;
    }
}
