package pe.com.acopio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.acopio.model.Usuario;
import pe.com.acopio.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Autenticar usuario
     */
    public Optional<Usuario> autenticar(String username, String password) {
        logger.info("Intentando autenticar usuario: {}", username);

        // En producción, la contraseña debería estar hasheada (BCrypt)
        Optional<Usuario> usuario = usuarioRepository.findByUsernameAndPassword(username, password);

        if (usuario.isPresent() && usuario.get().getActivo()) {
            logger.info("Usuario autenticado exitosamente: {}", username);
            return usuario;
        }

        logger.warn("Autenticación fallida para usuario: {}", username);
        return Optional.empty();
    }

    /**
     * Crear nuevo usuario
     */
    public Usuario crear(Usuario usuario) {
        logger.info("Creando nuevo usuario: {}", usuario.getUsername());

        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * Actualizar usuario
     */
    public Usuario actualizar(Usuario usuario) {
        logger.info("Actualizando usuario ID: {}", usuario.getId());
        return usuarioRepository.save(usuario);
    }

    /**
     * Obtener usuario por ID
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Obtener todos los usuarios activos
     */
    public List<Usuario> obtenerActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Obtener todos los usuarios
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Desactivar usuario (borrado lógico)
     */
    public void desactivar(Long id) {
        logger.info("Desactivando usuario ID: {}", id);

        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            u.setActivo(false);
            usuarioRepository.save(u);
        }
    }

    /**
     * Cambiar contraseña
     */
    public boolean cambiarPassword(Long id, String passwordAnterior, String passwordNuevo) {
        logger.info("Intentando cambiar contraseña para usuario ID: {}", id);

        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            if (u.getPassword().equals(passwordAnterior)) {
                u.setPassword(passwordNuevo);
                usuarioRepository.save(u);
                logger.info("Contraseña cambiada exitosamente para usuario ID: {}", id);
                return true;
            }
        }

        return false;
    }
}
