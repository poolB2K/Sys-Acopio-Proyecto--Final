package pe.com.acopio;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("pe.com.acopio.model")
@EnableJpaRepositories("pe.com.acopio.repository")
public class MainApplication extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        // Inicializa el contexto de Spring
        springContext = new SpringApplicationBuilder(MainApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);
        Parent rootNode = fxmlLoader.load();

        primaryStage.setTitle("SysAcopio - Login");
        Scene scene = new Scene(rootNode);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Cerrar contexto de Spring
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}