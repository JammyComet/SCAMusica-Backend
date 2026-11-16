package com.musica.ms_auth.controller;

import com.musica.ms_auth.dto.LoginRequestDTO;
import com.musica.ms_auth.dto.LoginResponseDTO;
import com.musica.ms_auth.service.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name="Autenticación", description="JWT de acceso 5 minutos y renovación rotativa en cookie HttpOnly")
public class AuthController {
    private static final String COOKIE="scamusic_refresh";
    private final SesionService sesiones;
    private final boolean cookieSecure=Boolean.parseBoolean(System.getenv().getOrDefault("COOKIE_SECURE","false"));
    public AuthController(SesionService sesiones) {this.sesiones=sesiones;}

    private String cookie(HttpServletRequest req) {
        if(req.getCookies()==null)return null;
        return Arrays.stream(req.getCookies()).filter(c->COOKIE.equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
    }
    private void fijarCookie(HttpServletResponse res, String valor, Instant vence) {
        long vida=Math.max(0,Duration.between(Instant.now(),vence).toSeconds());
        res.addHeader("Set-Cookie", ResponseCookie.from(COOKIE,valor)
            .httpOnly(true).secure(cookieSecure).sameSite("Strict").path("/api/v1/auth")
            .maxAge(Duration.ofSeconds(vida)).build().toString());
    }
    private void quitarCookie(HttpServletResponse res) {
        res.addHeader("Set-Cookie",ResponseCookie.from(COOKIE,"")
            .httpOnly(true).secure(cookieSecure).sameSite("Strict").path("/api/v1/auth")
            .maxAge(Duration.ZERO).build().toString());
    }
    private void verificarSolicitudNavegador(String solicitado) {
        // Header no-simple: obliga a preflight CORS; una página externa no puede hacer POST silencioso.
        if(!"SCAMusica".equals(solicitado))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Solicitud de sesión no permitida");
    }
    @Operation(summary="Iniciar sesión: JWT de acceso 5 minutos y cookie de renovación")
    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody @Valid LoginRequestDTO credenciales, HttpServletResponse res) {
        var emitida=sesiones.iniciar(credenciales);
        fijarCookie(res,emitida.refresh(),emitida.expira());
        return emitida.datos();
    }
    @Operation(summary="Renovar el JWT y rotar la credencial de renovación")
    @PostMapping("/refresh")
    public LoginResponseDTO refresh(HttpServletRequest req,HttpServletResponse res,
            @RequestHeader(value="X-Requested-With",required=false) String solicitado) {
        verificarSolicitudNavegador(solicitado);
        try {
            var emitida=sesiones.renovar(cookie(req));
            fijarCookie(res,emitida.refresh(),emitida.expira());
            return emitida.datos();
        } catch(ResponseStatusException e) {quitarCookie(res);throw e;}
    }
    @Operation(summary="Cerrar sesión, revocando la credencial de renovación")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req,HttpServletResponse res,
            @RequestHeader(value="X-Requested-With",required=false) String solicitado) {
        verificarSolicitudNavegador(solicitado);
        sesiones.cerrar(cookie(req));quitarCookie(res);
        return ResponseEntity.noContent().build();
    }
}
