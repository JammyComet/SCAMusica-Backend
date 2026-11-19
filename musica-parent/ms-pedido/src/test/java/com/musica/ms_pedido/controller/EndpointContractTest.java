package com.musica.ms_pedido.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class EndpointContractTest {
 @Test void detalleSeConstruyeEnReactDesdeLasColecciones() {
   assertFalse(Arrays.stream(TiendaOrdenController.class.getDeclaredMethods()).filter(m->m.isAnnotationPresent(GetMapping.class))
       .anyMatch(m->Arrays.asList(m.getAnnotation(GetMapping.class).value()).contains("/{codigo}")));
   assertFalse(Arrays.stream(BoletasController.class.getDeclaredMethods()).filter(m->m.isAnnotationPresent(GetMapping.class))
       .anyMatch(m->Arrays.asList(m.getAnnotation(GetMapping.class).value()).contains("/{numero}")));
 }
}
