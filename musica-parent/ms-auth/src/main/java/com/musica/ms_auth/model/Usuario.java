package com.musica.ms_auth.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String rol;
    private Long clienteId;
    private LocalDate fechaRegistro;
    private String run;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String region;
    private String comuna;
    private String direccion;
    @jakarta.persistence.Column(columnDefinition="boolean default true")
    private boolean activo = true;
}