package pe.com.acopio.util;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sistema de notificaciones Toast no bloqueantes
 * Las notificaciones aparecen en la esquina superior derecha y se desvanecen
 * automáticamente
 */
public class ToastNotification {

    private static final double TOAST_WIDTH = 350;
    private static final double TOAST_SPACING = 10;
    private static final double MARGIN_RIGHT = 20;
    private static final double MARGIN_TOP = 20;

    // Lista para rastrear toasts activos
    private static final List<Popup> activeToasts = new ArrayList<>();

    public enum ToastType {
        SUCCESS("#27ae60", "✓", "#ffffff"),
        ERROR("#e74c3c", "✕", "#ffffff"),
        WARNING("#f39c12", "⚠", "#ffffff"),
        INFO("#3498db", "ℹ", "#ffffff");

        private final String backgroundColor;
        private final String icon;
        private final String textColor;

        ToastType(String backgroundColor, String icon, String textColor) {
            this.backgroundColor = backgroundColor;
            this.icon = icon;
            this.textColor = textColor;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public String getIcon() {
            return icon;
        }

        public String getTextColor() {
            return textColor;
        }
    }

    /**
     * Muestra una notificación toast
     */
    public static void show(Stage owner, String message, ToastType type, double durationSeconds) {
        if (owner == null || message == null || message.trim().isEmpty()) {
            return;
        }

        Popup popup = new Popup();

        // Contenedor principal
        StackPane container = new StackPane();
        container.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);",
                type.getBackgroundColor()));
        container.setPrefWidth(TOAST_WIDTH);
        container.setMaxWidth(TOAST_WIDTH);

        // HBox para contenido
        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);

        // Icono
        Label iconLabel = new Label(type.getIcon());
        iconLabel.setStyle(String.format(
                "-fx-font-size: 24px; " +
                        "-fx-fontweight: bold; " +
                        "-fx-text-fill: %s;",
                type.getTextColor()));

        // Mensaje
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle(String.format(
                "-fx-font-size: 13px; " +
                        "-fx-text-fill: %s;",
                type.getTextColor()));
        messageLabel.setMaxWidth(TOAST_WIDTH - 100);

        // Botón cerrar
        Label closeButton = new Label("×");
        closeButton.setStyle(String.format(
                "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: %s; " +
                        "-fx-cursor: hand;",
                type.getTextColor()));
        closeButton.setOnMouseClicked(e -> closeToast(popup));

        content.getChildren().addAll(iconLabel, messageLabel, closeButton);
        HBox.setHgrow(messageLabel, javafx.scene.layout.Priority.ALWAYS);

        container.getChildren().add(content);
        popup.getContent().add(container);

        // Calcular posición
        Scene scene = owner.getScene();
        if (scene == null) {
            return;
        }

        double yOffset = calculateYOffset();
        double x = owner.getX() + owner.getWidth() - TOAST_WIDTH - MARGIN_RIGHT;
        double y = owner.getY() + MARGIN_TOP + yOffset;

        activeToasts.add(popup);

        // Mostrar popup
        popup.setAutoHide(false);
        popup.show(owner, x, y);

        // Animación de entrada
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), container);
        slideIn.setFromY(-100);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition entrance = new ParallelTransition(slideIn, fadeIn);
        entrance.play();

        // Auto-cerrar si se especificó duración
        if (durationSeconds > 0) {
            PauseTransition delay = new PauseTransition(Duration.seconds(durationSeconds));
            delay.setOnFinished(e -> closeToast(popup));
            delay.play();
        }
    }

    public static void showSuccess(Stage owner, String message) {
        show(owner, message, ToastType.SUCCESS, 3);
    }

    public static void showError(Stage owner, String message) {
        show(owner, message, ToastType.ERROR, 4);
    }

    public static void showWarning(Stage owner, String message) {
        show(owner, message, ToastType.WARNING, 3.5);
    }

    public static void showInfo(Stage owner, String message) {
        show(owner, message, ToastType.INFO, 3);
    }

    private static void closeToast(Popup popup) {
        if (!popup.isShowing()) {
            return;
        }

        StackPane container = (StackPane) popup.getContent().get(0);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            popup.hide();
            activeToasts.remove(popup);
        });
        fadeOut.play();
    }

    private static double calculateYOffset() {
        double offset = 0;
        for (Popup toast : activeToasts) {
            if (toast.isShowing() && !toast.getContent().isEmpty()) {
                StackPane pane = (StackPane) toast.getContent().get(0);
                offset += pane.getHeight() + TOAST_SPACING;
            }
        }
        return offset;
    }

    public static void closeAll() {
        List<Popup> toastsToClose = new ArrayList<>(activeToasts);
        for (Popup toast : toastsToClose) {
            closeToast(toast);
        }
    }
}
