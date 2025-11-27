package pe.com.acopio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Esta entidad guarda TODOS los campos del cálculo del Excel
 * para trazabilidad total, incluso si los precios cambian después.
 */
@Entity
@Table(name = "acopio_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcopioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "acopio_id", nullable = false)
    private Acopio acopio;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    // ===== CAMPOS DEL EXCEL (INPUTS) =====

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal peso; // Peso en gramos

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal ley; // Ley del material (porcentaje, ej: 95.5)

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal deduccion; // Deducción (porcentaje decimal, ej: 0.10 para 10%)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioOnzaBase; // Precio por onza en dólares (ej: 1800.00)

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal tipoCambioDolar; // Tipo de cambio USD a Soles (ej: 3.75)

    // ===== CAMPOS CALCULADOS (OUTPUTS) =====

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal precioGramoDolares; // Resultado de la fórmula en USD

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal precioGramoSoles; // Precio por gramo en Soles

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAPagar; // Total a pagar = precioGramoSoles * peso

    // ===== CAMPOS ADICIONALES =====

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private Integer numeroItem = 1; // Número de ítem en el acopio

    /**
     * Método para calcular los campos derivados basados en la fórmula del Excel:
     * PrecioGramoDolares = ((PrecioOnzaBase * (Ley / 100)) * (1 - Deduccion)) / 31.1035
     * PrecioGramoSoles = PrecioGramoDolares * Dolar
     * TotalAPagar = PrecioGramoSoles * Peso
     */
    public void calcular() {
        // Constante: 1 onza troy = 31.1035 gramos
        BigDecimal ONZA_TROY_GRAMOS = new BigDecimal("31.1035");
        BigDecimal CIEN = new BigDecimal("100");
        BigDecimal UNO = BigDecimal.ONE;

        // PrecioGramoDolares = ((PrecioOnzaBase * (Ley / 100)) * (1 - Deduccion)) / 31.1035
        BigDecimal leyDecimal = ley.divide(CIEN, 6, BigDecimal.ROUND_HALF_UP);
        BigDecimal precioConLey = precioOnzaBase.multiply(leyDecimal);
        BigDecimal factorDeduccion = UNO.subtract(deduccion);
        BigDecimal precioConDeduccion = precioConLey.multiply(factorDeduccion);
        this.precioGramoDolares = precioConDeduccion.divide(ONZA_TROY_GRAMOS, 6, BigDecimal.ROUND_HALF_UP);

        // PrecioGramoSoles = PrecioGramoDolares * Dolar
        this.precioGramoSoles = this.precioGramoDolares.multiply(tipoCambioDolar);

        // TotalAPagar = PrecioGramoSoles * Peso
        this.totalAPagar = this.precioGramoSoles.multiply(peso).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}