package com.musica.ms_auth.service;

import com.musica.ms_auth.model.Usuario;
import com.musica.ms_auth.model.SesionRefresh;
import com.musica.ms_auth.repository.UsuarioRepository;
import com.musica.ms_auth.repository.SesionRefreshRepository;
import com.musica.ms_auth.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SesionServiceTest {
    @BeforeAll static void init(){System.setProperty("scamusic.jwt.secret","clave-solo-para-pruebas-unitarias-12345678901234567890");}
    @AfterAll static void cleanup(){System.clearProperty("scamusic.jwt.secret");}
    @Test void rotaCredencialYAceptaSoloLaNueva() {
        var repo=mock(SesionRefreshRepository.class);
        var usuarios=mock(UsuarioRepository.class);
        var u=new Usuario();u.setId(5L);u.setUsername("persona@correo.cl");u.setRol("CLIENTE");u.setActivo(true);
        when(usuarios.findById(5L)).thenReturn(Optional.of(u));
        var tienda=new TiendaUsuarioService(usuarios,new BCryptPasswordEncoder(),new JwtService());
        var servicio=new SesionService(repo,usuarios,tienda);
        SesionRefresh fila=new SesionRefresh();fila.setUsuarioId(5L);fila.setExpiraEn(java.time.Instant.now().plusSeconds(3600));
        when(repo.buscarBloqueando(anyString())).thenAnswer(inv->{
            String hash=inv.getArgument(0);
            return hash.equals(fila.getTokenHash())?Optional.of(fila):Optional.empty();
        });
        // Simula una primera credencial de refresh en tabla; el hash usa SHA-256.
        String tokenViejo="token-que-estaba-guardado";
        try {
            var digest=java.security.MessageDigest.getInstance("SHA-256");
            fila.setTokenHash(java.util.HexFormat.of().formatHex(digest.digest(tokenViejo.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        } catch(Exception e){throw new IllegalStateException(e);}
        var siguiente=servicio.renovar(tokenViejo);
        assertNotEquals(tokenViejo,siguiente.refresh());
        assertEquals(300L,siguiente.datos().getExpiresIn());
        assertThrows(ResponseStatusException.class,()->servicio.renovar(tokenViejo));
        var nueva=servicio.renovar(siguiente.refresh());
        assertNotEquals(siguiente.refresh(),nueva.refresh());
    }
}
