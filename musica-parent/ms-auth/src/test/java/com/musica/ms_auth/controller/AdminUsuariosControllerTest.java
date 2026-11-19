package com.musica.ms_auth.controller;
import com.musica.ms_auth.service.TiendaUsuarioService;
import com.musica.ms_auth.dto.TiendaUsuarioDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AdminUsuariosControllerTest {
 @Test void usaDirectamenteServicioPropietario() {
   var usuarios=mock(TiendaUsuarioService.class);
   var dto=new TiendaUsuarioDTO();
   when(usuarios.crear(dto,false)).thenReturn(dto);
   var controller=new AdminUsuariosController(usuarios);
   assertEquals(201,controller.crear(dto).getStatusCode().value());
   verify(usuarios).crear(dto,false);
 }
}
