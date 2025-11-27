package pe.com.acopio.service;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

/**
 * Servicio para manejar la impresión de reportes JasperReports
 */
@Service
public class PrinterService {

    private static final Logger logger = LoggerFactory.getLogger(PrinterService.class);

    /**
     * Imprime un reporte directamente a la impresora predeterminada del sistema
     * Si no hay impresora disponible, lanza una excepción con mensaje claro
     *
     * @param jasperPrint Reporte a imprimir
     * @throws Exception Si no hay impresora o hay error de impresión
     */
    public void printToDefaultPrinter(JasperPrint jasperPrint) throws Exception {
        if (jasperPrint == null) {
            throw new IllegalArgumentException("El reporte a imprimir no puede ser nulo");
        }

        // Obtener impresora predeterminada
        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultPrinter == null) {
            // No hay impresora predeterminada, buscar cualquier impresora disponible
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

            if (printServices == null || printServices.length == 0) {
                throw new Exception("No se encontró ninguna impresora instalada en el sistema.\n" +
                        "Por favor, instale una impresora o configure una impresora PDF.");
            }

            // Usar la primera impresora disponible
            defaultPrinter = printServices[0];
            logger.warn("No hay impresora predeterminada. Usando: {}", defaultPrinter.getName());
        }

        logger.info("Imprimiendo en: {}", defaultPrinter.getName());

        try {
            // Configurar el exportador de impresión
            JRPrintServiceExporter exporter = new JRPrintServiceExporter();

            // Configurar el input (el reporte)
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

            // Configurar atributos de impresión
            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();

            // Configurar la impresora
            SimplePrintServiceExporterConfiguration configuration = new SimplePrintServiceExporterConfiguration();
            configuration.setPrintService(defaultPrinter);
            configuration.setPrintRequestAttributeSet(printRequestAttributeSet);
            configuration.setDisplayPageDialog(false); // No mostrar diálogo de página
            configuration.setDisplayPrintDialog(false); // No mostrar diálogo de impresión

            exporter.setConfiguration(configuration);

            // Imprimir
            exporter.exportReport();

            logger.info("Reporte enviado a impresora exitosamente");

        } catch (Exception e) {
            logger.error("Error al imprimir reporte", e);
            throw new Exception("Error al imprimir: " + e.getMessage() +
                    "\nVerifique que la impresora esté encendida y tenga papel.", e);
        }
    }

    /**
     * Obtiene el nombre de la impresora predeterminada
     *
     * @return Nombre de la impresora o mensaje si no hay
     */
    public String getDefaultPrinterName() {
        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultPrinter != null) {
            return defaultPrinter.getName();
        }

        // Buscar cualquier impresora
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        if (printServices != null && printServices.length > 0) {
            return printServices[0].getName() + " (primera disponible)";
        }

        return "Ninguna impresora disponible";
    }

    /**
     * Verifica si hay al menos una impresora disponible
     *
     * @return true si hay impresora disponible
     */
    public boolean isPrinterAvailable() {
        PrintService defaultPrinter = PrintServiceLookup.lookupDefaultPrintService();
        if (defaultPrinter != null) {
            return true;
        }

        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        return printServices != null && printServices.length > 0;
    }
}
