package pe.com.acopio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipoDocumento; // DNI, RUC, CE, etc.

    @Column(nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(nullable = false, length = 200)
    private String nombres;

    @Column(length = 200)
    private String apellidos;

    @Column(length = 250)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String ciudad;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Método helper para obtener nombre completo
    public String getNombreCompleto() {
        if (apellidos != null && !apellidos.isEmpty()) {
            return apellidos + " " + nombres;
        }
        return nombres;
    }
}

