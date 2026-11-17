package com.musica.ms_auth.service;

import com.musica.ms_auth.dto.TiendaUsuarioDTO;
import com.musica.ms_auth.model.Usuario;
import com.musica.ms_auth.repository.UsuarioRepository;
import com.musica.ms_auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TiendaUsuarioServiceTest {
    @org.junit.jupiter.api.BeforeAll static void secretoDePrueba(){
        System.setProperty("scamusic.jwt.secret","clave-solo-para-pruebas-unitarias-12345678901234567890");
    }
    @org.junit.jupiter.api.AfterAll static void limpiar(){System.clearProperty("scamusic.jwt.secret");}
    @Test void loginAceptaCredencialesBCryptYEmiteJWT() {
        var repo=mock(UsuarioRepository.class);
        var encoder=new BCryptPasswordEncoder();
        var usuario=new Usuario();usuario.setId(12L);usuario.setUsername("cliente@ejemplo.com");
        usuario.setPassword(encoder.encode("contraseña123"));usuario.setRol("CLIENTE");usuario.setActivo(true);
        when(repo.findByUsername("cliente@ejemplo.com")).thenReturn(java.util.Optional.of(usuario));
        var jwt=new JwtService();
        var service=new TiendaUsuarioService(repo,encoder,jwt);
        var respuesta=service.login(new com.musica.ms_auth.dto.LoginRequestDTO("CLIENTE@ejemplo.com","contraseña123"));
        assertEquals("cliente@ejemplo.com",respuesta.getUsername());
        assertEquals("CLIENTE",jwt.verificar(respuesta.getToken()).get("rol"));
        assertEquals(12,((Number)jwt.verificar(respuesta.getToken()).get("usuarioId")).intValue());
    }
    @Test void loginRechazaContraseñaIncorrecta() {
        var repo=mock(UsuarioRepository.class);
        var encoder=new BCryptPasswordEncoder();
        var usuario=new Usuario();usuario.setId(7L);usuario.setUsername("cliente@ejemplo.com");
        usuario.setPassword(encoder.encode("correcta"));usuario.setRol("CLIENTE");usuario.setActivo(true);
        when(repo.findByUsername("cliente@ejemplo.com")).thenReturn(java.util.Optional.of(usuario));
        var service=new TiendaUsuarioService(repo,encoder,new JwtService());
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            ()->service.login(new com.musica.ms_auth.dto.LoginRequestDTO("cliente@ejemplo.com","incorrecta")));
    }
    @Test void loginMigraContraseñaLegacyABCrypt() {
        var repo=mock(UsuarioRepository.class);
        var usuario=new Usuario();usuario.setId(5L);usuario.setUsername("cliente@ejemplo.com");
        usuario.setPassword("clave-vieja");usuario.setRol("CLIENTE");usuario.setActivo(true);
        when(repo.findByUsername("cliente@ejemplo.com")).thenReturn(java.util.Optional.of(usuario));
        var service=new TiendaUsuarioService(repo,new BCryptPasswordEncoder(),new JwtService());
        service.login(new com.musica.ms_auth.dto.LoginRequestDTO("cliente@ejemplo.com","clave-vieja"));
        assertNotEquals("clave-vieja",usuario.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("clave-vieja",usuario.getPassword()));
        verify(repo).save(usuario);
    }
    @Test void respuestasNoRevelanPasswordNiHash() {
        var repo=mock(UsuarioRepository.class);
        var service=new TiendaUsuarioService(repo,new BCryptPasswordEncoder(),new JwtService());
        var usuario=new Usuario();usuario.setId(8L);usuario.setUsername("cliente@gmail.com");
        usuario.setPassword("$2a$hash-que-no-se-muestra");usuario.setRol("CLIENTE");
        var dto=service.dto(usuario);
        assertEquals("cliente@gmail.com",dto.getCorreo());
        assertNull(dto.getContrasena());
        assertEquals("Cliente",dto.getRol());
    }
    @Test void registroPublicoNuncaPermiteCrearAdministradores() {
        var repo=mock(UsuarioRepository.class);
        when(repo.save(any(Usuario.class))).thenAnswer(inv->{Usuario u=inv.getArgument(0);u.setId(9L);return u;});
        var service=new TiendaUsuarioService(repo,new BCryptPasswordEncoder(),new JwtService());
        var dto=new TiendaUsuarioDTO();dto.setCorreo("nuevo@ejemplo.com");dto.setContrasena("clave123");dto.setRol("Administrador");
        var resultado=service.crear(dto,true);
        assertEquals("Cliente",resultado.getRol());
        var captor=org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(repo).save(captor.capture());
        assertEquals("CLIENTE",captor.getValue().getRol());
        assertNotEquals("clave123",captor.getValue().getPassword());
    }

    @Test void perfilRechazaContrasenaActualIncorrecta() {
        var repo=mock(UsuarioRepository.class);
        var encoder=new BCryptPasswordEncoder();
        var usuario=new Usuario();usuario.setId(21L);usuario.setUsername("cliente@gmail.com");
        usuario.setPassword(encoder.encode("actual-correcta"));usuario.setRol("CLIENTE");usuario.setActivo(true);
        when(repo.findById(21L)).thenReturn(java.util.Optional.of(usuario));
        var jwt=mock(JwtService.class);
        var claims=mock(io.jsonwebtoken.Claims.class);
        when(jwt.verificar("token-valido")).thenReturn(claims);
        when(claims.get("usuarioId")).thenReturn(21L);
        when(claims.getSubject()).thenReturn("cliente@gmail.com");
        when(claims.get("rol")).thenReturn("CLIENTE");
        var service=new TiendaUsuarioService(repo,encoder,jwt);
        var dto=new TiendaUsuarioDTO();dto.setContrasenaActual("incorrecta");dto.setContrasena("nueva-clave123");
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
            ()->service.actualizar(21L,dto,true,"Bearer token-valido"));
        assertTrue(encoder.matches("actual-correcta",usuario.getPassword()));
        verify(repo,never()).save(any());
    }
    @Test void perfilPermiteCambiarPasswordLegadoYNoConfundeRunInmutable() {
        var repo=mock(UsuarioRepository.class);
        var encoder=new BCryptPasswordEncoder();
        var usuario=new Usuario();usuario.setId(24L);usuario.setUsername("cliente@gmail.com");
        usuario.setPassword("clave-antigua123");usuario.setRol("CLIENTE");usuario.setActivo(true);
        usuario.setRun("11111111");
        when(repo.findById(24L)).thenReturn(java.util.Optional.of(usuario));
        when(repo.save(usuario)).thenReturn(usuario);
        var jwt=mock(JwtService.class);
        var claims=mock(io.jsonwebtoken.Claims.class);
        when(jwt.verificar("token-valido")).thenReturn(claims);
        when(claims.get("usuarioId")).thenReturn(24L);
        when(claims.getSubject()).thenReturn("cliente@gmail.com");
        when(claims.get("rol")).thenReturn("CLIENTE");
        var service=new TiendaUsuarioService(repo,encoder,jwt);
        var dto=new TiendaUsuarioDTO();
        dto.setRun("11111111");dto.setContrasenaActual("clave-antigua123");dto.setContrasena("clave-nueva123");
        service.actualizar(24L,dto,true,"Bearer token-valido");
        assertTrue(encoder.matches("clave-nueva123",usuario.getPassword()));
        assertFalse(encoder.matches("clave-antigua123",usuario.getPassword()));
        verify(repo,never()).existsByRun(anyString());
    }
    @Test void perfilActualizaDatosSinCambiarPassword() {
        var repo=mock(UsuarioRepository.class);
        var encoder=new BCryptPasswordEncoder();
        var usuario=new Usuario();usuario.setId(22L);usuario.setUsername("cliente@gmail.com");
        usuario.setPassword(encoder.encode("actual-correcta"));usuario.setRol("CLIENTE");usuario.setActivo(true);
        when(repo.findById(22L)).thenReturn(java.util.Optional.of(usuario));
        when(repo.save(usuario)).thenReturn(usuario);
        var jwt=mock(JwtService.class);
        var claims=mock(io.jsonwebtoken.Claims.class);
        when(jwt.verificar("token-valido")).thenReturn(claims);
        when(claims.get("usuarioId")).thenReturn(22L);
        when(claims.getSubject()).thenReturn("cliente@gmail.com");
        when(claims.get("rol")).thenReturn("CLIENTE");
        var service=new TiendaUsuarioService(repo,encoder,jwt);
        var dto=new TiendaUsuarioDTO();dto.setNombre("María");dto.setRol("Administrador");
        var resultado=service.actualizar(22L,dto,true,"Bearer token-valido");
        assertEquals("María",resultado.getNombre());
        assertEquals("CLIENTE",usuario.getRol());
        assertTrue(encoder.matches("actual-correcta",usuario.getPassword()));
    }
    @Test void cambioDePasswordActualizaBCryptYRevocaRefresh() {
        var repo=mock(UsuarioRepository.class);
        var encoder=new BCryptPasswordEncoder();
        var usuario=new Usuario();usuario.setId(23L);usuario.setUsername("cliente@gmail.com");
        usuario.setPassword(encoder.encode("clave-anterior"));usuario.setRol("CLIENTE");usuario.setActivo(true);
        when(repo.findById(23L)).thenReturn(java.util.Optional.of(usuario));
        when(repo.save(usuario)).thenReturn(usuario);
        var jwt=mock(JwtService.class);
        var claims=mock(io.jsonwebtoken.Claims.class);
        when(jwt.verificar("token-valido")).thenReturn(claims);
        when(claims.get("usuarioId")).thenReturn(23L);
        when(claims.getSubject()).thenReturn("cliente@gmail.com");
        when(claims.get("rol")).thenReturn("CLIENTE");
        var service=new TiendaUsuarioService(repo,encoder,jwt);
        var sesiones=mock(com.musica.ms_auth.repository.SesionRefreshRepository.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"sesiones",sesiones);
        var dto=new TiendaUsuarioDTO();dto.setContrasenaActual("clave-anterior");dto.setContrasena("clave-nueva123");
        service.actualizar(23L,dto,true,"Bearer token-valido");
        assertTrue(encoder.matches("clave-nueva123",usuario.getPassword()));
        assertFalse(encoder.matches("clave-anterior",usuario.getPassword()));
        verify(sesiones).deleteByUsuarioId(23L);
    }
}
