package pe.com.acopio.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.acopio.model.Proveedor;
import pe.com.acopio.repository.ProveedorRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProveedorService {

    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Crear nuevo proveedor
     */
    public Proveedor crear(Proveedor proveedor) {
        logger.info("Creando nuevo proveedor: {}", proveedor.getNumeroDocumento());

        if (proveedorRepository.existsByNumeroDocumento(proveedor.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un proveedor con este número de documento");
        }

        return proveedorRepository.save(proveedor);
    }

    /**
     * Actualizar proveedor
     */
    public Proveedor actualizar(Proveedor proveedor) {
        logger.info("Actualizando proveedor ID: {}", proveedor.getId());

        // Verificar que no exista otro proveedor con el mismo documento
        Optional<Proveedor> existente = proveedorRepository.findByNumeroDocumento(proveedor.getNumeroDocumento());
        if (existente.isPresent() && !existente.get().getId().equals(proveedor.getId())) {
            throw new RuntimeException("Ya existe otro proveedor con este número de documento");
        }

        return proveedorRepository.save(proveedor);
    }

    /**
     * Obtener proveedor por ID
     */
    public Optional<Proveedor> obtenerPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    /**
     * Obtener proveedor por número de documento
     */
    public Optional<Proveedor> obtenerPorDocumento(String numeroDocumento) {
        return proveedorRepository.findByNumeroDocumento(numeroDocumento);
    }

    /**
     * Obtener todos los proveedores activos
     */
    public List<Proveedor> obtenerActivos() {
        return proveedorRepository.findByActivoTrue();
    }

    /**
     * Obtener todos los proveedores
     */
    public List<Proveedor> obtenerTodos() {
        return proveedorRepository.findAll();
    }

    /**
     * Buscar proveedores por nombre o apellido
     */
    public List<Proveedor> buscarPorNombre(String termino) {
        return proveedorRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                termino, termino);
    }

    /**
     * Desactivar proveedor (borrado lógico)
     */
    public void desactivar(Long id) {
        logger.info("Desactivando proveedor ID: {}", id);

        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        if (proveedor.isPresent()) {
            Proveedor p = proveedor.get();
            p.setActivo(false);
            proveedorRepository.save(p);
        }
    }

    /**
     * Activar proveedor
     */
    public void activar(Long id) {
        logger.info("Activando proveedor ID: {}", id);

        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        if (proveedor.isPresent()) {
            Proveedor p = proveedor.get();
            p.setActivo(true);
            proveedorRepository.save(p);
        }
    }

    /**
     * Eliminar proveedor (borrado físico - usar con precaución)
     */
    public void eliminar(Long id) {
        logger.warn("Eliminando físicamente proveedor ID: {}", id);
        proveedorRepository.deleteById(id);
    }
}