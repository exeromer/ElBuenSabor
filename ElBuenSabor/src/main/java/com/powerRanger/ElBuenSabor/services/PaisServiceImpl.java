package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.repository.PaisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaisServiceImpl extends BaseServiceImpl<Pais, PaisRepository> implements PaisService {

    public PaisServiceImpl(PaisRepository paisRepository) {
        super(paisRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaisResponseDTO> findAllPaises() {
        try {
            return super.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar países: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaisResponseDTO findPaisById(Integer id) throws Exception {
        return convertToDto(super.findById(id));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) throws Exception {
        Pais pais = super.findById(id);
        if (pais.getProvincias() != null && !pais.getProvincias().isEmpty()) {
            throw new Exception("No se puede eliminar el País ID " + id + " porque tiene provincias asociadas.");
        }
        return super.delete(id);
    }

    private PaisResponseDTO convertToDto(Pais pais) {
        PaisResponseDTO dto = new PaisResponseDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        return dto;
    }
}