package pe.com.acopio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.com.acopio.model.Acopio;
import pe.com.acopio.model.Proveedor;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AcopioRepository extends JpaRepository<Acopio, Long> {

    List<Acopio> findByProveedor(Proveedor proveedor);

    List<Acopio> findByFechaAcopioBetween(LocalDate inicio, LocalDate fin);

    List<Acopio> findByFechaAcopio(LocalDate fecha);

    Long countByFechaAcopio(LocalDate fecha);

    Long countByFechaAcopioBetween(LocalDate inicio, LocalDate fin);

    @Query("SELECT MAX(a.numeroAcopio) FROM Acopio a WHERE a.numeroAcopio LIKE :patron")
    String findMaxNumeroAcopio(@org.springframework.data.repository.query.Param("patron") String patron);

    @Query("SELECT a FROM Acopio a ORDER BY a.fechaAcopio DESC")
    List<Acopio> findAllOrderByFechaDesc();
}
