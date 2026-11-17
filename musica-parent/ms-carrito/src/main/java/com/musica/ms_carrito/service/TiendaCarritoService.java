package com.musica.ms_carrito.service;

import com.musica.ms_carrito.dto.TiendaCarritoItemDTO;
import com.musica.ms_carrito.model.Carrito;
import com.musica.ms_carrito.model.CarritoItem;
import com.musica.ms_carrito.repository.CarritoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TiendaCarritoService {
    private final CarritoRepository repo;
    public TiendaCarritoService(CarritoRepository repo){this.repo=repo;}

    @Transactional(readOnly=true)
    public List<TiendaCarritoItemDTO> obtener(Long usuarioId){
        // Compatible con los carritos creados antes de retirar ms-cliente.
        return repo.findByClienteId(-usuarioId)
            .map(c->c.getItems().stream().map(i->new TiendaCarritoItemDTO(i.getProductoId(),i.getCantidad())).toList())
            .orElse(List.of());
    }

    @Transactional
    public List<TiendaCarritoItemDTO> guardar(Long usuarioId,List<TiendaCarritoItemDTO> datos){
        if(datos==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Carrito inválido");
        var identificadores=new HashSet<Long>();
        for(TiendaCarritoItemDTO i:datos){
            if(i==null || i.productoId()==null || i.cantidad()==null || i.cantidad()<=0 || !identificadores.add(i.productoId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Producto o cantidad inválidos en carrito");
        }
        Carrito c=repo.findByClienteId(-usuarioId).orElseGet(()->{
            var nuevo=new Carrito();nuevo.setClienteId(-usuarioId);nuevo.setItems(new ArrayList<>());return nuevo;
        });
        // Reutilizar las filas existentes. Vaciar toda la colección e insertar
        // de nuevo puede colisionar con uk_carrito_producto (carrito_id, producto_id)
        // durante el flush de Hibernate al aumentar una cantidad.
        Map<Long,CarritoItem> existentes = new HashMap<>();
        for(CarritoItem item : c.getItems()) existentes.put(item.getProductoId(), item);
        c.getItems().removeIf(item -> !identificadores.contains(item.getProductoId()));
        for(TiendaCarritoItemDTO i : datos){
            CarritoItem item = existentes.get(i.productoId());
            if(item == null){
                item = new CarritoItem();
                item.setCarrito(c);
                item.setProductoId(i.productoId());
                c.getItems().add(item);
            }
            item.setCantidad(i.cantidad());
        }
        c.setFechaActualizacion(LocalDateTime.now());repo.save(c);
        return datos;
    }
}
