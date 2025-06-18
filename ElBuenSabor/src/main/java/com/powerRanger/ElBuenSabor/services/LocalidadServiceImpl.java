package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;        // Necesario para ProvinciaResponseDTO
import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO;  // Necesario para el mapeo
import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.LocalidadRepository;
import com.powerRanger.ElBuenSabor.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LocalidadServiceImpl implements LocalidadService {

    @Autowired
    private LocalidadRepository localidadRepository;
    @Autowired
    private ProvinciaRepository provinciaRepository; // Necesario para asociar

    // Método de Mapeo de Entidad Pais a PaisResponseDTO
    private PaisResponseDTO convertPaisToDto(Pais pais) {
        if (pais == null) return null;
        PaisResponseDTO paisDto = new PaisResponseDTO();
        paisDto.setId(pais.getId());
        paisDto.setNombre(pais.getNombre());
        return paisDto;
    }

    // Método de Mapeo de Entidad Provincia a ProvinciaResponseDTO
    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) {
        if (provincia == null) return null;
        ProvinciaResponseDTO provinciaDto = new ProvinciaResponseDTO();
        provinciaDto.setId(provincia.getId());
        provinciaDto.setNombre(provincia.getNombre());
        if (provincia.getPais() != null) {
            provinciaDto.setPais(convertPaisToDto(provincia.getPais()));
        }
        return provinciaDto;
    }

    // Método de Mapeo de Entidad Localidad a LocalidadResponseDTO
    private LocalidadResponseDTO convertToDto(Localidad localidad) {
        LocalidadResponseDTO dto = new LocalidadResponseDTO();
        dto.setId(localidad.getId());
        dto.setNombre(localidad.getNombre());
        if (localidad.getProvincia() != null) {
            dto.setProvincia(convertProvinciaToDto(localidad.getProvincia()));
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalidadResponseDTO> obtenerTodas() {
        List<Localidad> localidades = localidadRepository.findAll();
        return localidades.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LocalidadResponseDTO obtenerPorId(Integer id) throws Exception {
        Localidad localidad = localidadRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la localidad con ID: " + id));
        return convertToDto(localidad);
    }

    @Override
    @Transactional
    public Localidad guardar(Localidad localidad) throws Exception {
        if (localidad.getNombre() == null || localidad.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre de la localidad es obligatorio.");
        }
        if (localidad.getProvincia() == null || localidad.getProvincia().getId() == null) {
            throw new Exception("La localidad debe estar asociada a una provincia válida.");
        }
        Provincia provinciaExistente = provinciaRepository.findById(localidad.getProvincia().getId())
                .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + localidad.getProvincia().getId()));
        localidad.setProvincia(provinciaExistente);
        return localidadRepository.save(localidad);
    }

    @Override
    @Transactional
    public Localidad actualizar(Integer id, Localidad localidadDetalles) throws Exception {
        Localidad localidadExistente = localidadRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la localidad con ID: " + id + " para actualizar."));

        if (localidadDetalles.getNombre() == null || localidadDetalles.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre de la localidad es obligatorio para actualizar.");
        }
        localidadExistente.setNombre(localidadDetalles.getNombre());

        if (localidadDetalles.getProvincia() != null && localidadDetalles.getProvincia().getId() != null) {
            Provincia provinciaNueva = provinciaRepository.findById(localidadDetalles.getProvincia().getId())
                    .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + localidadDetalles.getProvincia().getId()));
            localidadExistente.setProvincia(provinciaNueva);
        } else {
            throw new Exception("La provincia es obligatoria para la localidad.");
        }
        return localidadRepository.save(localidadExistente);
    }

    @Override
    @Transactional
    public boolean borrar(Integer id) throws Exception {
        Localidad localidad = localidadRepository.findById(id)
                .orElseThrow(() -> new Exception("No se encontró la localidad con ID: " + id + " para borrar."));

        if (localidad.getDomicilios() != null && !localidad.getDomicilios().isEmpty()) {
            throw new Exception("No se puede eliminar la Localidad ID " + id + " porque tiene domicilios asociados.");
        }
        localidadRepository.deleteById(id);
        return true;
    }
}