package pe.com.acopio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.acopio.model.Acopio;
import pe.com.acopio.model.AcopioDetalle;

import java.util.List;

@Repository
public interface AcopioDetalleRepository extends JpaRepository<AcopioDetalle, Long> {

    List<AcopioDetalle> findByAcopioId(Long acopioId);

    List<AcopioDetalle> findByMaterialId(Long materialId);

    List<AcopioDetalle> findByAcopioOrderByNumeroItemAsc(Acopio acopio);
}
