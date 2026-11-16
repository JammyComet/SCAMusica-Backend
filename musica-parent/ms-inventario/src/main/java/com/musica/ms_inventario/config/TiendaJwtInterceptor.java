package com.musica.ms_inventario.config;
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
@Configuration
public class TiendaJwtInterceptor implements WebMvcConfigurer {
  private final SecretKey key=Keys.hmacShaKeyFor(java.util.Objects.requireNonNull(System.getProperty("scamusic.jwt.secret",System.getenv("JWT_SECRET")),"Configura JWT_SECRET").getBytes(StandardCharsets.UTF_8));
  @Override public void addInterceptors(InterceptorRegistry r) { r.addInterceptor(new HandlerInterceptor() {
     @Override public boolean preHandle(HttpServletRequest req,HttpServletResponse res,Object handler) {
         String path=req.getRequestURI(),method=req.getMethod();
         if("OPTIONS".equals(method))return true;
         if(path.startsWith("/api/v1/categorias") && "GET".equals(method))return true;
         Claims claims;
         try {String auth=req.getHeader("Authorization");if(auth==null || !auth.startsWith("Bearer "))throw new IllegalArgumentException();
              claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(auth.substring(7)).getPayload();
              if(claims.getSubject()==null || !(claims.get("usuarioId") instanceof Number))throw new IllegalArgumentException();
              if(!"scamusic-auth".equals(claims.getIssuer()) || !"acceso".equals(claims.get("tipo",String.class)))throw new IllegalArgumentException();
         } catch(Exception e) {res.setStatus(401);return false;}
         String role=claims.get("rol",String.class);
         boolean ok="ADMIN".equals(role);
         if(path.startsWith("/api/v1/boletas")) {
             if(path.equals("/api/v1/boletas"))ok="ADMIN".equals(role);
             else if(path.equals("/api/v1/boletas/mias"))ok="CLIENTE".equals(role);
             else ok="ADMIN".equals(role)||"CLIENTE".equals(role);
         }
         if(!ok){res.setStatus(403);return false;}
         req.setAttribute("authUser",claims.getSubject());req.setAttribute("authRole",role);
         return true;
     }
  }).addPathPatterns("/api/v1/**"); }
}
