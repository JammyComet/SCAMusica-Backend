package com.musica.ms_categorias.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EndpointContractTest {
 @Test void consultaInternaDeCategoriaNoApareceEnSwagger() {
   assertTrue(CategoriaInternaController.class.isAnnotationPresent(Hidden.class));
 }
}
