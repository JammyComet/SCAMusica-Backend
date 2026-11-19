package com.musica.ms_pedido.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.musica.ms_pedido.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Override
    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findAll();

    @Override
    @EntityGraph(attributePaths = "detalles")
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = "detalles")
    List<Pedido> findByClienteIdOrderByIdDesc(Long clienteId);

    @EntityGraph(attributePaths = "detalles")
    Optional<Pedido> findByClaveSolicitud(String claveSolicitud);

    List<Pedido> findByEstadoAndClaveSolicitudIsNotNull(String estado);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> bloquearPorId(@org.springframework.data.repository.query.Param("id") Long id);
}
