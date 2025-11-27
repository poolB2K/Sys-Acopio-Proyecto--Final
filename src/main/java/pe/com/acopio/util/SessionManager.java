package pe.com.acopio.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pe.com.acopio.model.Usuario;

/**
 * Bean gestionado por Spring para manejar la sesión del usuario en la
 * aplicación JavaFX.
 * Singleton que mantiene el estado del usuario actual en la aplicación de
 * escritorio.
 */
@Component
@Scope("singleton")
public class SessionManager {

    private Usuario usuarioActual;

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
