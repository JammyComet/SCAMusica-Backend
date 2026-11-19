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
        boolean publico=read && (path.equals("/api/v1/productos") || path.startsWith("/api/v1/productos/imagenes/") || path.equals("/api/v1/categorias"))
            || method==HttpMethod.POST && Set.of("/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/logout","/api/v1/usuarios/registro","/api/v1/contactos").contains(path);
        if(publico)return chain.filter(x);
        String authorization=x.getRequest().getHeaders().getFirst("Authorization");
        Claims c;
        try {
            if(authorization==null || !authorization.startsWith("Bearer "))return error(x,HttpStatus.UNAUTHORIZED);
            c=Jwts.parser().verifyWith(key).build().parseSignedClaims(authorization.substring(7)).getPayload();
        }catch(Exception e){return error(x,HttpStatus.UNAUTHORIZED);}
        if(!"scamusic-auth".equals(c.getIssuer()) || !"acceso".equals(c.get("tipo",String.class)))return error(x,HttpStatus.UNAUTHORIZED);
        String rol=c.get("rol",String.class);
        String username=c.getSubject();
        if(rol==null || username==null || !(c.get("usuarioId") instanceof Number))return error(x,HttpStatus.UNAUTHORIZED);
        boolean admin="ADMIN".equals(rol), cliente="CLIENTE".equals(rol), vendedor="VENDEDOR".equals(rol);
        boolean permitido=path.equals("/api/v1/usuarios/me") && (method==HttpMethod.GET || method==HttpMethod.PUT)
            || (path.equals("/api/v1/admin/usuarios") && (read || method==HttpMethod.POST) && admin)
            || (path.startsWith("/api/v1/admin/usuarios/") && (method==HttpMethod.PUT || method==HttpMethod.DELETE) && admin)
            || (path.equals("/api/v1/productos") && method==HttpMethod.POST && admin)
            || (path.equals("/api/v1/productos/imagenes") && method==HttpMethod.POST && admin)
            || (path.startsWith("/api/v1/productos/") && (method==HttpMethod.PUT || method==HttpMethod.DELETE) && admin)
            || (path.equals("/api/v1/categorias") && method==HttpMethod.POST && admin)
            || (path.startsWith("/api/v1/categorias/") && (method==HttpMethod.PUT || method==HttpMethod.DELETE) && admin)
            || (path.equals("/api/v1/carritos") && (read || method==HttpMethod.PUT) && cliente)
            || (path.equals("/api/v1/ordenes") && (read && (admin || vendedor) || method==HttpMethod.POST && cliente))
            || (path.equals("/api/v1/ordenes/mias") && read && cliente)
            || (path.equals("/api/v1/boletas") && read && admin)
            || (path.equals("/api/v1/boletas/mias") && read && cliente)
            || (path.equals("/api/v1/reportes/resumen") && read && admin)
            || (path.equals("/api/v1/inventario/criticos") && read && admin);
        if(!permitido)return error(x,HttpStatus.FORBIDDEN);
        // Never trust a client-supplied identity header. Derive it from the signed JWT.
        var request=x.getRequest().mutate().headers(h->{h.remove("X-SCA-User");h.remove("X-SCA-Role");h.remove("X-SCA-Id");h.remove("X-Internal-Key");})
            .header("X-SCA-User",username).header("X-SCA-Role",rol).header("X-SCA-Id",String.valueOf(c.get("usuarioId"))).build();
        return chain.filter(x.mutate().request(request).build());
    }
}
