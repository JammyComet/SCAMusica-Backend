package com.musica.ms_reportes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Contrato documentado de GET /api/v1/reportes/resumen. */
@Schema(name="ResumenReportes")
public record ResumenReporteDoc(
    @Schema(example="145900") Double ventasTotales,
    @Schema(example="4") Long ordenesPagadas,
    @Schema(example="6") Long unidadesVendidas,
    @Schema(example="10") Long productosActivos,
    @Schema(example="2") Long stockCritico,
    @Schema(example="15") Long usuarios
) {}
