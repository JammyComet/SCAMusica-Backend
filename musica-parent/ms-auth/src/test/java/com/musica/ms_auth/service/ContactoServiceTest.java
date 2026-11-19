package com.musica.ms_auth.service;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ContactoServiceTest {
 @Test void rechazaDatosInvalidos() {
   var servicio=new ContactoService(mock(JdbcTemplate.class));
   assertThrows(ResponseStatusException.class, ()->servicio.guardar("", "a@ejemplo.com", "hola"));
   assertThrows(ResponseStatusException.class, ()->servicio.guardar("Ana", "no-es-correo", "hola"));
 }
 @Test void conservaCamposRealesDelFrontend() {
   var jdbc=mock(JdbcTemplate.class);
   new ContactoService(jdbc).guardar("Ana","ana@ejemplo.com","Consulta");
   verify(jdbc).execute(contains("CREATE TABLE IF NOT EXISTS contacto"));
   verify(jdbc).update(contains("INSERT INTO contacto"), eq("Ana"), eq("ana@ejemplo.com"), eq("Consulta"), any());
 }
}
