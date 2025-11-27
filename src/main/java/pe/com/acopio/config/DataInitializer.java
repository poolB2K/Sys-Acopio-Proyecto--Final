package pe.com.acopio.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.com.acopio.model.Usuario;
import pe.com.acopio.model.Material;
import pe.com.acopio.repository.UsuarioRepository;
import pe.com.acopio.repository.MaterialRepository;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Iniciando carga de datos por defecto ===");

        if (!usuarioRepository.existsByUsername("elvis123")) {
            Usuario admin = new Usuario();
            admin.setUsername("elvis123");
            admin.setPassword("faijo123");
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setRol("ADMIN");
            admin.setActivo(true);
            admin.setFechaCreacion(LocalDateTime.now());
            usuarioRepository.save(admin);
            System.out.println("✓ Usuario creado (user: elvis123, pass: faijo123)");
        }

        if (materialRepository.count() == 0) {
            Material oro = new Material();
            oro.setNombre("Oro");
            oro.setDescripcion("Oro de acopio");
            oro.setActivo(true);
            materialRepository.save(oro);

            System.out.println("✓ Material creado (Oro)");
        }

        System.out.println("=== Carga de datos completada ===");
    }
}
