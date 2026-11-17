package com.musica.ms_catalogo.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImagenProductoService {
    private static final long MAX_BYTES=5L*1024*1024;
    private final Path directorio;

    public ImagenProductoService(@Value("${catalogo.upload.dir:/app/uploads/productos}") String directorio) {
        this.directorio=Path.of(directorio).toAbsolutePath().normalize();
    }

    public Map<String, String> subir(MultipartFile archivo) {
        if (archivo==null || archivo.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona una imagen");
        if (archivo.getSize()>MAX_BYTES)
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,"La imagen supera los 5 MB");
        String nombreOriginal=archivo.getOriginalFilename()==null ? "" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        String tipo=archivo.getContentType()==null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
        byte[] primeros=new byte[12];
        try(InputStream entrada=archivo.getInputStream()) {
            int leidos=entrada.readNBytes(primeros,0,primeros.length);
            String extension=extensionComprobada(nombreOriginal,tipo,primeros,leidos);
            String nombre=UUID.randomUUID().toString().replace("-", "")+"."+extension;
            Files.createDirectories(directorio);
            Path destino=directorio.resolve(nombre);
            // La ruta siempre contiene un UUID generado por el servidor.
            try {
                try(InputStream contenido=archivo.getInputStream()) {
                    Files.copy(contenido,destino);
                }
            } catch(Exception e) {
                Files.deleteIfExists(destino);
                throw e;
            }
            return Map.of("imagen","subidos/"+nombre);
        } catch(ResponseStatusException e) {
            throw e;
        } catch(IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"No se pudo guardar la portada",e);
        }
    }

    static String extensionComprobada(String nombre, String tipo, byte[] bytes, int n) {
        boolean jpeg=n>=3 && (bytes[0]&0xff)==0xff && (bytes[1]&0xff)==0xd8 && (bytes[2]&0xff)==0xff;
        boolean png=n>=8 && Arrays.equals(Arrays.copyOf(bytes,8),new byte[]{(byte)137,80,78,71,13,10,26,10});
        boolean webp=n>=12 && bytes[0]=='R' && bytes[1]=='I' && bytes[2]=='F' && bytes[3]=='F'
              && bytes[8]=='W' && bytes[9]=='E' && bytes[10]=='B' && bytes[11]=='P';
        if (jpeg && (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) && "image/jpeg".equals(tipo)) return "jpg";
        if (png && nombre.endsWith(".png") && "image/png".equals(tipo)) return "png";
        if (webp && nombre.endsWith(".webp") && "image/webp".equals(tipo)) return "webp";
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecciona una imagen JPG, PNG o WebP válida");
    }

    public ResponseEntity<Resource> obtener(String nombre) {
        // No aceptar rutas, nombres arbitrarios ni extensiones ejecutables.
        if (!nombre.matches("[0-9a-f]{32}\\.(jpg|png|webp)"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Imagen no encontrada");
        Path ruta=directorio.resolve(nombre).normalize();
        if (!ruta.startsWith(directorio) || !Files.isRegularFile(ruta))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Imagen no encontrada");
        String tipo=nombre.endsWith(".png") ? "image/png" : nombre.endsWith(".webp") ? "image/webp" : "image/jpeg";
        return ResponseEntity.ok()
             .header("X-Content-Type-Options","nosniff")
             .contentType(MediaType.parseMediaType(tipo))
             .cacheControl(CacheControl.noCache())
             .body(new FileSystemResource(ruta));
    }
}
