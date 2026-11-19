package com.musica.ms_pedido.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pedido")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "subtotal_productos", nullable = false)
    private Double subtotalProductos;

    @Column(name = "costo_envio", nullable = false)
    private Double costoEnvio;

    @Column(name = "total_pago", nullable = false)
    private Double totalPago;

    @Column(name = "metodo_envio", nullable = false)
    private String metodoEnvio;

    @Column(name = "direccion_entrega", nullable = false, length = 500)
    private String direccionEntrega;

    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_pedido")
    private LocalDate fechaPedido;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles;

    // Snapshot de boleta utilizada por la tienda React.
    @Column(unique=true) private String codigo;
    @Column(unique=true) private String boletaNumero;
    private String clienteCorreo;
    private String clienteNombre;
    private String clienteRun;
    private String clienteTelefono;
    private String entregaRegion;
    private String entregaComuna;
    // Null para las compras antiguas. Única por cliente y solicitud para evitar pagos duplicados.
    @Column(name="clave_solicitud",unique=true,length=100) private String claveSolicitud;
    @Column(name="huella_solicitud",length=64) private String huellaSolicitud;
}
