package com.musica.ms_auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO utilizado para iniciar sesión")
public class LoginRequestDTO {

    @Schema(
            description = "Nombre de usuario",
            example = "admin"
    )
    @NotBlank(message = "El username es obligatorio")
    private String username;

    @Schema(
            description = "Contraseña del usuario",
            example = "admin123"
    )
    @NotBlank(message = "El password es obligatorio")
    private String password;
}