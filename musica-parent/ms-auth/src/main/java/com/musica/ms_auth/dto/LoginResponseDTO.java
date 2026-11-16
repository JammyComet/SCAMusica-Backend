package com.musica.ms_auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Respuesta generada al iniciar sesión")
public class LoginResponseDTO {
    private String token;
    private long expiresIn = 300L; // vida del JWT de acceso, en segundos
    private Long usuarioId;
    private String username;
    private String rol;
    private Long clienteId;

    public LoginResponseDTO(String token) {
        this.token = token;
    }

    public LoginResponseDTO(String token, Long usuarioId, String username, String rol) {
        this(token, usuarioId, username, rol, null);
    }

    public LoginResponseDTO(String token, Long usuarioId, String username, String rol, Long clienteId) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.username = username;
        this.rol = rol;
        this.clienteId = clienteId;
    }
}
