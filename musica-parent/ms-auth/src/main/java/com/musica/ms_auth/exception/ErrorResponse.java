package com.musica.ms_auth.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Estructura estándar de respuesta de error utilizada por el microservicio")
public class ErrorResponse {

    @Schema(description = "Fecha y hora en que ocurrió el error", example = "2026-06-28T14:30:00")
    private String timestamp;

    @Schema(description = "Código HTTP devuelto", example = "404")
    private Integer status;

    @Schema(description = "Tipo o categoría del error", example = "Recurso no encontrado")
    private String error;

    @Schema(description = "Mensaje técnico o de negocio asociado al error", example = "El registro solicitado no existe")
    private String mensaje;

    @Schema(description = "Detalle adicional del error, cuando corresponde")
    private Object detalle;

    public ErrorResponse() {
    }

    public ErrorResponse(String timestamp, Integer status, String error, String mensaje, Object detalle) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.detalle = detalle;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Object getDetalle() { return detalle; }
    public void setDetalle(Object detalle) { this.detalle = detalle; }
}
