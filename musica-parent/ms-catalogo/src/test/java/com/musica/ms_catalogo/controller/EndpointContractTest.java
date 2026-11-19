package com.musica.ms_catalogo.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EndpointContractTest {
 @Test void transaccionesInternasNoAparecenEnSwagger() {
   assertTrue(CatalogoInternoController.class.isAnnotationPresent(Hidden.class));
 }
}
