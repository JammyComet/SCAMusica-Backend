package com.musica.ms_catalogo.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class ImagenProductoServiceTest {
    @TempDir Path temporal;

    @Test void guardaYRecuperaUnaPortadaEnDirectorioPersistente() throws Exception {
        var servicio=new ImagenProductoService(temporal.toString());
        byte[] contenido=Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO++r3sAAAAASUVORK5CYII=");
        var archivo=new MockMultipartFile("archivo","portada.png","image/png",contenido);
        String referencia=servicio.subir(archivo).get("imagen");
        assertTrue(referencia.matches("subidos/[0-9a-f]{32}\\.png"));
        String nombre=referencia.substring("subidos/".length());
        assertArrayEquals(contenido,Files.readAllBytes(temporal.resolve(nombre)));
        var respuesta=new ImagenProductoService(temporal.toString()).obtener(nombre);
        assertEquals(MediaType.IMAGE_PNG,respuesta.getHeaders().getContentType());
        assertEquals(contenido.length,respuesta.getBody().contentLength());
    }

    @Test void rechazaArchivosQueNoSonImagenes() {
        var servicio=new ImagenProductoService(temporal.toString());
        var falso=new MockMultipartFile("archivo","imagen.png","image/png","no es png".getBytes());
        assertEquals(HttpStatus.BAD_REQUEST,assertThrows(ResponseStatusException.class,()->servicio.subir(falso)).getStatusCode());
    }

    @Test void rechazaNombreDeArchivoConRutas() {
        var servicio=new ImagenProductoService(temporal.toString());
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,()->servicio.obtener("../../etc/passwd")).getStatusCode());
    }

    @Test void noPermiteArchivosSuperioresACincoMegas() {
        var servicio=new ImagenProductoService(temporal.toString());
        var grande=new MockMultipartFile("archivo","foto.jpg","image/jpeg",new byte[5*1024*1024+1]);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE,assertThrows(ResponseStatusException.class,()->servicio.subir(grande)).getStatusCode());
    }
}
