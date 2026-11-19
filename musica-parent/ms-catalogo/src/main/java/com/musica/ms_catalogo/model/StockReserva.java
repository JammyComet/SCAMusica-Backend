package com.musica.ms_catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Clave idempotente de un descuento: se confirma junto con el stock en UNA transacción local. */
@Entity
@Table(name = "reservas_stock")
public class StockReserva {
    @Id @Column(length = 100) private String clave;
    @Column(nullable = false, length = 2048) private String lineas;
    @Column(nullable = false) private Instant creadaEn;
    protected StockReserva() {}
    public StockReserva(String clave, String lineas) {
        this.clave = clave; this.lineas = lineas; this.creadaEn = Instant.now();
    }
    public String getClave() { return clave; }
    public String getLineas() { return lineas; }
}
