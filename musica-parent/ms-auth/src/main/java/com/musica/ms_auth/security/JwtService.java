package com.musica.ms_auth.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    public static final long EXPIRACION_SEGUNDOS = 300L;
    private final SecretKey key = Keys.hmacShaKeyFor(
        Objects.requireNonNull(System.getProperty("scamusic.jwt.secret", System.getenv("JWT_SECRET")),
            "Configura JWT_SECRET (mínimo 32 bytes) en todos los microservicios")
            .getBytes(StandardCharsets.UTF_8));

    public Claims verificar(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (!"scamusic-auth".equals(claims.getIssuer()) ||
            !"acceso".equals(claims.get("tipo", String.class)))
            throw new IllegalArgumentException("JWT no es un token de acceso de SCAMusica");
        return claims;
    }

    public String generarToken(String username) {return generarToken(null, username, null, null);}
    public String generarToken(Long usuarioId, String username, String rol) {return generarToken(usuarioId, username, rol, null);}
    public String generarToken(Long usuarioId, String username, String rol, Long clienteId) {
        long ahora = System.currentTimeMillis();
        var builder = Jwts.builder()
            .issuer("scamusic-auth")
            .claim("tipo", "acceso")
            .id(UUID.randomUUID().toString())
            .subject(username)
            .issuedAt(new Date(ahora))
            .expiration(new Date(ahora + EXPIRACION_SEGUNDOS * 1000L));
        if (usuarioId != null) builder.claim("usuarioId", usuarioId);
        if (rol != null) builder.claim("rol", rol);
        if (clienteId != null) builder.claim("clienteId", clienteId);
        return builder.signWith(key).compact();
    }
}
