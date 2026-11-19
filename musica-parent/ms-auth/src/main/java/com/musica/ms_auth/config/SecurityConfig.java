package com.musica.ms_auth.config;

import com.musica.ms_auth.security.JwtService;
import com.musica.ms_auth.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {
    @Bean public BCryptPasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtService jwt,UsuarioRepository usuarios) throws Exception {
        http.csrf(csrf->csrf.disable())
            .sessionManagement(sm->sm.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .httpBasic(basic->basic.disable()).formLogin(form->form.disable())
            .authorizeHttpRequests(a->a
            .requestMatchers("/api/v1/auth/login","/api/v1/auth/refresh","/api/v1/auth/logout","/api/v1/usuarios/registro","/v3/api-docs/**","/swagger-ui/**","/doc/**","/actuator/health").permitAll()
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/contactos").permitAll()
            .requestMatchers("/api/v1/admin/usuarios", "/api/v1/admin/usuarios/**").hasRole("ADMIN")
            .requestMatchers("/api/v1/usuarios/me").authenticated()
                        .anyRequest().denyAll())
            .addFilterBefore(new OncePerRequestFilter() {
                @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
                    String h=req.getHeader("Authorization");
                    if(h!=null && h.startsWith("Bearer ")) try {
                        var c=jwt.verificar(h.substring(7));
                        String role=c.get("rol",String.class);
                        Long usuarioId=((Number)c.get("usuarioId")).longValue();
                        var current=usuarios.findById(usuarioId).orElseThrow();
                        if(!current.isActivo() || !current.getUsername().equals(c.getSubject()) || !current.getRol().equals(role)) throw new IllegalArgumentException("Usuario no autorizado");
                        var auth=new UsernamePasswordAuthenticationToken(c.getSubject(),null,java.util.List.of(new SimpleGrantedAuthority("ROLE_"+role)));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } catch(Exception ignored) { SecurityContextHolder.clearContext(); }
                    chain.doFilter(req,res);
                }
            },org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
