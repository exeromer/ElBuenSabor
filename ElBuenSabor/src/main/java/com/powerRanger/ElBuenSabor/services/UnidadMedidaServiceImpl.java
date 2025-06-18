package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UnidadMedidaResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors; // Para el mapeo de listas

@Service
@Validated
public class UnidadMedidaServiceImpl implements UnidadMedidaService {

    @Autowired
    private UnidadMedidaRepository unidadMedidaRepository;

    // Método de Mapeo de Entidad a DTO
    private UnidadMedidaResponseDTO convertToDto(UnidadMedida unidadMedida) {
        UnidadMedidaResponseDTO dto = new UnidadMedidaResponseDTO();
        dto.setId(unidadMedida.getId());
        dto.setDenominacion(unidadMedida.getDenominacion());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadMedidaResponseDTO> getAll() {
        List<UnidadMedida> unidades = unidadMedidaRepository.findAll();
        return unidades.stream()
                .map(this::convertToDto) // Mapea cada UnidadMedida a DTO
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadMedidaResponseDTO getById(Integer id) throws Exception {
        UnidadMedida unidad = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new Exception("Unidad de Medida no encontrada con ID: " + id));
        return convertToDto(unidad); // Mapea la UnidadMedida encontrada a DTO
    }

    @Override
    @Transactional
    public UnidadMedida create(@Valid UnidadMedida unidadMedida) throws Exception {
        // Lógica de validación (ej. denominación única) podría ir aquí si es necesario
        // if(unidadMedidaRepository.findByDenominacion(unidadMedida.getDenominacion()).isPresent()){
        //     throw new Exception("Ya existe una unidad de medida con esa denominación.");
        // }
        return unidadMedidaRepository.save(unidadMedida);
    }

    @Override
    @Transactional
    public UnidadMedida update(Integer id, @Valid UnidadMedida unidadMedidaDetails) throws Exception {
        UnidadMedida unidadExistente = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new Exception("Unidad de Medida no encontrada con ID: " + id + " para actualizar."));

        unidadExistente.setDenominacion(unidadMedidaDetails.getDenominacion());
        // Si UnidadMedida tuviera más campos actualizables, se setearían aquí.
        return unidadMedidaRepository.save(unidadExistente);
    }

    @Override
    @Transactional
    public void delete(Integer id) throws Exception {
        UnidadMedida unidadExistente = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new Exception("Unidad de Medida no encontrada con ID: " + id + " para eliminar."));

        if (unidadExistente.getArticulos() != null && !unidadExistente.getArticulos().isEmpty()) {
            throw new Exception("No se puede eliminar la Unidad de Medida ID " + id + " porque está siendo utilizada por uno o más artículos.");
        }
        unidadMedidaRepository.deleteById(id);
    }
}