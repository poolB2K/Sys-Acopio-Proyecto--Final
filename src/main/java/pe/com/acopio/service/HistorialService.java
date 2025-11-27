package pe.com.acopio.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.acopio.model.HistorialMovimiento;
import pe.com.acopio.model.Usuario;
import pe.com.acopio.repository.HistorialMovimientoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor // Inyección de dependencias por constructor con Lombok
public class HistorialService {

    private static final Logger logger = LoggerFactory.getLogger(HistorialService.class);

    private final HistorialMovimientoRepository historialRepository;

    /**
     * Registra una acción en el historial para un usuario específico.
     * El usuario debe ser proporcionado por la capa que llama al servicio (ej. Controller).
     *
     * @param usuario     El usuario que realiza la acción.
     * @param accion      El tipo de acción (ej. "REGISTRO_ACOPIO").
     * @param descripcion Un detalle de la acción.
     * @param modulo      El módulo del sistema donde ocurrió la acción (ej. "ACOPIO").
     */
    @Transactional
    public void logAccion(Usuario usuario, String accion, String descripcion, String modulo) {
        if (usuario == null) {
            logger.error("Intento de registrar acción '{}' con un usuario nulo.", accion);
            // Opcional: lanzar una excepción si un usuario nulo es un estado inválido.
            // throw new IllegalArgumentException("El usuario no puede ser nulo para registrar una acción.");
            return; // O continuar si es un caso de uso válido (ej. acciones del sistema)
        }

        HistorialMovimiento movimiento = new HistorialMovimiento();
        movimiento.setUsuario(usuario);
        movimiento.setAccion(accion);
        movimiento.setDescripcion(descripcion);
        movimiento.setModulo(modulo);
        movimiento.setFechaHora(LocalDateTime.now());

        historialRepository.save(movimiento);
        logger.info("Acción registrada: {} - {} por usuario {}", accion, descripcion, usuario.getUsername());
    }

    /**
     * Obtener historial completo ordenado por fecha descendente
     */
    @Transactional(readOnly = true)
    public List<HistorialMovimiento> obtenerTodos() {
        return historialRepository.findAllOrderByFechaDesc();
    }

    /**
     * Obtener historial de un usuario específico
     */
    @Transactional(readOnly = true)
    public List<HistorialMovimiento> obtenerPorUsuario(Usuario usuario) {
        return historialRepository.findByUsuario(usuario);
    }

    /**
     * Obtener historial de un módulo específico
     */
    @Transactional(readOnly = true)
    public List<HistorialMovimiento> obtenerPorModulo(String modulo) {
        return historialRepository.findByModulo(modulo);
    }

    /**
     * Obtener historial entre fechas
     */
    @Transactional(readOnly = true)
    public List<HistorialMovimiento> obtenerPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return historialRepository.findByFechaHoraBetween(inicio, fin);
    }

    /**
     * Obtener historial reciente (últimas 24 horas)
     */
    @Transactional(readOnly = true)
    public List<HistorialMovimiento> obtenerRecientes() {
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
        return historialRepository.findRecientes(hace24Horas);
    }

    /**
     * Obtener historial de hoy
     */
    @Transactional(readOnly = true)
    public List<HistorialMovimiento> obtenerHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime finHoy = inicioHoy.plusDays(1);
        return historialRepository.findByFechaHoraBetween(inicioHoy, finHoy);
    }
}
