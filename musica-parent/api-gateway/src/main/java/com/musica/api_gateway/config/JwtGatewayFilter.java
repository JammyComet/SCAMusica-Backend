package com.musica.api_gateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private final SecretKey key=Keys.hmacShaKeyFor(java.util.Objects.requireNonNull(System.getProperty("scamusic.jwt.secret",System.getenv("JWT_SECRET")),"Configura JWT_SECRET").getBytes(StandardCharsets.UTF_8));
    @Override public int getOrder() {return -100;}
    private Mono<Void> error(ServerWebExchange x,HttpStatus status) {x.getResponse().setStatusCode(status);return x.getResponse().setComplete();}
    @Override public Mono<Void> filter(ServerWebExchange x,GatewayFilterChain chain) {
        String path=x.getRequest().getPath().value();
        HttpMethod method=x.getRequest().getMethod();
        if(method==HttpMethod.OPTIONS || !path.startsWith("/api/")) return chain.filter(x);
        // Never relay a caller-supplied internal service key, including on public routes.
        x=x.mutate().request(x.getRequest().mutate().headers(h->h.remove("X-Internal-Key")).build()).build();
        boolean read=method==HttpMethod.GET;
        if ((read && (path.equals("/api/v1/productos") || path.startsWith("/api/v1/productos/") || path.equals("/api/v1/categorias") || path.startsWith("/api/v1/categorias/")))
            || (method==HttpMethod.POST && Set.of("/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/logout","/api/v1/usuarios/registro","/api/v1/contactos").contains(path))) return chain.filter(x);
        String authorization=x.getRequest().getHeaders().getFirst("Authorization");
        Claims c;
        try {
            if(authorization==null || !authorization.startsWith("Bearer ")) return error(x,HttpStatus.UNAUTHORIZED);
            c=Jwts.parser().verifyWith(key).build().parseSignedClaims(authorization.substring(7)).getPayload();
        }catch(Exception e){return error(x,HttpStatus.UNAUTHORIZED);}
        if(!"scamusic-auth".equals(c.getIssuer()) || !"acceso".equals(c.get("tipo",String.class))) return error(x,HttpStatus.UNAUTHORIZED);
        String rol=c.get("rol",String.class);
        String username=c.getSubject();
        if(rol==null || username==null || c.get("usuarioId")==null) return error(x,HttpStatus.UNAUTHORIZED);
        boolean permitido;
        if(path.equals("/api/v1/usuarios/me")) permitido=true;
        else if(path.startsWith("/api/v1/usuarios")) permitido=rol.equals("ADMIN");
        else if(path.startsWith("/api/v1/productos") || path.startsWith("/api/v1/categorias")) permitido=rol.equals("ADMIN");
        else if(path.startsWith("/api/v1/carritos")) permitido=rol.equals("CLIENTE");
        else if(path.startsWith("/api/v1/ordenes")) {
            if(path.equals("/api/v1/ordenes/mias")) permitido=read && rol.equals("CLIENTE");
            else if(path.equals("/api/v1/ordenes")) permitido= read ? (rol.equals("ADMIN") || rol.equals("VENDEDOR")) : (method==HttpMethod.POST && rol.equals("CLIENTE"));
            else permitido=read && (rol.equals("CLIENTE") || rol.equals("ADMIN") || rol.equals("VENDEDOR"));
        } else if(path.startsWith("/api/v1/boletas")) {
            if(path.equals("/api/v1/boletas/mias")) permitido=read && rol.equals("CLIENTE");
            else if(path.equals("/api/v1/boletas")) permitido=read && rol.equals("ADMIN");
            else permitido=read && (rol.equals("CLIENTE") || rol.equals("ADMIN"));
        }
        else if(path.startsWith("/api/v1/admin/usuarios") || path.startsWith("/api/v1/reportes") || path.startsWith("/api/v1/inventario")) permitido=read ? rol.equals("ADMIN") : (path.startsWith("/api/v1/admin/usuarios") && rol.equals("ADMIN"));
        else if(path.startsWith("/api/v1/contactos")) permitido=rol.equals("ADMIN");
        else if(path.startsWith("/api/")) permitido=false;
        else permitido=false;
        if(!permitido) return error(x,HttpStatus.FORBIDDEN);
        // Never trust a client-supplied identity header. Derive it from the signed JWT.
        var request=x.getRequest().mutate().headers(h->{h.remove("X-SCA-User");h.remove("X-SCA-Role");h.remove("X-SCA-Id");h.remove("X-Internal-Key");})
            .header("X-SCA-User",username).header("X-SCA-Role",rol).header("X-SCA-Id",String.valueOf(c.get("usuarioId"))).build();
        return chain.filter(x.mutate().request(request).build());
    }
}
