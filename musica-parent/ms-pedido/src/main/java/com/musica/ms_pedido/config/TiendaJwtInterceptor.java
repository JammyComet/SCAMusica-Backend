package com.musica.ms_pedido.config;

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
                boolean publico = false;
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
                boolean permitido = (path.equals("/api/v1/ordenes") && (method.equals("GET") && ("ADMIN".equals(rol) || "VENDEDOR".equals(rol)) || method.equals("POST") && "CLIENTE".equals(rol)))
                  || (path.equals("/api/v1/ordenes/mias") && method.equals("GET") && "CLIENTE".equals(rol))
                  || (path.equals("/api/v1/boletas") && method.equals("GET") && "ADMIN".equals(rol))
                  || (path.equals("/api/v1/boletas/mias") && method.equals("GET") && "CLIENTE".equals(rol));
                if(!permitido){res.setStatus(403);return false;}
                req.setAttribute("authId",((Number)claims.get("usuarioId")).longValue());
                req.setAttribute("authUser",claims.getSubject());
                req.setAttribute("authRole",rol);
                return true;
            }
        }).addPathPatterns("/api/v1/**");
    }
}
