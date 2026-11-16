package com.musica.ms_catalogo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "catalogo")
@Data
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String artista;
    private String formato;
    private String genero;
    private int precio;
    // Campos que ya usaba el frontend React, ahora almacenados en MySQL.
    @jakarta.persistence.Column(unique=true)
    private String codigo;
    @jakarta.persistence.Column(length=2000)
    private String descripcion;
    private Integer stock;
    private Integer stockCritico;
    private Long categoriaId;
    private Integer anio;
    private String imagen;
    private String estado;
    private Integer descuento;
}
