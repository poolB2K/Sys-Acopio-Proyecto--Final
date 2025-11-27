package pe.com.acopio;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SysAcopioApplication {

    public static void main(String[] args) {
        // Asegurarse de que el directorio de la base de datos exista ANTES de lanzar la aplicación.
        // Esta es la corrección clave.
        File dbDir = new File("data");
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        // Lanzar JavaFX con Spring Boot
        javafx.application.Application.launch(MainApplication.class, args);
    }
}
