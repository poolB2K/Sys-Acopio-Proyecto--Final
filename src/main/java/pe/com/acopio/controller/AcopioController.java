package pe.com.acopio.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import pe.com.acopio.model.*;
import pe.com.acopio.repository.MaterialRepository;
import pe.com.acopio.service.AcopioService;
import pe.com.acopio.service.ProveedorService;
import pe.com.acopio.util.ReportAlert;
import pe.com.acopio.util.SessionManager;
import net.sf.jasperreports.engine.JasperPrint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AcopioController {

    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Material> cmbMaterial;
    @FXML private TextField txtPeso;
    @FXML private TextField txtLey;
    @FXML private TextField txtDeduccion;
    @FXML private TextField txtPrecioOnza;
    @FXML private TextField txtTipoCambio;
    @FXML private TextField txtPrecioGramoDolares;
    @FXML private TextField txtPrecioGramoSoles;
    @FXML private TextField txtTotalItem;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblTotalGeneral;

    @FXML private TableView<AcopioDetalle> tblDetalles;
    @FXML private TableColumn<AcopioDetalle, Integer> colItem;
    @FXML private TableColumn<AcopioDetalle, String> colMaterial;
    @FXML private TableColumn<AcopioDetalle, BigDecimal> colPeso;
    @FXML private TableColumn<AcopioDetalle, BigDecimal> colLey;
    @FXML private TableColumn<AcopioDetalle, BigDecimal> colDeduccion;
    @FXML private TableColumn<AcopioDetalle, BigDecimal> colPrecioOnza;
    @FXML private TableColumn<AcopioDetalle, BigDecimal> colTipoCambio;
    @FXML private TableColumn<AcopioDetalle, BigDecimal> colPrecioGramo;
    @FXML private TableColumn<AcopioDetalle, BigDecimal> colTotal;

    private final ProveedorService proveedorService;
    private final AcopioService acopioService;
    private final MaterialRepository materialRepository;
    private final SessionManager sessionManager;

    private ObservableList<AcopioDetalle> detallesObservable = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTabla();
        cargarProveedores();
        cargarMateriales();

        // Establecer fecha actual
        dpFecha.setValue(LocalDate.now());

        // Configurar listeners para cálculo automático
        txtPeso.textProperty().addListener((obs, old, newVal) -> calcularPreview());
        txtLey.textProperty().addListener((obs, old, newVal) -> calcularPreview());
        txtDeduccion.textProperty().addListener((obs, old, newVal) -> calcularPreview());
        txtPrecioOnza.textProperty().addListener((obs, old, newVal) -> calcularPreview());
        txtTipoCambio.textProperty().addListener((obs, old, newVal) -> calcularPreview());

        // Valores por defecto
        txtDeduccion.setText("0.10"); // 10%
        txtTipoCambio.setText("3.75"); // Tipo de cambio ejemplo
    }

    private void configurarTabla() {
        colItem.setCellValueFactory(new PropertyValueFactory<>("numeroItem"));
        colMaterial.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMaterial().getNombre()));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colLey.setCellValueFactory(new PropertyValueFactory<>("ley"));
        colDeduccion.setCellValueFactory(new PropertyValueFactory<>("deduccion"));
        colPrecioOnza.setCellValueFactory(new PropertyValueFactory<>("precioOnzaBase"));
        colTipoCambio.setCellValueFactory(new PropertyValueFactory<>("tipoCambioDolar"));
        colPrecioGramo.setCellValueFactory(new PropertyValueFactory<>("precioGramoSoles"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAPagar"));

        tblDetalles.setItems(detallesObservable);
    }

    private void cargarProveedores() {
        List<Proveedor> proveedores = proveedorService.obtenerActivos();
        cmbProveedor.setItems(FXCollections.observableArrayList(proveedores));

        // Configurar cómo se muestra el proveedor
        cmbProveedor.setConverter(new javafx.util.StringConverter<Proveedor>() {
            @Override
            public String toString(Proveedor p) {
                return p != null ? p.getNombreCompleto() + " - " + p.getNumeroDocumento() : "";
            }
            @Override
            public Proveedor fromString(String string) {
                return null;
            }
        });
    }

    private void cargarMateriales() {
        List<Material> materiales = materialRepository.findByActivoTrue();
        cmbMaterial.setItems(FXCollections.observableArrayList(materiales));

        cmbMaterial.setConverter(new javafx.util.StringConverter<Material>() {
            @Override
            public String toString(Material m) {
                return m != null ? m.getNombre() : "";
            }
            @Override
            public Material fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Calcula en tiempo real el preview de los campos calculados
     * Implementa la fórmula del Excel
     */
    private void calcularPreview() {
        try {
            BigDecimal peso = new BigDecimal(txtPeso.getText().isEmpty() ? "0" : txtPeso.getText());
            BigDecimal ley = new BigDecimal(txtLey.getText().isEmpty() ? "0" : txtLey.getText());
            BigDecimal deduccion = new BigDecimal(txtDeduccion.getText().isEmpty() ? "0" : txtDeduccion.getText());
            BigDecimal precioOnza = new BigDecimal(txtPrecioOnza.getText().isEmpty() ? "0" : txtPrecioOnza.getText());
            BigDecimal tipoCambio = new BigDecimal(txtTipoCambio.getText().isEmpty() ? "0" : txtTipoCambio.getText());

            // Usar el servicio para calcular
            AcopioDetalle previewDetalle = acopioService.calcularDetalle(
                    peso, ley, deduccion, precioOnza, tipoCambio);

            // Mostrar resultados - CORREGIDO: Usar RoundingMode en lugar de BigDecimal.ROUND_HALF_UP
            txtPrecioGramoDolares.setText(previewDetalle.getPrecioGramoDolares().setScale(6, RoundingMode.HALF_UP).toString());
            txtPrecioGramoSoles.setText(previewDetalle.getPrecioGramoSoles().setScale(6, RoundingMode.HALF_UP).toString());
            txtTotalItem.setText(previewDetalle.getTotalAPagar().setScale(2, RoundingMode.HALF_UP).toString());

        } catch (NumberFormatException e) {
            // Campos vacíos o inválidos - no hacer nada
        }
    }

    @FXML
    private void handleAgregarDetalle() {
        if (!validarCamposDetalle()) {
            return;
        }

        try {
            Material material = cmbMaterial.getValue();
            BigDecimal peso = new BigDecimal(txtPeso.getText());
            BigDecimal ley = new BigDecimal(txtLey.getText());
            BigDecimal deduccion = new BigDecimal(txtDeduccion.getText());
            BigDecimal precioOnza = new BigDecimal(txtPrecioOnza.getText());
            BigDecimal tipoCambio = new BigDecimal(txtTipoCambio.getText());

            // Crear detalle
            AcopioDetalle detalle = new AcopioDetalle();
            detalle.setMaterial(material);
            detalle.setPeso(peso);
            detalle.setLey(ley);
            detalle.setDeduccion(deduccion);
            detalle.setPrecioOnzaBase(precioOnza);
            detalle.setTipoCambioDolar(tipoCambio);
            detalle.setNumeroItem(detallesObservable.size() + 1);

            // Calcular (fórmula del Excel)
            detalle.calcular();

            // Agregar a la tabla
            detallesObservable.add(detalle);

            // Actualizar total general
            actualizarTotalGeneral();

            // Limpiar campos para nuevo ítem
            limpiarCamposDetalle();

        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al agregar detalle: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminarDetalle() {
        AcopioDetalle seleccionado = tblDetalles.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            detallesObservable.remove(seleccionado);
            renumerarItems();
            actualizarTotalGeneral();
        } else {
            ReportAlert.showWarning("Selección", "Seleccione un ítem para eliminar");
        }
    }

    @FXML
    private void handleGuardarAcopio() {
        if (!validarAcopio()) {
            return;
        }

        if (detallesObservable.isEmpty()) {
            ReportAlert.showWarning("Sin Detalles", "Agregue al menos un detalle al acopio");
            return;
        }

        if (!ReportAlert.showConfirmation("Confirmar",
                "¿Desea guardar este acopio por un total de S/. " + lblTotalGeneral.getText() + "?")) {
            return;
        }

        try {
            // Crear acopio
            Acopio acopio = new Acopio();
            acopio.setProveedor(cmbProveedor.getValue());
            acopio.setUsuario(sessionManager.getUsuarioActual());
            acopio.setFechaAcopio(dpFecha.getValue());
            acopio.setObservaciones(txtObservaciones.getText());
            acopio.setEstado("REGISTRADO");

            // Convertir lista observable a lista normal
            List<AcopioDetalle> detalles = new ArrayList<>(detallesObservable);

            // Guardar
            Acopio acopioGuardado = acopioService.crear(acopio, detalles);

            ReportAlert.showSuccess("Éxito",
                    "Acopio " + acopioGuardado.getNumeroAcopio() + " registrado exitosamente");

            // Preguntar si desea imprimir voucher
            if (ReportAlert.showConfirmation("Imprimir", "¿Desea imprimir el voucher?")) {
                handleImprimirVoucher(acopioGuardado.getId());
            }

            // Limpiar formulario
            limpiarFormulario();

        } catch (Exception e) {
            e.printStackTrace();
            ReportAlert.showError("Error", "Error al guardar acopio: " + e.getMessage());
        }
    }

    private void handleImprimirVoucher(Long acopioId) {
        try {
            JasperPrint jasperPrint = acopioService.generarVoucher(acopioId);
            ReportAlert.showReport(jasperPrint, "Voucher de Acopio");
        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al generar voucher: " + e.getMessage());
        }
    }

    private boolean validarCamposDetalle() {
        if (cmbMaterial.getValue() == null) {
            ReportAlert.showWarning("Validación", "Seleccione un material");
            return false;
        }
        if (txtPeso.getText().isEmpty() || new BigDecimal(txtPeso.getText()).compareTo(BigDecimal.ZERO) <= 0) {
            ReportAlert.showWarning("Validación", "Ingrese un peso válido");
            return false;
        }
        if (txtLey.getText().isEmpty()) {
            ReportAlert.showWarning("Validación", "Ingrese la ley del material");
            return false;
        }
        if (txtPrecioOnza.getText().isEmpty()) {
            ReportAlert.showWarning("Validación", "Ingrese el precio por onza");
            return false;
        }
        return true;
    }

    private boolean validarAcopio() {
        if (cmbProveedor.getValue() == null) {
            ReportAlert.showWarning("Validación", "Seleccione un proveedor");
            return false;
        }
        if (dpFecha.getValue() == null) {
            ReportAlert.showWarning("Validación", "Seleccione una fecha");
            return false;
        }
        return true;
    }

    private void actualizarTotalGeneral() {
        BigDecimal total = detallesObservable.stream()
                .map(AcopioDetalle::getTotalAPagar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalGeneral.setText(total.setScale(2, RoundingMode.HALF_UP).toString());
    }

    private void renumerarItems() {
        for (int i = 0; i < detallesObservable.size(); i++) {
            detallesObservable.get(i).setNumeroItem(i + 1);
        }
        tblDetalles.refresh();
    }

    private void limpiarCamposDetalle() {
        cmbMaterial.setValue(null);
        txtPeso.clear();
        txtLey.clear();
        txtPrecioOnza.clear();
        txtPrecioGramoDolares.clear();
        txtPrecioGramoSoles.clear();
        txtTotalItem.clear();
    }

    @FXML
    private void handleNuevo() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        cmbProveedor.setValue(null);
        dpFecha.setValue(LocalDate.now());
        txtObservaciones.clear();
        detallesObservable.clear();
        lblTotalGeneral.setText("0.00");
        limpiarCamposDetalle();
    }
}