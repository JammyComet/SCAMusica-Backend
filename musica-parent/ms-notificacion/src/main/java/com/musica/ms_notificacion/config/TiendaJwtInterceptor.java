package com.musica.ms_notificacion.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TiendaJwtInterceptor implements WebMvcConfigurer {
    private final SecretKey key=Keys.hmacShaKeyFor(java.util.Objects.requireNonNull(System.getProperty("scamusic.jwt.secret",System.getenv("JWT_SECRET")),"Configura JWT_SECRET").getBytes(StandardCharsets.UTF_8));
    @Override public void addInterceptors(InterceptorRegistry r){r.addInterceptor(new HandlerInterceptor(){
        @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
            String path=req.getRequestURI(),m=req.getMethod();
            if("OPTIONS".equals(m))return true;
            if(path.startsWith("/api/v1/contactos") && "POST".equals(m)) return true;
            if((path.startsWith("/api/v1/productos") || path.startsWith("/api/v1/categorias")) && "GET".equals(m))return true;
            if(path.equals("/api/v1/productos/descontar-lote")) {
                String internal=java.util.Objects.requireNonNull(System.getenv("INTERNAL_KEY"),"Configura INTERNAL_KEY");
                if(internal.equals(req.getHeader("X-Internal-Key")))return true;
                res.setStatus(403);return false;
            }
            Claims c;
            try {
                String header=req.getHeader("Authorization");
                if(header==null || !header.startsWith("Bearer "))throw new IllegalArgumentException();
                c=Jwts.parser().verifyWith(key).build().parseSignedClaims(header.substring(7)).getPayload();
                if(c.getSubject()==null || c.get("usuarioId")==null)throw new IllegalArgumentException();
            }catch(Exception e){res.setStatus(401);return false;}
            if(!"scamusic-auth".equals(c.getIssuer()) || !"acceso".equals(c.get("tipo",String.class))) {res.setStatus(401);return false;}
            String role=c.get("rol",String.class);
            boolean permitido=false;
            if(path.startsWith("/api/v1/productos") || path.startsWith("/api/v1/categorias") || path.startsWith("/api/v1/contactos"))permitido="ADMIN".equals(role);
            else if(path.startsWith("/api/v1/carritos"))permitido="CLIENTE".equals(role);
            else if(path.startsWith("/api/v1/ordenes")) {
                if(path.equals("/api/v1/ordenes/mias"))permitido="GET".equals(m)&&"CLIENTE".equals(role);
                else if(path.equals("/api/v1/ordenes"))permitido="GET".equals(m)?("ADMIN".equals(role)||"VENDEDOR".equals(role)):("POST".equals(m)&&"CLIENTE".equals(role));
                else permitido="GET".equals(m);
            }
            if(!permitido){res.setStatus(403);return false;}
            req.setAttribute("authId",((Number)c.get("usuarioId")).longValue());
            req.setAttribute("authUser",c.getSubject());req.setAttribute("authRole",role);
            return true;
        }
    }).addPathPatterns("/api/v1/**");}
}
