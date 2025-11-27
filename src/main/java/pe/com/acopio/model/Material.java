package pe.com.acopio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "materiales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // ORO, MATE, PLATA, etc.

    @Column(length = 50)
    private String codigo; // AU, MT, AG, etc.

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 20)
    private String unidadMedida = "GRAMOS"; // GRAMOS, KILOGRAMOS, etc.

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
