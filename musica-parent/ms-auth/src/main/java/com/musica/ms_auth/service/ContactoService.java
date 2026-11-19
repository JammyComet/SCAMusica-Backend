package com.musica.ms_auth.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Guarda solo los campos enviados por el formulario React, en db_usuario. */
@Service
public class ContactoService {
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");
    private final JdbcTemplate jdbc;
    public ContactoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private void prepararTabla() {
        // No borra la tabla histórica de db_notificacion: una instalación antigua conserva sus datos.
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS contacto (
                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(100) NOT NULL,
                correo VARCHAR(254) NOT NULL,
                comentario VARCHAR(500) NOT NULL,
                fecha DATE NOT NULL
            )
            """);
    }
    public void guardar(String nombre, String correo, String comentario) {
        if(nombre==null || nombre.isBlank() || nombre.length()>100 ||
           correo==null || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || correo.length()>254 ||
           comentario==null || comentario.isBlank() || comentario.length()>500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Mensaje inválido");
        prepararTabla();
        jdbc.update("INSERT INTO contacto (nombre, correo, comentario, fecha) VALUES (?, ?, ?, ?)",
            nombre.trim(),correo.trim().toLowerCase(),comentario.trim(),Date.valueOf(LocalDate.now(ZONA)));
    }
}
