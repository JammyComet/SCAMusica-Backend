package com.musica.ms_carrito.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.musica.ms_carrito.model.Carrito;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    @EntityGraph(attributePaths = "items")
    Optional<Carrito> findByClienteId(Long clienteId);
}
