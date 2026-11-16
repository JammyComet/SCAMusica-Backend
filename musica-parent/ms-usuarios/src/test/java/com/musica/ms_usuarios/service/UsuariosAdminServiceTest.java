package com.musica.ms_usuarios.service;
import com.musica.ms_usuarios.client.AuthUsuariosClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class UsuariosAdminServiceTest {
 @Test void enviaCredencialesAlServicioPropietario(){
  var auth=mock(AuthUsuariosClient.class);
  when(auth.listar("Bearer token")).thenReturn(List.of(Map.of("correo","cliente@gmail.com")));
  var servicio=new UsuariosAdminService(auth);
  assertEquals(1,servicio.listar("Bearer token").size());
  verify(auth).listar("Bearer token");
 }
}
