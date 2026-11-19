package com.musica.ms_categorias.controller;

import com.musica.ms_categorias.model.Categoria;
import com.musica.ms_categorias.service.CategoriaService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;

/** Solo utilizado por ms-catalogo para validar la categoría de un disco. */
@RestController
@Hidden
@RequestMapping("/internal/categorias")
public class CategoriaInternaController {
    private final CategoriaService service;
    public CategoriaInternaController(CategoriaService service) { this.service=service; }
    @GetMapping("/{id}") public Categoria detalle(@PathVariable Long id) { return service.uno(id); }
}
