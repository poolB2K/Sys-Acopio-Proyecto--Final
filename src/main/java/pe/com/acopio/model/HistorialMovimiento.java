package pe.com.acopio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String accion; // LOGIN, REGISTRO_ACOPIO, CREACION_PROVEEDOR, etc.

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 50)
    private String modulo; // ACOPIO, PROVEEDOR, SISTEMA, etc.

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 50)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String detallesAdicionales; // JSON con información adicional

    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    // Constructor de conveniencia
    public HistorialMovimiento(Usuario usuario, String accion, String descripcion, String modulo) {
        this.usuario = usuario;
        this.accion = accion;
        this.descripcion = descripcion;
        this.modulo = modulo;
        this.fechaHora = LocalDateTime.now();
    }
}