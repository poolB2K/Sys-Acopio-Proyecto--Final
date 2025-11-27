package pe.com.acopio.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.com.acopio.model.Proveedor;
import pe.com.acopio.service.HistorialService;
import pe.com.acopio.service.ProveedorService;
import pe.com.acopio.util.ConsultaDNI;
import pe.com.acopio.util.ReportAlert;
import pe.com.acopio.util.SessionManager;

import java.util.List;
import java.util.Map;

@Controller
public class ProveedorController {

    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private TextField txtNumeroDocumento;
    @FXML private Button btnConsultarDNI;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtCiudad;
    @FXML private TextArea txtObservaciones;
    @FXML private TextField txtBuscar;

    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, Long> colId;
    @FXML private TableColumn<Proveedor, String> colTipoDoc;
    @FXML private TableColumn<Proveedor, String> colNumeroDoc;
    @FXML private TableColumn<Proveedor, String> colNombres;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colCiudad;
    @FXML private TableColumn<Proveedor, Boolean> colActivo;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private HistorialService historialService;

    @Autowired
    private SessionManager sessionManager;

    private ObservableList<Proveedor> proveedoresObservable = FXCollections.observableArrayList();
    private Proveedor proveedorSeleccionado;

    @FXML
    private void initialize() {
        configurarTabla();
        configurarComboTipoDocumento();
        cargarProveedores();

        // Listener para cambio de tipo de documento
        cmbTipoDocumento.valueProperty().addListener((obs, old, newVal) -> {
            btnConsultarDNI.setDisable(!"DNI".equals(newVal) && !"RUC".equals(newVal));
        });

        // Listener para selección en tabla
        tblProveedores.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null) {
                        cargarDatosProveedor(newVal);
                    }
                });
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTipoDoc.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        colNumeroDoc.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        colNombres.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getNombreCompleto()));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        tblProveedores.setItems(proveedoresObservable);
    }

    private void configurarComboTipoDocumento() {
        cmbTipoDocumento.setItems(FXCollections.observableArrayList(
                "DNI", "RUC", "CE", "PASAPORTE", "OTROS"
        ));
        cmbTipoDocumento.setValue("DNI");
    }

    private void cargarProveedores() {
        List<Proveedor> proveedores = proveedorService.obtenerTodos();
        proveedoresObservable.clear();
        proveedoresObservable.addAll(proveedores);
    }

    @FXML
    private void handleConsultarDNI() {
        String tipoDoc = cmbTipoDocumento.getValue();
        String numeroDoc = txtNumeroDocumento.getText().trim();

        if (numeroDoc.isEmpty()) {
            ReportAlert.showWarning("Validación", "Ingrese el número de documento");
            return;
        }

        try {
            if ("DNI".equals(tipoDoc)) {
                if (numeroDoc.length() != 8) {
                    ReportAlert.showWarning("Validación", "El DNI debe tener 8 dígitos");
                    return;
                }

                Map<String, String> datos = ConsultaDNI.consultarDNI(numeroDoc);
                if (datos != null) {
                    txtNombres.setText(datos.get("nombres"));
                    txtApellidos.setText(datos.get("apellidoPaterno") + " " + datos.get("apellidoMaterno"));
                    ReportAlert.showSuccess("Consulta Exitosa", "Datos obtenidos correctamente");
                } else {
                    ReportAlert.showWarning("Sin Resultados", "No se encontraron datos para este DNI");
                }

            } else if ("RUC".equals(tipoDoc)) {
                if (numeroDoc.length() != 11) {
                    ReportAlert.showWarning("Validación", "El RUC debe tener 11 dígitos");
                    return;
                }

                Map<String, String> datos = ConsultaDNI.consultarRUC(numeroDoc);
                if (datos != null) {
                    txtNombres.setText(datos.get("razonSocial"));
                    txtDireccion.setText(datos.get("direccion"));
                    txtApellidos.clear(); // Para RUC no hay apellidos
                    ReportAlert.showSuccess("Consulta Exitosa", "Datos obtenidos correctamente");
                } else {
                    ReportAlert.showWarning("Sin Resultados", "No se encontraron datos para este RUC");
                }
            }
        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al consultar: " + e.getMessage());
        }
    }

    @FXML
    private void handleNuevo() {
        limpiarFormulario();
        proveedorSeleccionado = null;
        txtNumeroDocumento.requestFocus();
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }

        try {
            Proveedor proveedor = construirProveedor();

            if (proveedorSeleccionado == null) {
                // Crear nuevo
                proveedorService.crear(proveedor);
                ReportAlert.showSuccess("Éxito", "Proveedor registrado exitosamente");

                historialService.logAccion(
                        sessionManager.getUsuarioActual(),
                        "CREACION_PROVEEDOR",
                        "Proveedor " + proveedor.getNombreCompleto() + " creado",
                        "PROVEEDOR");
            } else {
                // Actualizar existente
                proveedor.setId(proveedorSeleccionado.getId());
                proveedorService.actualizar(proveedor);
                ReportAlert.showSuccess("Éxito", "Proveedor actualizado exitosamente");

                historialService.logAccion(
                        sessionManager.getUsuarioActual(),
                        "ACTUALIZACION_PROVEEDOR",
                        "Proveedor " + proveedor.getNombreCompleto() + " actualizado",
                        "PROVEEDOR");
            }

            cargarProveedores();
            limpiarFormulario();
            proveedorSeleccionado = null;

        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void handleEliminar() {
        Proveedor seleccionado = tblProveedores.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            ReportAlert.showWarning("Selección", "Seleccione un proveedor");
            return;
        }

        if (!ReportAlert.showConfirmation("Confirmar",
                "¿Desea desactivar al proveedor " + seleccionado.getNombreCompleto() + "?")) {
            return;
        }

        try {
            proveedorService.desactivar(seleccionado.getId());
            ReportAlert.showSuccess("Éxito", "Proveedor desactivado");

            historialService.logAccion(
                    sessionManager.getUsuarioActual(),
                    "DESACTIVACION_PROVEEDOR",
                    "Proveedor " + seleccionado.getNombreCompleto() + " desactivado",
                    "PROVEEDOR");

            cargarProveedores();
            limpiarFormulario();

        } catch (Exception e) {
            ReportAlert.showError("Error", "Error al desactivar: " + e.getMessage());
        }
    }

    @FXML
    private void handleBuscar() {
        String termino = txtBuscar.getText().trim();

        if (termino.isEmpty()) {
            cargarProveedores();
            return;
        }

        List<Proveedor> resultados = proveedorService.buscarPorNombre(termino);
        proveedoresObservable.clear();
        proveedoresObservable.addAll(resultados);
    }

    private void cargarDatosProveedor(Proveedor proveedor) {
        proveedorSeleccionado = proveedor;

        cmbTipoDocumento.setValue(proveedor.getTipoDocumento());
        txtNumeroDocumento.setText(proveedor.getNumeroDocumento());
        txtNombres.setText(proveedor.getNombres());
        txtApellidos.setText(proveedor.getApellidos());
        txtDireccion.setText(proveedor.getDireccion());
        txtTelefono.setText(proveedor.getTelefono());
        txtEmail.setText(proveedor.getEmail());
        txtCiudad.setText(proveedor.getCiudad());
        txtObservaciones.setText(proveedor.getObservaciones());
    }

    private Proveedor construirProveedor() {
        Proveedor proveedor = new Proveedor();
        proveedor.setTipoDocumento(cmbTipoDocumento.getValue());
        proveedor.setNumeroDocumento(txtNumeroDocumento.getText().trim());
        proveedor.setNombres(txtNombres.getText().trim());
        proveedor.setApellidos(txtApellidos.getText().trim());
        proveedor.setDireccion(txtDireccion.getText().trim());
        proveedor.setTelefono(txtTelefono.getText().trim());
        proveedor.setEmail(txtEmail.getText().trim());
        proveedor.setCiudad(txtCiudad.getText().trim());
        proveedor.setObservaciones(txtObservaciones.getText().trim());
        proveedor.setActivo(true);

        return proveedor;
    }

    private boolean validarCampos() {
        if (cmbTipoDocumento.getValue() == null) {
            ReportAlert.showWarning("Validación", "Seleccione el tipo de documento");
            return false;
        }
        if (txtNumeroDocumento.getText().trim().isEmpty()) {
            ReportAlert.showWarning("Validación", "Ingrese el número de documento");
            return false;
        }
        if (txtNombres.getText().trim().isEmpty()) {
            ReportAlert.showWarning("Validación", "Ingrese los nombres");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        cmbTipoDocumento.setValue("DNI");
        txtNumeroDocumento.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtCiudad.clear();
        txtObservaciones.clear();
        tblProveedores.getSelectionModel().clearSelection();
    }
}
