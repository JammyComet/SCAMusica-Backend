package com.musica.ms_pedido.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musica.ms_pedido.model.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
}