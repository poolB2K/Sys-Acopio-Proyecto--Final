package pe.com.acopio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "acopios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acopio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String numeroAcopio; // ACO-2025-0001

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fechaAcopio;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column
    private BigDecimal totalPagar;

    @Column(length = 50)
    private String estado = "REGISTRADO"; // REGISTRADO, PAGADO, ANULADO

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @OneToMany(mappedBy = "acopio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcopioDetalle> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (fechaAcopio == null) {
            fechaAcopio = LocalDate.now();
        }
    }

    // Método helper para agregar detalle
    public void addDetalle(AcopioDetalle detalle) {
        detalles.add(detalle);
        detalle.setAcopio(this);
    }

    // Método helper para calcular total
    public void calcularTotal() {
        totalPagar = detalles.stream()
                .map(AcopioDetalle::getTotalAPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}