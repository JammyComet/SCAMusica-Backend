package com.musica.ms_usuarios.service;
import com.musica.ms_usuarios.client.AuthUsuariosClient;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
@Service
public class UsuariosAdminService {
 private final AuthUsuariosClient auth;
 public UsuariosAdminService(AuthUsuariosClient auth){this.auth=auth;}
 public List<Map<String,Object>> listar(String jwt){return auth.listar(jwt);}
 public Map<String,Object> crear(String jwt,Map<String,Object> datos){return auth.crear(jwt,datos);}
 public Map<String,Object> actualizar(String jwt,Long id,Map<String,Object> datos){return auth.editar(jwt,id,datos);}
 public void eliminar(String jwt,Long id){auth.eliminar(jwt,id);}
}
