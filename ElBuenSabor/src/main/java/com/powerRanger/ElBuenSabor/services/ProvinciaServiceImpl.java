package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.PaisRepository;
import com.powerRanger.ElBuenSabor.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProvinciaServiceImpl extends BaseServiceImpl<Provincia, ProvinciaRepository> implements ProvinciaService {

    @Autowired
    private PaisRepository paisRepository;

    public ProvinciaServiceImpl(ProvinciaRepository provinciaRepository) {
        super(provinciaRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinciaResponseDTO> findAllProvincias() {
        try {
            return super.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar provincias: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinciaResponseDTO findProvinciaById(Integer id) throws Exception {
        return convertToDto(super.findById(id));
    }

    @Override
    @Transactional
    public Provincia save(Provincia provincia) throws Exception {
        if (provincia.getPais() == null || provincia.getPais().getId() == null) {
            throw new Exception("La provincia debe estar asociada a un país válido.");
        }
        Pais paisExistente = paisRepository.findById(provincia.getPais().getId())
                .orElseThrow(() -> new Exception("No se encontró el país con ID: " + provincia.getPais().getId()));
        provincia.setPais(paisExistente);
        return super.save(provincia);
    }

    @Override
    @Transactional
    public Provincia update(Integer id, Provincia provinciaDetalles) throws Exception {
        Provincia provinciaExistente = super.findById(id);

        if (provinciaDetalles.getNombre() == null || provinciaDetalles.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre de la provincia es obligatorio.");
        }
        provinciaExistente.setNombre(provinciaDetalles.getNombre());

        if (provinciaDetalles.getPais() != null && provinciaDetalles.getPais().getId() != null) {
            Pais paisNuevo = paisRepository.findById(provinciaDetalles.getPais().getId())
                    .orElseThrow(() -> new Exception("No se encontró el país con ID: " + provinciaDetalles.getPais().getId()));
            provinciaExistente.setPais(paisNuevo);
        } else {
            throw new Exception("El país es obligatorio para la provincia.");
        }
        return super.update(id, provinciaExistente);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) throws Exception {
        Provincia provincia = super.findById(id);
        if (provincia.getLocalidades() != null && !provincia.getLocalidades().isEmpty()) {
            throw new Exception("No se puede eliminar la Provincia ID " + id + " porque tiene localidades asociadas.");
        }
        return super.delete(id);
    }

    private ProvinciaResponseDTO convertToDto(Provincia provincia) {
        ProvinciaResponseDTO dto = new ProvinciaResponseDTO();
        dto.setId(provincia.getId());
        dto.setNombre(provincia.getNombre());
        if (provincia.getPais() != null) {
            dto.setPais(convertPaisToDto(provincia.getPais()));
        }
        return dto;
    }

    private PaisResponseDTO convertPaisToDto(Pais pais) {
        if (pais == null) return null;
        PaisResponseDTO paisDto = new PaisResponseDTO();
        paisDto.setId(pais.getId());
        paisDto.setNombre(pais.getNombre());
        return paisDto;
    }
}