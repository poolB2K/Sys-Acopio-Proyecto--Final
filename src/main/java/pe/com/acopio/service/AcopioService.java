package pe.com.acopio.service;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import jakarta.persistence.EntityNotFoundException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.acopio.model.*;
import pe.com.acopio.repository.AcopioDetalleRepository;
import pe.com.acopio.repository.AcopioRepository;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AcopioService {

    private static final String ESTADO_ANULADO = "ANULADO";

    private static final Logger logger = LoggerFactory.getLogger(AcopioService.class);

    private final AcopioRepository acopioRepository;
    private final AcopioDetalleRepository acopioDetalleRepository;
    private final HistorialService historialService;

    /**
     * Crear un nuevo acopio con sus detalles
     */
    public Acopio crear(Acopio acopio, List<AcopioDetalle> detalles) {
        logger.info("Creando nuevo acopio para proveedor: {}", acopio.getProveedor().getNombreCompleto());

        // Generar número de acopio automático
        String numeroAcopio = generarNumeroAcopio();
        acopio.setNumeroAcopio(numeroAcopio);

        // Agregar detalles y calcular cada uno
        int numeroItem = 1;
        for (AcopioDetalle detalle : detalles) {
            detalle.setNumeroItem(numeroItem++);
            detalle.calcular(); // Ejecuta la fórmula del Excel
            acopio.addDetalle(detalle);
        }

        // Calcular total del acopio
        acopio.calcularTotal();

        // Guardar
        Acopio acopioGuardado = acopioRepository.save(acopio);

        // Registrar en historial con ID del acopio para futura reimpresión
        String detallesAdicionales = String.format("{\"acopioId\": %d}", acopioGuardado.getId());
        historialService.logAccion(
                acopioGuardado.getUsuario(), "REGISTRO_ACOPIO",
                "Acopio " + numeroAcopio + " registrado por S/. " + acopio.getTotalPagar(),
                "ACOPIO",
                detallesAdicionales);

        logger.info("Acopio creado exitosamente: {}", numeroAcopio);
        return acopioGuardado;
    }

    /**
     * Genera número de acopio automático: ACO-YYYY-NNNN
     * El contador se basa en todos los acopios del año actual
     */
    private String generarNumeroAcopio() {
        LocalDate hoy = LocalDate.now();
        String año = String.valueOf(hoy.getYear());

        // Contar todos los acopios del año actual (de enero 1 a diciembre 31)
        LocalDate inicioAño = LocalDate.of(hoy.getYear(), 1, 1);
        LocalDate finAño = LocalDate.of(hoy.getYear(), 12, 31);
        Long count = acopioRepository.countByFechaAcopioBetween(inicioAño, finAño);

        String secuencia = String.format("%04d", count + 1);

        return "ACO-" + año + "-" + secuencia;
    }

    /**
     * Obtener acopio por ID con sus detalles
     */
    public Optional<Acopio> obtenerPorId(Long id) {
        return acopioRepository.findById(id);
    }

    /**
     * Obtener todos los acopios
     */
    public List<Acopio> obtenerTodos() {
        return acopioRepository.findAll();
    }

    /**
     * Obtener acopios por proveedor
     */
    public List<Acopio> obtenerPorProveedor(Proveedor proveedor) {
        return acopioRepository.findByProveedor(proveedor);
    }

    /**
     * Obtener acopios por rango de fechas
     */
    public List<Acopio> obtenerPorFechas(LocalDate inicio, LocalDate fin) {
        return acopioRepository.findByFechaAcopioBetween(inicio, fin);
    }

    /**
     * Obtener acopios de hoy
     */
    public List<Acopio> obtenerHoy() {
        return acopioRepository.findByFechaAcopio(LocalDate.now());
    }

    /**
     * Anular acopio
     */
    public void anular(Long id, String motivo) {
        logger.info("Anulando acopio ID: {}", id);

        Acopio acopio = acopioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el acopio con ID: " + id));

        acopio.setEstado(ESTADO_ANULADO);
        acopio.setObservaciones(ESTADO_ANULADO + ": " + motivo);
        acopioRepository.save(acopio);

        historialService.logAccion(
                acopio.getUsuario(), "ANULACION_ACOPIO",
                "Acopio " + acopio.getNumeroAcopio() + " anulado. Motivo" + motivo,
                "ACOPIO");
    }

    /**
     * Generar reporte (voucher) de acopio usando JasperReports
     */
    public JasperPrint generarVoucher(Long acopioId) {
        logger.info("Generando voucher para acopio ID: {}", acopioId);

        try {
            Acopio acopio = acopioRepository.findById(acopioId)
                    .orElseThrow(() -> new EntityNotFoundException("No se encontró el acopio con ID: " + acopioId));

            // Cargar el archivo JRXML
            InputStream reportStream = getClass().getResourceAsStream("/reports/comprobante_acopio.jrxml");
            if (reportStream == null) {
                throw new RuntimeException("No se encontró el archivo de reporte: /reports/comprobante_acopio.jrxml. " +
                        "Asegúrese de que esté en la carpeta 'src/main/resources/reports'");
            }

            // Compilar el reporte
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Preparar parámetros
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("numeroAcopio", acopio.getNumeroAcopio());
            parameters.put("fechaAcopio", acopio.getFechaAcopio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parameters.put("proveedorNombre", acopio.getProveedor().getNombreCompleto());
            parameters.put("proveedorDocumento", acopio.getProveedor().getNumeroDocumento());
            parameters.put("proveedorDireccion", acopio.getProveedor().getDireccion());
            parameters.put("usuarioNombre", acopio.getUsuario().getNombreCompleto());
            parameters.put("totalPagar", acopio.getTotalPagar());
            parameters.put("observaciones", acopio.getObservaciones() != null ? acopio.getObservaciones() : "");

            // Preparar datasource con los detalles
            List<AcopioDetalle> detalles = acopioDetalleRepository.findByAcopioOrderByNumeroItemAsc(acopio);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detalles);

            // Generar el reporte con Locale en inglés para evitar problemas de separador
            // decimal
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            jasperPrint.setLocaleCode("en_US");

            logger.info("Voucher generado exitosamente para acopio: {}", acopio.getNumeroAcopio());

            // Registrar en historial con ID del acopio
            String detallesAdicionales = String.format("{\"acopioId\": %d}", acopio.getId());
            historialService.logAccion(
                    acopio.getUsuario(), "IMPRESION_VOUCHER",
                    "Voucher generado para acopio " + acopio.getNumeroAcopio(),
                    "ACOPIO",
                    detallesAdicionales);

            return jasperPrint;

        } catch (Exception e) {
            logger.error("Error al generar voucher para acopio ID: {}", acopioId, e);
            throw new RuntimeException("Error al generar voucher: " + e.getMessage(), e);
        }
    }

    /**
     * Calcular un detalle usando la fórmula del Excel
     * Este método es público para que pueda ser usado por los controladores
     * para pre-visualizar cálculos antes de guardar
     */
    public AcopioDetalle calcularDetalle(BigDecimal peso, BigDecimal ley, BigDecimal deduccion,
            BigDecimal precioOnza, BigDecimal tipoCambio) {
        AcopioDetalle detalle = new AcopioDetalle();
        detalle.setPeso(peso);
        detalle.setLey(ley);
        detalle.setDeduccion(deduccion);
        detalle.setPrecioOnzaBase(precioOnza);
        detalle.setTipoCambioDolar(tipoCambio);

        detalle.calcular();

        return detalle;
    }
}
