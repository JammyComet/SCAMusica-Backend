package com.musica.ms_catalogo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permisos exclusivos de este microservicio; sin reglas copiadas de otros módulos. */
@Configuration
public class TiendaJwtInterceptor implements WebMvcConfigurer {
    private final SecretKey key=Keys.hmacShaKeyFor(java.util.Objects.requireNonNull(
        System.getProperty("scamusic.jwt.secret",System.getenv("JWT_SECRET")),"Configura JWT_SECRET").getBytes(StandardCharsets.UTF_8));
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
                String path=req.getRequestURI(), method=req.getMethod();
                if("OPTIONS".equals(method))return true;
        // La ruta interna es exclusiva para microservicios; no pasa por Gateway.
        if(path.startsWith("/internal/")) {
            String esperado=java.util.Objects.requireNonNull(System.getenv("INTERNAL_KEY"),"Configura INTERNAL_KEY");
            if(esperado.equals(req.getHeader("X-Internal-Key")))return true;
            res.setStatus(403);return false;
        }
                boolean publico = method.equals("GET") && (path.equals("/api/v1/productos") || path.startsWith("/api/v1/productos/imagenes/"));
                if(publico)return true;
                Claims claims;
                try {
                    String authorization=req.getHeader("Authorization");
                    if(authorization==null || !authorization.startsWith("Bearer "))throw new IllegalArgumentException();
                    claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(authorization.substring(7)).getPayload();
                    if(claims.getSubject()==null || !(claims.get("usuarioId") instanceof Number))throw new IllegalArgumentException();
                    if(!"scamusic-auth".equals(claims.getIssuer()) || !"acceso".equals(claims.get("tipo",String.class)))throw new IllegalArgumentException();
                } catch(Exception ex){res.setStatus(401);return false;}
                String rol=claims.get("rol",String.class);
                boolean permitido = "ADMIN".equals(rol) && (path.equals("/api/v1/inventario/criticos") && method.equals("GET") || path.equals("/api/v1/productos") && method.equals("POST") || path.equals("/api/v1/productos/imagenes") && method.equals("POST") || path.startsWith("/api/v1/productos/") && (method.equals("PUT") || method.equals("DELETE")));
                if(!permitido){res.setStatus(403);return false;}
                req.setAttribute("authId",((Number)claims.get("usuarioId")).longValue());
                req.setAttribute("authUser",claims.getSubject());
                req.setAttribute("authRole",rol);
                return true;
            }
        }).addPathPatterns("/api/v1/**", "/internal/**");
    }
}
