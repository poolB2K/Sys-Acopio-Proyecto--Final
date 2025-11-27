import re

# Leer el archivo
with open(r'd:\programacion\SysAcopio\SysAcopio\src\main\java\pe\com\acopio\controller\HistorialController.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Agregar imports necesarios si no existen
if 'import pe.com.acopio.service.AcopioService;' not in content:
    content = content.replace(
        'import pe.com.acopio.service.HistorialService;',
        'import pe.com.acopio.service.HistorialService;\nimport pe.com.acopio.service.AcopioService;\nimport pe.com.acopio.service.JasperReportService;\nimport net.sf.jasperreports.engine.JasperPrint;\nimport pe.com.acopio.util.ReportAlert;'
    )

# Agregar inyección de servicios
if '@Autowired\n    private AcopioService acopioService;' not in content:
    content = content.replace(
        '@Autowired\n    private HistorialService historialService;',
        '@Autowired\n    private HistorialService historialService;\n\n    @Autowired\n    private AcopioService acopioService;\n\n    @Autowired\n    private JasperReportService jasperReportService;'
    )

# Agregar métodos antes del último }
nuevo_codigo = '''
    /**
     * Maneja la reimpresión del voucher desde el historial
     */
    @FXML
    private void handleReimprimirVoucher() {
        HistorialMovimiento seleccionado = tblHistorial.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            ReportAlert.showWarning("Selección", "Por favor seleccione un movimiento del historial");
            return;
        }

        // Verificar que sea una impresión de voucher
        if (!"IMPRESION_VOUCHER".equals(seleccionado.getAccion())) {
            ReportAlert.showWarning("Tipo Incorrecto",
                    "El movimiento seleccionado no es una generación de voucher.\\n\\n" +
                            "Por favor seleccione un registro con acción 'IMPRESION_VOUCHER'");
            return;
        }

        // Extraer el ID del acopio de los detalles adicionales
        String detalles = seleccionado.getDetallesAdicionales();
        if (detalles == null || detalles.trim().isEmpty()) {
            ReportAlert.showError("Error", "No se pudo obtener el ID del acopio desde el historial");
            return;
        }

        try {
            // Buscar el patrón "AcopioID:" y extraer el número
            String detallesLimpio = detalles.trim();
            int index = detallesLimpio.indexOf("AcopioID:");
            
            if (index == -1) {
                ReportAlert.showError("Error", "No se encontró el ID del acopio en los detalles");
                return;
            }

            // Extraer la parte después de "AcopioID:"
            String idStr = detallesLimpio.substring(index + 9).trim(); // 9 = length of "AcopioID:"
            
            // Tomar solo la primera línea si hay múltiples líneas
            if ("\\n" in idStr) {
                idStr = idStr.split("\\n")[0].trim();
            }
            if ("\\r" in idStr) {
                idStr = idStr.split("\\r")[0].trim();
            }
            
            // Extraer solo los dígitos al inicio
            StringBuilder numStr = new StringBuilder();
            for (char c : idStr.toCharArray()) {
                if (Character.isDigit(c)) {
                    numStr.append(c);
                } else {
                    break;
                }
            }
            
            if (numStr.length() == 0) {
                ReportAlert.showError("Error", "El ID del acopio no tiene un formato válido");
                return;
            }
            
            Long acopioId = Long.parseLong(numStr.toString());

            // Generar el voucher
            JasperPrint jasperPrint = acopioService.generarVoucher(acopioId);

            // Mostrar opciones al usuario
            showVoucherOptionsDialog(jasperPrint, acopioId);

        } catch (NumberFormatException e) {
            ReportAlert.showError("Error", "El ID del acopio no tiene un formato válido: " + e.getMessage());
        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al generar el voucher: " + e.getMessage());
        }
    }

    private void showVoucherOptionsDialog(JasperPrint jasperPrint, Long acopioId) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "Ver en Pantalla",
                "Ver en Pantalla", "Imprimir Directamente", "Exportar PDF", "Exportar Excel");
        dialog.setTitle("Voucher Generado");
        dialog.setHeaderText("¿Qué desea hacer con el voucher?");
        dialog.setContentText("Seleccione una opción:");

        dialog.showAndWait().ifPresent(opcion -> {
            try {
                switch (opcion) {
                    case "Ver en Pantalla":
                        ReportAlert.showReport(jasperPrint, "Voucher de Acopio");
                        break;
                    case "Imprimir Directamente":
                        handleImprimirDirecto(jasperPrint);
                        break;
                    case "Exportar PDF":
                        exportarAPDF(jasperPrint, acopioId);
                        break;
                    case "Exportar Excel":
                        exportarAExcel(jasperPrint, acopioId);
                        break;
                }
            } catch (Exception e) {
                ReportAlert.showError("Error", "Error al procesar voucher: " + e.getMessage());
            }
        });
    }

    private void handleImprimirDirecto(JasperPrint jasperPrint) {
        try {
            java.util.List<String> impresoras = jasperReportService.getAvailablePrinters();
            if (impresoras.isEmpty()) {
                ReportAlert.showWarning("Sin Impresoras",
                        "No se encontraron impresoras disponibles en el sistema");
                return;
            }
            ChoiceDialog<String> dialog = new ChoiceDialog<>(impresoras.get(0), impresoras);
            dialog.setTitle("Seleccionar Impresora");
            dialog.setHeaderText("Impresión Directa");
            dialog.setContentText("Seleccione la impresora:");
            dialog.showAndWait().ifPresent(impresora -> {
                try {
                    jasperReportService.printDirect(jasperPrint, impresora);
                    ReportAlert.showSuccess("Éxito",
                            "Voucher enviado a impresora: " + impresora);
                } catch (Exception e) {
                    ReportAlert.showError("Error de Impresión",
                            "Error al imprimir: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al obtener impresoras: " + e.getMessage());
        }
    }

    private void exportarAPDF(JasperPrint jasperPrint, Long acopioId) {
        try {
            java.io.File file = ReportAlert.showPDFSaveDialog("Voucher_Acopio_" + acopioId);
            if (file != null) {
                jasperReportService.exportToPDF(jasperPrint, file.getAbsolutePath());
                ReportAlert.showSuccess("Éxito",
                        "Voucher exportado exitosamente a:\\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al exportar a PDF: " + e.getMessage());
        }
    }

    private void exportarAExcel(JasperPrint jasperPrint, Long acopioId) {
        try {
            java.io.File file = ReportAlert.showExcelSaveDialog("Voucher_Acopio_" + acopioId);
            if (file != null) {
                jasperReportService.exportToExcel(jasperPrint, file.getAbsolutePath());
                ReportAlert.showSuccess("Éxito",
                        "Voucher exportado exitosamente a:\\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al exportar a Excel: " + e.getMessage());
        }
    }
'''

# Insertar antes del último }
content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + nuevo_codigo + '\n}'

# Escribir el archivo
with open(r'd:\programacion\SysAcopio\SysAcopio\src\main\java\pe\com\acopio\controller\HistorialController.java', 'w', encoding='utf-8') as f:
    f.write(content)

print("HistorialController.java actualizado exitosamente")
