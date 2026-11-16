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
}
