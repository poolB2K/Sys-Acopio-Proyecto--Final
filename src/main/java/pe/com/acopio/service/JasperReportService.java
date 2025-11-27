package pe.com.acopio.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio centralizado para gestión de reportes JasperReports
 * Proporciona funcionalidades de compilación, caché y exportación a múltiples
 * formatos
 */
@Service
public class JasperReportService {

    private static final Logger logger = LoggerFactory.getLogger(JasperReportService.class);

    // Carpeta para reportes pre-compilados
    private static final String COMPILED_REPORTS_DIR = "jasper/";

    // Carpeta de reportes fuente
    private static final String REPORTS_DIR = "/reports/";

    // Caché de reportes compilados en memoria
    private final Map<String, JasperReport> reportCache = new HashMap<>();

    /**
     * Compila un archivo JRXML a JasperReport
     * Si ya existe el .jasper compilado y es más reciente, lo usa directamente
     */
    public JasperReport compileReport(String reportName) throws JRException {
        logger.info("Compilando reporte: {}", reportName);

        // Verificar caché en memoria
        if (reportCache.containsKey(reportName)) {
            logger.debug("Reporte {} encontrado en caché de memoria", reportName);
            return reportCache.get(reportName);
        }

        String jrxmlPath = REPORTS_DIR + reportName + ".jrxml";
        String jasperPath = COMPILED_REPORTS_DIR + reportName + ".jasper";

        JasperReport jasperReport;

        // Intentar cargar desde archivo .jasper pre-compilado
        File jasperFile = new File(jasperPath);
        if (jasperFile.exists()) {
            try {
                logger.debug("Cargando reporte pre-compilado: {}", jasperPath);
                jasperReport = (JasperReport) JRLoader.loadObject(jasperFile);
                reportCache.put(reportName, jasperReport);
                return jasperReport;
            } catch (JRException e) {
                logger.warn("No se pudo cargar el reporte pre-compilado, compilando desde JRXML", e);
            }
        }

        // Compilar desde JRXML
        InputStream jrxmlStream = getClass().getResourceAsStream(jrxmlPath);
        if (jrxmlStream == null) {
            throw new JRException("No se encontró el archivo de reporte: " + jrxmlPath);
        }

        try {
            jasperReport = JasperCompileManager.compileReport(jrxmlStream);

            // Guardar versión compilada para uso futuro
            saveCompiledReport(jasperReport, jasperPath);

            // Agregar a caché
            reportCache.put(reportName, jasperReport);

            logger.info("Reporte {} compilado exitosamente", reportName);
            return jasperReport;

        } finally {
            try {
                jrxmlStream.close();
            } catch (IOException e) {
                logger.error("Error al cerrar stream del reporte", e);
            }
        }
    }

    /**
     * Guarda un reporte compilado en disco
     */
    private void saveCompiledReport(JasperReport jasperReport, String jasperPath) {
        try {
            Path path = Paths.get(jasperPath);
            Files.createDirectories(path.getParent());

            try (OutputStream outputStream = new FileOutputStream(jasperPath)) {
                JRSaver.saveObject(jasperReport, outputStream);
                logger.debug("Reporte compilado guardado en: {}", jasperPath);
            }
        } catch (Exception e) {
            logger.warn("No se pudo guardar el reporte compilado: {}", e.getMessage());
        }
    }

    /**
     * Genera un JasperPrint a partir de un reporte compilado
     */
    public JasperPrint fillReport(String reportName, Map<String, Object> parameters,
            JRDataSource dataSource) throws JRException {
        JasperReport jasperReport = compileReport(reportName);
        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    /**
     * Exporta un JasperPrint a archivo PDF
     */
    public void exportToPDF(JasperPrint jasperPrint, String outputPath) throws JRException {
        logger.info("Exportando reporte a PDF: {}", outputPath);

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputPath));

        SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
        configuration.setMetadataAuthor("SysAcopio");
        configuration.setMetadataCreator("SysAcopio - Sistema de Acopio de Minerales");

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        logger.info("Reporte exportado exitosamente a PDF");
    }

    /**
     * Exporta un JasperPrint a archivo Excel (XLSX)
     */
    public void exportToExcel(JasperPrint jasperPrint, String outputPath) throws JRException {
        logger.info("Exportando reporte a Excel: {}", outputPath);

        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputPath));

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setDetectCellType(true);
        configuration.setCollapseRowSpan(false);
        configuration.setWhitePageBackground(false);

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        logger.info("Reporte exportado exitosamente a Excel");
    }

    /**
     * Exporta un JasperPrint a HTML
     */
    public void exportToHTML(JasperPrint jasperPrint, String outputPath) throws JRException {
        logger.info("Exportando reporte a HTML: {}", outputPath);
        JasperExportManager.exportReportToHtmlFile(jasperPrint, outputPath);
        logger.info("Reporte exportado exitosamente a HTML");
    }

    /**
     * Muestra el reporte en el visor de JasperReports
     */
    public void viewReport(JasperPrint jasperPrint, String title) {
        logger.info("Mostrando reporte: {}", title);
        net.sf.jasperreports.view.JasperViewer viewer = new net.sf.jasperreports.view.JasperViewer(jasperPrint, false);
        viewer.setTitle(title);
        viewer.setVisible(true);
    }

    /**
     * Obtiene la lista de impresoras disponibles en el sistema
     */
    public List<String> getAvailablePrinters() {
        logger.info("Obteniendo lista de impresoras disponibles");
        List<String> printerNames = new ArrayList<>();

        javax.print.PrintService[] printServices = javax.print.PrintServiceLookup.lookupPrintServices(null, null);

        for (javax.print.PrintService printService : printServices) {
            printerNames.add(printService.getName());
            logger.debug("Impresora encontrada: {}", printService.getName());
        }

        return printerNames;
    }

    /**
     * Obtiene el servicio de impresión por nombre
     */
    private javax.print.PrintService findPrintService(String printerName) {
        javax.print.PrintService[] printServices = javax.print.PrintServiceLookup.lookupPrintServices(null, null);

        for (javax.print.PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                return printService;
            }
        }
        return null;
    }

    /**
     * Imprime directamente a una impresora específica
     */
    public void printDirect(JasperPrint jasperPrint, String printerName) throws JRException {
        logger.info("Imprimiendo directamente a impresora: {}", printerName);

        javax.print.PrintService printService = findPrintService(printerName);
        if (printService == null) {
            throw new JRException("No se encontró la impresora: " + printerName);
        }

        // Configurar el exportador de impresión
        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();
        configuration.setPrintService(printService);
        configuration.setDisplayPageDialog(false);
        configuration.setDisplayPrintDialog(false);

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        logger.info("Reporte impreso exitosamente");
    }

    /**
     * Imprime con diálogo de selección de impresora del sistema
     */
    public void printWithDialog(JasperPrint jasperPrint) throws JRException {
        logger.info("Mostrando diálogo de impresión");

        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();
        configuration.setDisplayPageDialog(false);
        configuration.setDisplayPrintDialog(true); // Mostrar diálogo de impresión

        exporter.setConfiguration(configuration);
        exporter.exportReport();

        logger.info("Impresión completada");
    }

    /**
     * Limpia la caché de reportes compilados
     */
    public void clearCache() {
        logger.info("Limpiando caché de reportes");
        reportCache.clear();
    }

    /**
     * Pre-compila todos los reportes disponibles
     */
    public void preCompileAllReports() {
        logger.info("Pre-compilando todos los reportes disponibles");

        try {
            // Compilar comprobante_acopio
            compileReport("comprobante_acopio");
            logger.info("Reportes pre-compilados exitosamente");
        } catch (Exception e) {
            logger.error("Error al pre-compilar reportes", e);
        }
    }
}
