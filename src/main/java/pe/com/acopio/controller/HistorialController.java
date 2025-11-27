package pe.com.acopio.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pe.com.acopio.model.HistorialMovimiento;
import pe.com.acopio.service.HistorialService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HistorialController {

    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<String> cmbModulo;
    @FXML private TextField txtBuscar;

    @FXML private TableView<HistorialMovimiento> tblHistorial;
    @FXML private TableColumn<HistorialMovimiento, Long> colId;
    @FXML private TableColumn<HistorialMovimiento, String> colFechaHora;
    @FXML private TableColumn<HistorialMovimiento, String> colUsuario;
    @FXML private TableColumn<HistorialMovimiento, String> colModulo;
    @FXML private TableColumn<HistorialMovimiento, String> colAccion;
    @FXML private TableColumn<HistorialMovimiento, String> colDescripcion;

    @FXML private TextArea txtDetalles;

    @Autowired
    private HistorialService historialService;

    private ObservableList<HistorialMovimiento> historialObservable = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTabla();
        configurarComboModulo();
        establecerFechasDefault();
        cargarHistorial();

        // Listener para mostrar detalles al seleccionar
        tblHistorial.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null) {
                        mostrarDetalles(newVal);
                    }
                });
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Formatear fecha y hora
        colFechaHora.setCellValueFactory(cellData -> {
            LocalDateTime fechaHora = cellData.getValue().getFechaHora();
            String formatted = fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        colUsuario.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getUsuario().getNombreCompleto()));

        colModulo.setCellValueFactory(new PropertyValueFactory<>("modulo"));
        colAccion.setCellValueFactory(new PropertyValueFactory<>("accion"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        tblHistorial.setItems(historialObservable);
    }

    private void configurarComboModulo() {
        cmbModulo.setItems(FXCollections.observableArrayList(
                "TODOS", "SISTEMA", "ACOPIO", "PROVEEDOR", "USUARIO"
        ));
        cmbModulo.setValue("TODOS");
    }

    private void establecerFechasDefault() {
        // Fecha fin: hoy
        dpFechaFin.setValue(LocalDate.now());
        // Fecha inicio: hace 30 días
        dpFechaInicio.setValue(LocalDate.now().minusDays(30));
    }

    private void cargarHistorial() {
        List<HistorialMovimiento> movimientos = historialService.obtenerTodos();
        historialObservable.clear();
        historialObservable.addAll(movimientos);
    }

    @FXML
    private void handleBuscar() {
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();
        String modulo = cmbModulo.getValue();
        String terminoBusqueda = txtBuscar.getText().trim().toLowerCase();

        List<HistorialMovimiento> movimientos;

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            movimientos = historialService.obtenerPorFechas(inicio, fin);
        } else {
            movimientos = historialService.obtenerTodos();
        }

        // Filtrar por módulo
        if (!"TODOS".equals(modulo)) {
            movimientos = movimientos.stream()
                    .filter(m -> modulo.equals(m.getModulo()))
                    .toList();
        }

        // Filtrar por término de búsqueda
        if (!terminoBusqueda.isEmpty()) {
            movimientos = movimientos.stream()
                    .filter(m ->
                            m.getAccion().toLowerCase().contains(terminoBusqueda) ||
                                    m.getDescripcion().toLowerCase().contains(terminoBusqueda) ||
                                    m.getUsuario().getNombreCompleto().toLowerCase().contains(terminoBusqueda)
                    )
                    .toList();
        }

        historialObservable.clear();
        historialObservable.addAll(movimientos);
    }

    @FXML
    private void handleLimpiar() {
        establecerFechasDefault();
        cmbModulo.setValue("TODOS");
        txtBuscar.clear();
        cargarHistorial();
    }

    @FXML
    private void handleVerHoy() {
        List<HistorialMovimiento> movimientos = historialService.obtenerHoy();
        historialObservable.clear();
        historialObservable.addAll(movimientos);
    }

    @FXML
    private void handleVerRecientes() {
        List<HistorialMovimiento> movimientos = historialService.obtenerRecientes();
        historialObservable.clear();
        historialObservable.addAll(movimientos);
    }

    private void mostrarDetalles(HistorialMovimiento movimiento) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("DETALLES DEL MOVIMIENTO\n");
        sb.append("═══════════════════════════════════════\n\n");

        sb.append("ID: ").append(movimiento.getId()).append("\n");
        sb.append("Fecha y Hora: ").append(
                movimiento.getFechaHora().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                )
        ).append("\n");
        sb.append("Usuario: ").append(movimiento.getUsuario().getNombreCompleto()).append("\n");
        sb.append("Username: ").append(movimiento.getUsuario().getUsername()).append("\n");
        sb.append("Módulo: ").append(movimiento.getModulo()).append("\n");
        sb.append("Acción: ").append(movimiento.getAccion()).append("\n\n");

        sb.append("Descripción:\n");
        sb.append(movimiento.getDescripcion()).append("\n\n");

        if (movimiento.getDetallesAdicionales() != null && !movimiento.getDetallesAdicionales().isEmpty()) {
            sb.append("Detalles Adicionales:\n");
            sb.append(movimiento.getDetallesAdicionales()).append("\n");
        }

        txtDetalles.setText(sb.toString());
    }
}