package pe.com.acopio.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import pe.com.acopio.model.Usuario;
import pe.com.acopio.service.HistorialService;
import pe.com.acopio.service.UsuarioService;
import pe.com.acopio.util.ReportAlert;
import pe.com.acopio.util.SessionManager;

import java.util.Optional;

@Controller
public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private HistorialService historialService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            ReportAlert.showWarning("Campos Vacíos", "Por favor ingrese usuario y contraseña");
            return;
        }

        Optional<Usuario> usuarioOpt = usuarioService.autenticar(username, password);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // Iniciar sesión
            sessionManager.setUsuarioActual(usuario);

            // Registrar en historial
            historialService.logAccion(usuario, "LOGIN",
                    "Usuario " + username + " inició sesión", "SISTEMA");

            // Abrir ventana principal
            abrirVentanaPrincipal();

        } else {
            ReportAlert.showError("Error de Autenticación",
                    "Usuario o contraseña incorrectos");
            txtPassword.clear();
            txtUsername.requestFocus();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }

    private void abrirVentanaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/maingui.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("SysAcopio - Sistema de Acopio de Minerales");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            // Cerrar ventana de login
            Stage loginStage = (Stage) txtUsername.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            ReportAlert.showError("Error", "No se pudo abrir la ventana principal");
        }
    }

    @FXML
    private void initialize() {
        // Evento Enter en el campo de contraseña
        txtPassword.setOnAction(event -> handleLogin());
    }
}
