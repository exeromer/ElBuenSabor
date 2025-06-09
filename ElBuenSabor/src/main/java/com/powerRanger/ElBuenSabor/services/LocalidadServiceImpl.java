package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.LocalidadRepository;
import com.powerRanger.ElBuenSabor.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocalidadServiceImpl extends BaseServiceImpl<Localidad, LocalidadRepository> implements LocalidadService {

    @Autowired
    private ProvinciaRepository provinciaRepository;

    public LocalidadServiceImpl(LocalidadRepository localidadRepository) {
        super(localidadRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalidadResponseDTO> findAllLocalidades() {
        try {
            return super.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar localidades: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LocalidadResponseDTO findLocalidadById(Integer id) throws Exception {
        return convertToDto(super.findById(id));
    }

    @Override
    @Transactional
    public Localidad save(Localidad localidad) throws Exception {
        if (localidad.getProvincia() == null || localidad.getProvincia().getId() == null) {
            throw new Exception("La localidad debe estar asociada a una provincia válida.");
        }
        Provincia provinciaExistente = provinciaRepository.findById(localidad.getProvincia().getId())
                .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + localidad.getProvincia().getId()));
        localidad.setProvincia(provinciaExistente);
        return super.save(localidad);
    }

    @Override
    @Transactional
    public Localidad update(Integer id, Localidad localidadDetalles) throws Exception {
        Localidad localidadExistente = super.findById(id);

        if (localidadDetalles.getNombre() == null || localidadDetalles.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre de la localidad es obligatorio.");
        }
        localidadExistente.setNombre(localidadDetalles.getNombre());

        if (localidadDetalles.getProvincia() != null && localidadDetalles.getProvincia().getId() != null) {
            Provincia provinciaNueva = provinciaRepository.findById(localidadDetalles.getProvincia().getId())
                    .orElseThrow(() -> new Exception("No se encontró la provincia con ID: " + localidadDetalles.getProvincia().getId()));
            localidadExistente.setProvincia(provinciaNueva);
        } else {
            throw new Exception("La provincia es obligatoria para la localidad.");
        }
        return super.update(id, localidadExistente);
    }

    @Override
    @Transactional
    public boolean delete(Integer id) throws Exception {
        Localidad localidad = super.findById(id);
        if (localidad.getDomicilios() != null && !localidad.getDomicilios().isEmpty()) {
            throw new Exception("No se puede eliminar la Localidad ID " + id + " porque tiene domicilios asociados.");
        }
        return super.delete(id);
    }

    private LocalidadResponseDTO convertToDto(Localidad localidad) {
        LocalidadResponseDTO dto = new LocalidadResponseDTO();
        dto.setId(localidad.getId());
        dto.setNombre(localidad.getNombre());
        if (localidad.getProvincia() != null) {
            dto.setProvincia(convertProvinciaToDto(localidad.getProvincia()));
        }
        return dto;
    }

    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) {
        if (provincia == null) return null;
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
        PaisResponseDTO dto = new PaisResponseDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        return dto;
    }
}