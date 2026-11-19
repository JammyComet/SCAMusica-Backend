package com.musica.ms_auth.controller;

import com.musica.ms_auth.service.ContactoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Formulario Contacto del frontend. No existe una pantalla de lectura de mensajes en React. */
@RestController
@RequestMapping("/api/v1/contactos")
@Tag(name="Contacto", description="Envío del formulario Contacto de la tienda")
public class ContactoController {
    private final ContactoService contactos;
    public ContactoController(ContactoService contactos) { this.contactos = contactos; }

    public record Mensaje(
        @Schema(description="Nombre de quien envía el mensaje", example="Ana Pérez")
        @NotBlank @Size(max=100) String nombre,
        @Schema(description="Correo de contacto", example="ana@example.com")
        @NotBlank @Email @Size(max=254) String correo,
        @Schema(description="Consulta escrita en la tienda", example="¿Tienen disponible este disco?")
        @NotBlank @Size(max=500) String comentario
    ) {}

    @Operation(summary="Enviar consulta desde la página Contacto")
    @ApiResponse(responseCode="201", description="Consulta recibida y guardada; sin cuerpo de respuesta")
    @ApiResponse(responseCode="400", description="Datos incompletos o inválidos")
    @PostMapping
    public ResponseEntity<Void> enviar(@Valid @RequestBody Mensaje m) {
        contactos.guardar(m.nombre(), m.correo(), m.comentario());
        return ResponseEntity.status(201).build();
    }
}
