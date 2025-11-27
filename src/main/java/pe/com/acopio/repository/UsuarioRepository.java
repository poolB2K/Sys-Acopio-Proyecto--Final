package pe.com.acopio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.acopio.model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameAndPassword(String username, String password);

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
    
    List<Usuario> findByActivoTrue();
}

