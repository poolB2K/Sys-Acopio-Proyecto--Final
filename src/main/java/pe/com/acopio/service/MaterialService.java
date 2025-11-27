package pe.com.acopio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.com.acopio.model.Material;
import pe.com.acopio.repository.MaterialRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;

    @Transactional
    public Material guardar(Material material) {
        return materialRepository.save(material);
    }

    @Transactional(readOnly = true)
    public List<Material> listarActivos() {
        return materialRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<Material> listarTodos() {
        return materialRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Material> buscarPorId(Long id) {
        return materialRepository.findById(id);
    }
}
