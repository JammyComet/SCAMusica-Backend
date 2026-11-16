package com.musica.ms_notificacion.service;

import com.musica.ms_notificacion.dto.TiendaContactoDTO;
import com.musica.ms_notificacion.model.Notificacion;
import com.musica.ms_notificacion.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TiendaContactoServiceTest {
    @Test void recibeFormularioDelFrontend() {
        var repo=mock(NotificacionRepository.class);
        var service=new TiendaContactoService(repo);
        service.enviar(new TiendaContactoDTO("Ana","ana@ejemplo.com","Consulta sobre un disco"));
        var captor=org.mockito.ArgumentCaptor.forClass(Notificacion.class);
        verify(repo).save(captor.capture());
        assertEquals("CONTACTO",captor.getValue().getEstado());
        assertTrue(captor.getValue().getMensaje().contains("ana@ejemplo.com"));
    }
    @Test void rechazaContactoSinCorreo() {
        var repo=mock(NotificacionRepository.class);
        assertThrows(ResponseStatusException.class,()->new TiendaContactoService(repo).enviar(new TiendaContactoDTO("Ana","mal-correo","Hola")));
        verifyNoInteractions(repo);
    }
}
