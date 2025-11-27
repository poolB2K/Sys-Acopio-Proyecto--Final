package pe.com.acopio.service;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.com.acopio.model.Acopio;
import pe.com.acopio.model.Proveedor;
import pe.com.acopio.repository.AcopioRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generación de reportes consolidados
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteService.class);
    private final JasperReportService jasperReportService;
    private final AcopioRepository acopioRepository;

    /**
     * Genera reporte de acopios por periodo
     */
    public JasperPrint generarReporteAcopiosPeriodo(LocalDate fechaInicio, LocalDate fechaFin) throws JRException {
        logger.info("Generando reporte de acopios del {} al {}", fechaInicio, fechaFin);

        // Obtener acopios del periodo
        List<Acopio> acopios = acopioRepository.findByFechaAcopioBetweenOrderByFechaAcopioDesc(
                fechaInicio, fechaFin);

        // Calcular total
        BigDecimal totalGeneral = acopios.stream()
                .map(Acopio::getTotalPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Preparar parámetros
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fechaInicio", fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("fechaFin", fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        parameters.put("totalGeneral", totalGeneral);
        parameters.put("cantidadAcopios", acopios.size());

        // Preparar datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(acopios);

        // Generar reporte
        JasperPrint jasperPrint = jasperReportService.fillReport(
                "reporte_acopios_periodo", parameters, dataSource);

        logger.info("Reporte de acopios por periodo generado: {} registros", acopios.size());
        return jasperPrint;
    }

    /**
     * Genera reporte histórico de un proveedor
     */
    public JasperPrint generarReporteProveedorHistorico(Proveedor proveedor) throws JRException {
        logger.info("Generando reporte histórico del proveedor: {}", proveedor.getNombreCompleto());

        // Obtener todos los acopios del proveedor
        List<Acopio> acopios = acopioRepository.findByProveedorOrderByFechaAcopioDesc(proveedor);

        // Calcular total pagado
        BigDecimal totalPagado = acopios.stream()
                .map(Acopio::getTotalPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Preparar parámetros
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("proveedorNombre", proveedor.getNombreCompleto());
        parameters.put("proveedorDocumento", proveedor.getTipoDocumento() + " - " + proveedor.getNumeroDocumento());
        parameters.put("proveedorDireccion", proveedor.getDireccion() != null ? proveedor.getDireccion() : "");
        parameters.put("totalAcopios", acopios.size());
        parameters.put("totalPagado", totalPagado);

        // Preparar datasource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(acopios);

        // Generar reporte
        JasperPrint jasperPrint = jasperReportService.fillReport(
                "reporte_proveedor_historico", parameters, dataSource);

        logger.info("Reporte histórico de proveedor generado: {} acopios", acopios.size());
        return jasperPrint;
    }

    /**
     * Genera reporte de inventario de materiales
     * Nota: Este reporte necesitará una estructura DTO para consolidar los datos
     */
    public JasperPrint generarReporteInventarioMateriales() throws JRException {
        logger.info("Generando reporte de inventario de materiales");

        // TODO: Implementar lógica de consolidación de materiales
        // Por ahora, retornamos un reporte básico

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("fechaReporte", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        parameters.put("totalMateriales", 0);

        // Datasource vacío por ahora
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(List.of());

        JasperPrint jasperPrint = jasperReportService.fillReport(
                "reporte_inventario_materiales", parameters, dataSource);

        logger.info("Reporte de inventario generado");
        return jasperPrint;
    }
}
