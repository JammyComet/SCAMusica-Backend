package com.musica.ms_auth.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class TiendaUsuarioDTO {
    private Long id;
    private String correo;
    private String contrasena; // Escritura solamente: NUNCA devolver el hash al frontend.
    private String contrasenaActual;
    private String rol;
    private String run;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String region;
    private String comuna;
    private String direccion;
    private Boolean activo;
}
