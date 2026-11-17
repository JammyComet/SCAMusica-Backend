package com.musica.ms_auth.service;

import com.musica.ms_auth.dto.TiendaUsuarioDTO;
import com.musica.ms_auth.model.Usuario;
import com.musica.ms_auth.repository.UsuarioRepository;
import com.musica.ms_auth.repository.SesionRefreshRepository;
import com.musica.ms_auth.security.JwtService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TiendaUsuarioService {
    private final UsuarioRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    @org.springframework.beans.factory.annotation.Autowired(required=false)
    private SesionRefreshRepository sesiones;
    public TiendaUsuarioService(UsuarioRepository repo, BCryptPasswordEncoder encoder, JwtService jwt) {
        this.repo=repo; this.encoder=encoder; this.jwt=jwt;
    }
    public TiendaUsuarioDTO dto(Usuario u) {
        TiendaUsuarioDTO d=new TiendaUsuarioDTO();
        d.setId(u.getId()); d.setCorreo(u.getUsername());
        d.setRol(switch (u.getRol().toUpperCase()) { case "ADMIN" -> "Administrador"; case "VENDEDOR" -> "Vendedor"; default -> "Cliente"; });
        d.setRun(u.getRun()); d.setNombre(u.getNombre()); d.setApellidos(u.getApellidos());
        d.setFechaNacimiento(u.getFechaNacimiento()); d.setTelefono(u.getTelefono());
        d.setRegion(u.getRegion()); d.setComuna(u.getComuna()); d.setDireccion(u.getDireccion());
        d.setActivo(u.isActivo());
        return d;
    }
    public Usuario desdeToken(String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) throw new IllegalArgumentException("Sin token");
            var claims=jwt.verificar(header.substring(7));
            Long id=((Number)claims.get("usuarioId")).longValue();
            Usuario u=repo.findById(id).orElseThrow();
            if (!u.isActivo() || !u.getUsername().equals(claims.getSubject()) || !u.getRol().equals(claims.get("rol"))) throw new IllegalArgumentException("Token inválido");
            return u;
        } catch (Exception ex) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sesión inválida o expirada"); }
    }
    public String token(Usuario u) { return jwt.generarToken(u.getId(),u.getUsername(),u.getRol(),u.getClienteId()); }
    public com.musica.ms_auth.dto.LoginResponseDTO login(com.musica.ms_auth.dto.LoginRequestDTO d) {
        if (d==null || d.getUsername()==null || d.getPassword()==null || d.getUsername().isBlank() || d.getPassword().isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas");
        String correo=d.getUsername().trim().toLowerCase();
        Usuario u=repo.findByUsername(correo).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas"));
        if (!u.isActivo() || !passwordValidaYActualizarSiEsLegacy(u,d.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas");
        return new com.musica.ms_auth.dto.LoginResponseDTO(token(u),u.getId(),u.getUsername(),u.getRol(),u.getClienteId());
    }
    private boolean passwordValidaYActualizarSiEsLegacy(Usuario u,String password) {
        String hash=u.getPassword();
        if(hash==null)return false;
        if(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))
            return encoder.matches(password,hash);
        if(!hash.equals(password))return false;
        u.setPassword(encoder.encode(password));
        repo.save(u);
        return true;
    }
    public List<TiendaUsuarioDTO> listar() { return repo.findAll().stream().map(this::dto).toList(); }
    public TiendaUsuarioDTO uno(Long id) { return dto(repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND))); }
    public TiendaUsuarioDTO crear(TiendaUsuarioDTO d, boolean publico) {
        if (d.getCorreo()==null || d.getContrasena()==null || d.getContrasena().length()<8 || d.getContrasena().length()>72) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Se requiere correo y contraseña de 8 a 72 caracteres");
        String correo=d.getCorreo().trim().toLowerCase();
        if(repo.existsByUsername(correo)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Correo ya registrado");
        if(d.getRun()!=null && !d.getRun().isBlank() && repo.existsByRun(d.getRun().trim())) throw new ResponseStatusException(HttpStatus.CONFLICT,"RUN duplicado");
        Usuario u=new Usuario(); u.setUsername(correo); u.setFechaRegistro(LocalDate.now());
        u.setRol(publico ? "CLIENTE" : rol(d.getRol())); u.setActivo(true);
        u.setPassword(encoder.encode(d.getContrasena()));
        asignar(u,d,false);
        return dto(repo.save(u));
    }
    @org.springframework.transaction.annotation.Transactional
    public TiendaUsuarioDTO actualizar(Long id,TiendaUsuarioDTO d, boolean propio, String header) {
        boolean revocar = false;
        Usuario u=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (propio && !u.getId().equals(desdeToken(header).getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (!propio && d.getRun()!=null && !d.getRun().equals(u.getRun()) && repo.existsByRun(d.getRun().trim())) throw new ResponseStatusException(HttpStatus.CONFLICT,"RUN duplicado");
        if (!propio && d.getRol()!=null) {
            if (u.getUsername().equals("admin@duoc.cl") && !"ADMIN".equals(rol(d.getRol()))) throw new ResponseStatusException(HttpStatus.CONFLICT,"El administrador principal no puede cambiar de rol");
            String rolNuevo=rol(d.getRol());
            if(!rolNuevo.equals(u.getRol())) revocar=true;
            u.setRol(rolNuevo);
        }
        if (!propio && d.getActivo()!=null && !u.getUsername().equals("admin@duoc.cl")) {
            if(u.isActivo()!=d.getActivo()) revocar=true;
            u.setActivo(d.getActivo());
        }
        if (d.getContrasena()!=null && !d.getContrasena().isBlank()) {
            if(d.getContrasena().length()<8 || d.getContrasena().length()>72)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La nueva contraseña debe tener entre 8 y 72 caracteres");
            if (propio && !verificarContrasenaActual(u,d.getContrasenaActual())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Contraseña actual incorrecta");
            u.setPassword(encoder.encode(d.getContrasena()));
            revocar=true;
        }
        asignar(u,d,propio);
        TiendaUsuarioDTO resultado = dto(repo.save(u));
        if(revocar && sesiones!=null) sesiones.deleteByUsuarioId(id);
        return resultado;
    }
    private boolean verificarContrasenaActual(Usuario usuario, String actual) {
        if (actual == null || usuario.getPassword() == null) return false;
        String guardada = usuario.getPassword();
        if (guardada.startsWith("$2a$") || guardada.startsWith("$2b$") || guardada.startsWith("$2y$"))
            return encoder.matches(actual, guardada);
        // Compatibilidad con contraseñas antiguas: siempre se sustituye por BCrypt al cambiarla.
        return guardada.equals(actual);
    }
    private void asignar(Usuario u,TiendaUsuarioDTO d,boolean propio) {
        if (d.getRun()!=null && !propio) u.setRun(d.getRun());
        if (d.getNombre()!=null) u.setNombre(d.getNombre());
        if (d.getApellidos()!=null) u.setApellidos(d.getApellidos());
        if (d.getFechaNacimiento()!=null) u.setFechaNacimiento(d.getFechaNacimiento());
        if (d.getTelefono()!=null) u.setTelefono(d.getTelefono());
        if (d.getRegion()!=null) u.setRegion(d.getRegion());
        if (d.getComuna()!=null) u.setComuna(d.getComuna());
        if (d.getDireccion()!=null) u.setDireccion(d.getDireccion());
    }
    @org.springframework.transaction.annotation.Transactional
    public void eliminar(Long id) {
        Usuario u=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (u.getUsername().equals("admin@duoc.cl")) throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede eliminar al administrador principal");
        if(sesiones!=null) sesiones.deleteByUsuarioId(id);
        repo.delete(u);
    }
    private String rol(String rol) { return switch(String.valueOf(rol).toUpperCase()) {
        case "ADMINISTRADOR", "ADMIN" -> "ADMIN"; case "VENDEDOR" -> "VENDEDOR"; case "CLIENTE" -> "CLIENTE";
        default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rol inválido");
    }; }
    @Bean
    public CommandLineRunner sembrarUsuariosTienda() {
        return args -> {
            if(!"true".equalsIgnoreCase(System.getenv("SEED_DEMO_USERS"))) return;
            String admin=System.getenv("DEMO_ADMIN_PASSWORD");
            String vendedor=System.getenv("DEMO_VENDEDOR_PASSWORD");
            String cliente=System.getenv("DEMO_CLIENTE_PASSWORD");
            if(admin==null || admin.length()<8 || admin.length()>72 || vendedor==null || vendedor.length()<8 || vendedor.length()>72 || cliente==null || cliente.length()<8 || cliente.length()>72)
                throw new IllegalStateException("Para SEED_DEMO_USERS=true configura las 3 contraseñas DEMO_*_PASSWORD (mínimo 8 caracteres)");
            // El restablecimiento es explícito y está restringido a cuentas demo locales.
            boolean resetDemo = "true".equalsIgnoreCase(System.getenv("RESET_DEMO_PASSWORDS"));
            if (resetDemo && !"true".equalsIgnoreCase(System.getenv("LOCAL_DEMO_MODE")))
                throw new IllegalStateException("RESET_DEMO_PASSWORDS solo se permite en el entorno Docker de desarrollo");
            semilla("admin@duoc.cl",admin,"ADMIN","99999999","Administrador","SCAMusica","1980-01-01","911111111",null,null,null);
            semilla("vendedor@duoc.cl",vendedor,"VENDEDOR","88888888","Vendedor","SCAMusica","1990-01-01","913333333",null,null,null);
            semilla("cliente@gmail.com",cliente,"CLIENTE","11111111","Cliente","SCAMusica","2000-01-01","912345678","Región Metropolitana de Santiago","Santiago","Av. Música 123");
        };
    }
    private void semilla(String correo,String pass,String rol,String run,String nombre,String apellido,String nacimiento,String telefono,String region,String comuna,String direccion) {
        if (repo.existsByUsername(correo)) {
            // No tocar contraseñas existentes en cada arranque. Solamente cuando
            // el usuario invoca el script local actualizar-claves-demo.ps1.
            if ("true".equalsIgnoreCase(System.getenv("RESET_DEMO_PASSWORDS"))) {
                Usuario actual=repo.findByUsername(correo).orElseThrow();
                actual.setPassword(encoder.encode(pass));
                repo.save(actual);
                if(sesiones!=null) sesiones.deleteByUsuarioId(actual.getId());
            }
            return;
        }
        Usuario u=new Usuario(); u.setUsername(correo); u.setPassword(encoder.encode(pass)); u.setRol(rol);
        u.setFechaRegistro(LocalDate.now()); u.setRun(run); u.setNombre(nombre); u.setApellidos(apellido);
        u.setFechaNacimiento(LocalDate.parse(nacimiento)); u.setTelefono(telefono); u.setRegion(region); u.setComuna(comuna); u.setDireccion(direccion); u.setActivo(true);
        repo.save(u);
    }
}
