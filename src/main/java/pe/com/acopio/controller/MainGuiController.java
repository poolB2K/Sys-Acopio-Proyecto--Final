package pe.com.acopio.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import pe.com.acopio.model.Usuario;
import pe.com.acopio.service.HistorialService;
import pe.com.acopio.util.ReportAlert;
import pe.com.acopio.util.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class MainGuiController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label lblUsuario;

    @FXML
    private Label lblFechaHora;

    private final ApplicationContext applicationContext;
    private final HistorialService historialService;
    private final SessionManager sessionManager;

    @FXML
    private void initialize() {
        // Mostrar usuario actual
        String nombreUsuario = sessionManager.getUsuarioActual() != null
                ? sessionManager.getUsuarioActual().getNombreCompleto()
                : "N/A";
        lblUsuario.setText("Usuario: " + nombreUsuario);

        // Mostrar fecha y hora
        actualizarFechaHora();

        // Cargar vista de acopio por defecto
        cargarVistaAcopio();
    }

    private void actualizarFechaHora() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblFechaHora.setText(ahora.format(formatter));
    }

    @FXML
    private void handleNuevoAcopio() {
        cargarVistaAcopio();
    }

    @FXML
    private void handleProveedores() {
        cargarVista("/proveedor.fxml");
    }

    @FXML
    private void handleHistorial() {
        cargarVista("/historial.fxml");
    }

    @FXML
    private void handleCerrarSesion() {
        if (ReportAlert.showConfirmation("Cerrar Sesión",
                "¿Está seguro que desea cerrar sesión?")) {

            Usuario usuarioActual = sessionManager.getUsuarioActual();
            // Registrar cierre de sesión
            historialService.logAccion(usuarioActual, "LOGOUT",
                    "Usuario cerró sesión", "SISTEMA");

            // Cerrar sesión
            sessionManager.cerrarSesion();

            // Cerrar ventana actual y volver al login
            mainBorderPane.getScene().getWindow().hide();

            // Aquí deberías reabrir la ventana de login
            // (implementar según tu lógica de inicio)
        }
    }

    @FXML
    private void handleSalir() {
        if (ReportAlert.showConfirmation("Salir",
                "¿Está seguro que desea salir del sistema?")) {

            Usuario usuarioActual = sessionManager.getUsuarioActual();
            historialService.logAccion(usuarioActual, "SALIR",
                    "Usuario salió del sistema", "SISTEMA");

            System.exit(0);
        }
    }

    private void cargarVistaAcopio() {
        cargarVista("/acopio.fxml");
    }

    private void cargarVista(String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            loader.setControllerFactory(applicationContext::getBean);
            Parent vista = loader.load();
            mainBorderPane.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
            ReportAlert.showError("Error", "No se pudo cargar la vista: " + e.getMessage());
        }
    }
}