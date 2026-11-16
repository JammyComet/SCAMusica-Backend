package com.musica.ms_notificacion.service;

import com.musica.ms_notificacion.dto.TiendaContactoDTO;
import com.musica.ms_notificacion.model.Notificacion;
import com.musica.ms_notificacion.repository.NotificacionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TiendaContactoService {
    private final NotificacionRepository repo;
    public TiendaContactoService(NotificacionRepository repo){this.repo=repo;}
    public void enviar(TiendaContactoDTO c){
        if(c==null || c.nombre()==null || c.nombre().isBlank() || c.correo()==null || !c.correo().contains("@") || c.comentario()==null || c.comentario().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Mensaje inválido");
        Notificacion n=new Notificacion();
        n.setMensaje("Contacto de: "+c.nombre()+" <"+c.correo()+">: "+c.comentario());
        n.setEstado("CONTACTO");n.setFechaNotificacion(LocalDate.now());repo.save(n);
    }
    public List<Notificacion> listar(){return repo.findAll().stream().filter(n->"CONTACTO".equals(n.getEstado())).toList();}
}
