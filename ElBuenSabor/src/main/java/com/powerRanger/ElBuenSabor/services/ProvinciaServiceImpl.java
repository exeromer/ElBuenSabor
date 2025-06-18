package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;       // Importar PaisResponseDTO
import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.PaisRepository;
import com.powerRanger.ElBuenSabor.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProvinciaServiceImpl implements ProvinciaService {

    @Autowired
    private ProvinciaRepository provinciaRepository;
    @Autowired
    private PaisRepository paisRepository; // Necesario si el DTO de Pais se construye aquí

    // Método de Mapeo de Entidad Pais a PaisResponseDTO (puede estar en PaisService o ser un mapper común)
    private PaisResponseDTO convertPaisToDto(Pais pais) {
        if (pais == null) return null;
        PaisResponseDTO paisDto = new PaisResponseDTO();
        paisDto.setId(pais.getId());
        paisDto.setNombre(pais.getNombre());
        return paisDto;
    }

    // Método de Mapeo de Entidad Provincia a ProvinciaResponseDTO
    private ProvinciaResponseDTO convertToDto(Provincia provincia) {
        ProvinciaResponseDTO dto = new ProvinciaResponseDTO();
        dto.setId(provincia.getId());
        dto.setNombre(provincia.getNombre());
        if (provincia.getPais() != null) {
            // Asumimos que PaisResponseDTO solo tiene id y nombre y no causa carga LAZY problemática
            // o que el contexto transaccional está activo (lo cual está por readOnly=true).
            dto.setPais(convertPaisToDto(provincia.getPais()));
        }
        // No incluimos la lista de localidades para mantener la respuesta simple por ahora.
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinciaResponseDTO> obtenerTodas() {
        List<Provincia> provincias = provinciaRepository.findAll();
        return provincias.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinciaResponseDTO obtenerPorId(Integer id) throws Exception {
        Provincia provincia = provinciaRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + id));
        return convertToDto(provincia);
    }

    @Override
    @Transactional
    public Provincia guardar(Provincia provincia) throws Exception {
        if (provincia.getPais() == null || provincia.getPais().getId() == null) {
            throw new Exception("La provincia debe estar asociada a un país válido.");
        }
        Pais paisExistente = paisRepository.findById(provincia.getPais().getId())
                .orElseThrow(() -> new Exception("No se encontró el país con ID: " + provincia.getPais().getId()));
        provincia.setPais(paisExistente);
        return provinciaRepository.save(provincia);
    }

    @Override
    @Transactional
    public Provincia actualizar(Integer id, Provincia provinciaDetalles) throws Exception {
        Provincia provinciaExistente = provinciaRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + id + " para actualizar."));

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
        return provinciaRepository.save(provinciaExistente);
    }

    @Override
    @Transactional
    public boolean borrar(Integer id) throws Exception {
        Provincia provincia = provinciaRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + id + " para borrar."));

        if (provincia.getLocalidades() != null && !provincia.getLocalidades().isEmpty()) {
            throw new Exception("No se puede eliminar la Provincia ID " + id + " porque tiene localidades asociadas.");
        }

        provinciaRepository.deleteById(id);
        return true;
    }
}