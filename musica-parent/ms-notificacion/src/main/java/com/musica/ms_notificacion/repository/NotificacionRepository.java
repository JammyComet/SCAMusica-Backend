package com.musica.ms_notificacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musica.ms_notificacion.model.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByClienteId(Long clienteId);

    List<Notificacion> findByPedidoId(Long pedidoId);

    List<Notificacion> findByEstadoIgnoreCase(String estado);
}