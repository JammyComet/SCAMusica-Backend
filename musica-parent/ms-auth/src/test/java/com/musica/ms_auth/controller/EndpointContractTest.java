package com.musica.ms_auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

/** Protege el contrato dictado por el frontend: Contacto solo envía; usuarios no duplica CRUD. */
class EndpointContractTest {
 @Test void contactoSoloTienePost() {
   var metodos=ContactoController.class.getDeclaredMethods();
   assertEquals(1, Arrays.stream(metodos).filter(m->m.isAnnotationPresent(PostMapping.class)).count());
   assertEquals(0, Arrays.stream(metodos).filter(m->m.isAnnotationPresent(GetMapping.class)).count());
 }
 @Test void usuariosSoloRegistroYPerfil() {
   var metodos=TiendaUsuarioController.class.getDeclaredMethods();
   assertEquals(3,Arrays.stream(metodos).filter(m->m.isAnnotationPresent(GetMapping.class)||m.isAnnotationPresent(PostMapping.class)||m.isAnnotationPresent(PutMapping.class)||m.isAnnotationPresent(DeleteMapping.class)).count());
   assertEquals(0, Arrays.stream(metodos).filter(m->m.isAnnotationPresent(DeleteMapping.class)).count());
 }
}
