package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UnidadMedidaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class UnidadMedidaServiceImpl extends BaseServiceImpl<UnidadMedida, UnidadMedidaRepository> implements UnidadMedidaService {

    public UnidadMedidaServiceImpl(UnidadMedidaRepository unidadMedidaRepository) {
        super(unidadMedidaRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadMedidaResponseDTO> findAllUnidades() {
        try {
            return super.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar unidades de medida: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadMedidaResponseDTO findUnidadById(Integer id) throws Exception {
        return convertToDto(super.findById(id));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) throws Exception {
        UnidadMedida unidadExistente = super.findById(id);
        if (unidadExistente.getArticulos() != null && !unidadExistente.getArticulos().isEmpty()) {
            throw new Exception("No se puede eliminar la Unidad de Medida ID " + id + " porque está siendo utilizada por uno o más artículos.");
        }
        return super.delete(id);
    }

    private UnidadMedidaResponseDTO convertToDto(UnidadMedida unidadMedida) {
        UnidadMedidaResponseDTO dto = new UnidadMedidaResponseDTO();
        dto.setId(unidadMedida.getId());
        dto.setDenominacion(unidadMedida.getDenominacion());
        return dto;
    }
}