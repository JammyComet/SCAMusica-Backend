package com.musica.ms_auth.service;

import com.musica.ms_auth.dto.LoginRequestDTO;
import com.musica.ms_auth.dto.LoginResponseDTO;
import com.musica.ms_auth.model.SesionRefresh;
import com.musica.ms_auth.repository.SesionRefreshRepository;
import com.musica.ms_auth.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SesionService {
    public static final long SESION_SEGUNDOS = 8 * 60 * 60; // Máximo absoluto de 8 horas.
    private final SesionRefreshRepository sesiones;
    private final UsuarioRepository usuarios;
    private final TiendaUsuarioService tienda;
    private final SecureRandom aleatorio = new SecureRandom();

    public record SesionEmitida(LoginResponseDTO datos, String refresh, Instant expira) {}

    public SesionService(SesionRefreshRepository sesiones, UsuarioRepository usuarios, TiendaUsuarioService tienda) {
        this.sesiones=sesiones;this.usuarios=usuarios;this.tienda=tienda;
    }

    private String generarSecreto() {
        byte[] bytes = new byte[48]; aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private String hash(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    private ResponseStatusException noAutorizado() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión expiró. Inicia sesión nuevamente");
    }
    private LoginResponseDTO respuesta(com.musica.ms_auth.model.Usuario u) {
        var r=new LoginResponseDTO(tienda.token(u),u.getId(),u.getUsername(),u.getRol(),u.getClienteId());
        r.setExpiresIn(300L);
        return r;
    }

    @Transactional
    public SesionEmitida iniciar(LoginRequestDTO credenciales) {
        LoginResponseDTO login=tienda.login(credenciales);
        String refresh=generarSecreto();
        Instant expira=Instant.now().plus(SESION_SEGUNDOS,ChronoUnit.SECONDS);
        SesionRefresh s=new SesionRefresh();s.setUsuarioId(login.getUsuarioId());s.setTokenHash(hash(refresh));s.setExpiraEn(expira);
        sesiones.save(s);
        login.setExpiresIn(300L);
        return new SesionEmitida(login,refresh,expira);
    }

    // La fila queda bloqueada en MySQL mientras se consume y rota el refresh.
    // El refresh anterior deja de servir inmediatamente; no prolongamos las 8h originales.
    @Transactional
    public SesionEmitida renovar(String refreshAnterior) {
        if(refreshAnterior==null || refreshAnterior.isBlank())throw noAutorizado();
        SesionRefresh sesion=sesiones.buscarBloqueando(hash(refreshAnterior)).orElseThrow(this::noAutorizado);
        if(!sesion.getExpiraEn().isAfter(Instant.now())) {
            sesiones.delete(sesion);throw noAutorizado();
        }
        var u=usuarios.findById(sesion.getUsuarioId()).orElseThrow(this::noAutorizado);
        if(!u.isActivo()) {sesiones.delete(sesion);throw noAutorizado();}
        String nuevo=generarSecreto();
        sesion.setTokenHash(hash(nuevo));
        sesiones.saveAndFlush(sesion);
        return new SesionEmitida(respuesta(u),nuevo,sesion.getExpiraEn());
    }

    @Transactional
    public void cerrar(String refresh) {
        if(refresh==null || refresh.isBlank())return;
        sesiones.buscarBloqueando(hash(refresh)).ifPresent(sesiones::delete);
    }
}
