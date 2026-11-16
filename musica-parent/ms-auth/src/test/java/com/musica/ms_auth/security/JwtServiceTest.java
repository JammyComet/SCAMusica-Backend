package com.musica.ms_auth.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    @org.junit.jupiter.api.BeforeAll static void secretoDePrueba(){
        System.setProperty("scamusic.jwt.secret","clave-solo-para-pruebas-unitarias-12345678901234567890");
    }
    @org.junit.jupiter.api.AfterAll static void limpiar(){System.clearProperty("scamusic.jwt.secret");}
    @Test void elJwtExpiraEnCincoMinutosYCadaEmisionEsDistinta(){
        JwtService service=new JwtService();
        String primero=service.generarToken(1L,"test@correo.cl","CLIENTE");
        String segundo=service.generarToken(1L,"test@correo.cl","CLIENTE");
        var c=service.verificar(primero);
        assertEquals(300L,c.getExpiration().getTime()/1000-c.getIssuedAt().getTime()/1000);
        assertEquals("acceso",c.get("tipo"));
        assertNotEquals(primero,segundo);
    }
    @Test void firmaYVerificaRolesEnJwt() {
        JwtService service=new JwtService();
        String token=service.generarToken(12L,"persona@correo.cl","CLIENTE",5L);
        var claims=service.verificar(token);
        assertEquals("persona@correo.cl",claims.getSubject());
        assertEquals("CLIENTE",claims.get("rol"));
        assertEquals(12,((Number)claims.get("usuarioId")).intValue());
    }
    @Test void noAceptaTokenManipulado() {
        JwtService service=new JwtService();
        String valido=service.generarToken(1L,"persona@correo.cl","CLIENTE");
        String[] partes=valido.split("\\.");
        String payload=partes[1];
        String manipulado=partes[0]+"."+(payload.startsWith("a")?"b":"a")+payload.substring(1)+"."+partes[2];
        assertThrows(Exception.class,()->service.verificar(manipulado));
    }
}
