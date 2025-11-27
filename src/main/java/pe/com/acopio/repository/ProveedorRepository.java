package pe.com.acopio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.acopio.model.Proveedor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

        List<Proveedor> findByActivoTrue();

        List<Proveedor> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
                        String nombres, String apellido);

        // Métodos adicionales requeridos por ProveedorService
        boolean existsByNumeroDocumento(String numeroDocumento);

        Optional<Proveedor> findByNumeroDocumento(String numeroDocumento);
}
