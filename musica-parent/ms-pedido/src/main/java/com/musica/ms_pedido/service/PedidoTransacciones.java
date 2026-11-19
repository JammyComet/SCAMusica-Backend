package com.musica.ms_pedido.service;

import com.musica.ms_pedido.model.Pedido;
import com.musica.ms_pedido.repository.PedidoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fronteras explícitas de transacciones de PEDIDO (no incluyen llamadas HTTP). */
@Service
public class PedidoTransacciones {
    private final PedidoRepository repo;
    public PedidoTransacciones(PedidoRepository repo) { this.repo=repo; }

    @Transactional
    public Pedido crearPendiente(Pedido pedido) {
        pedido.setEstado("Pendiente");
        Pedido creado=repo.saveAndFlush(pedido);
        creado.setCodigo("ORD-"+String.format("%04d",creado.getId()));
        creado.setBoletaNumero("BOL-"+String.format("%04d",creado.getId()));
        return repo.saveAndFlush(creado);
    }
    @Transactional(readOnly=true)
    public Optional<Pedido> porClave(String clave) { return repo.findByClaveSolicitud(clave); }
    @Transactional(readOnly=true)
    public Pedido porId(Long id) { return repo.findById(id).orElseThrow(); }
    @Transactional
    public void confirmar(Long id) {
        Pedido p=repo.bloquearPorId(id).orElseThrow();
        if("Pendiente".equals(p.getEstado())) {p.setEstado("Pagada");repo.saveAndFlush(p);}
    }
    @Transactional
    public void rechazar(Long id) {
        Pedido p=repo.bloquearPorId(id).orElseThrow();
        if("Pendiente".equals(p.getEstado())) {p.setEstado("Rechazada");repo.saveAndFlush(p);}
    }
    @Transactional(readOnly=true)
    public List<Pedido> pendientes(){return repo.findByEstadoAndClaveSolicitudIsNotNull("Pendiente");}
}
