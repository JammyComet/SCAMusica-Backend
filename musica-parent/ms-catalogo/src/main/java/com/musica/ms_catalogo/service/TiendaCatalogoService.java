package com.musica.ms_catalogo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musica.ms_catalogo.model.Catalogo;
import com.musica.ms_catalogo.repository.CatalogoRepository;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TiendaCatalogoService {
    private final CatalogoRepository productos;
    private final com.musica.ms_catalogo.client.CategoriasClient categorias;
    public TiendaCatalogoService(CatalogoRepository p,com.musica.ms_catalogo.client.CategoriasClient c) {productos=p;categorias=c;}
    public List<Catalogo> listar() {return productos.findAll().stream().filter(p->p.getCodigo()!=null).toList();}
    public Catalogo porCodigo(String codigo) {return productos.findByCodigo(codigo).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Producto no encontrado"));}
    public Catalogo guardar(Catalogo p, String codigoOriginal) {
        if(p==null || p.getCodigo()==null || p.getCodigo().isBlank() || p.getNombre()==null || p.getNombre().isBlank() || p.getArtista()==null || p.getArtista().isBlank() || p.getFormato()==null || p.getFormato().isBlank() || p.getEstado()==null || !List.of("Activo","Inactivo").contains(p.getEstado()) || p.getPrecio()<0 || p.getStock()==null || p.getStock()<0 || (p.getDescuento()!=null && (p.getDescuento()<0 || p.getDescuento()>100)) || (p.getStockCritico()!=null && p.getStockCritico()<0) || p.getCategoriaId()==null || !categorias.existe(p.getCategoriaId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Datos de producto incompletos");
        Catalogo destino;
        if(codigoOriginal==null) {
            if(productos.existsByCodigo(p.getCodigo())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Código duplicado");
            destino=new Catalogo();
        } else {
            destino=porCodigo(codigoOriginal);
            if (!p.getCodigo().equals(codigoOriginal) && productos.existsByCodigo(p.getCodigo())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Código duplicado");
        }
        destino.setCodigo(p.getCodigo());destino.setNombre(p.getNombre());destino.setArtista(p.getArtista());
        destino.setDescripcion(p.getDescripcion());destino.setPrecio(p.getPrecio());destino.setStock(p.getStock());
        destino.setStockCritico(p.getStockCritico());destino.setCategoriaId(p.getCategoriaId());
        destino.setFormato(p.getFormato());destino.setGenero(p.getGenero());destino.setAnio(p.getAnio());
        destino.setImagen(p.getImagen());destino.setEstado(p.getEstado());destino.setDescuento(p.getDescuento());
        return productos.save(destino);
    }
    public void eliminar(String codigo){productos.delete(porCodigo(codigo));}
    @Transactional
    public void descontarLote(List<Map<String,Object>> lineas){
        if(lineas==null || lineas.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Pedido vacío");
        Map<String,Integer> totalPorCodigo=new java.util.TreeMap<>();
        for(var linea:lineas){
            if(linea.get("codigo")==null || !(linea.get("cantidad") instanceof Number)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Producto y cantidad obligatorios");
            String codigo=String.valueOf(linea.get("codigo"));
            double valor=((Number)linea.get("cantidad")).doubleValue();
            if(!Double.isFinite(valor) || valor<1 || valor>Integer.MAX_VALUE || valor!=Math.rint(valor))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cantidad inválida");
            int cantidad=(int)valor;
            try{totalPorCodigo.merge(codigo,cantidad,Math::addExact);}
            catch(ArithmeticException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cantidad excesiva");}
        }
        var bloqueados=new java.util.ArrayList<Catalogo>();
        for(var entrada:totalPorCodigo.entrySet()){
            Catalogo p=productos.bloquearCodigo(entrada.getKey()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Producto no encontrado"));
            if(p.getStock()==null || p.getStock()<entrada.getValue() || !"Activo".equals(p.getEstado())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Stock insuficiente: "+entrada.getKey());
            bloqueados.add(p);
        }
        for(var p:bloqueados){p.setStock(p.getStock()-totalPorCodigo.get(p.getCodigo()));productos.save(p);}
    }
    @Bean
    public CommandLineRunner sembrarTienda(ObjectMapper mapper) {
        return args -> {
            var ps=mapper.readValue(new ClassPathResource("tienda-productos.json").getInputStream(),new TypeReference<List<Catalogo>>(){});
            for(var p:ps) if(!productos.existsByCodigo(p.getCodigo())) {
                p.setId(null);
                // Los IDs proceden del catálogo de categorías sembrado en ms-categorias.
                productos.save(p);
            }
        };
    }
}
