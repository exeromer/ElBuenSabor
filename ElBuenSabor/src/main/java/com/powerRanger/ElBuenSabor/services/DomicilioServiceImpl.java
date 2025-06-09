package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import com.powerRanger.ElBuenSabor.repository.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class DomicilioServiceImpl extends BaseServiceImpl<Domicilio, DomicilioRepository> implements DomicilioService {

    @Autowired
    private LocalidadRepository localidadRepository;

    public DomicilioServiceImpl(DomicilioRepository domicilioRepository) {
        super(domicilioRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomicilioResponseDTO> findAllDomicilios() {
        try {
            return super.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar domicilios: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DomicilioResponseDTO findDomicilioById(Integer id) throws Exception {
        return convertToDto(super.findById(id));
    }

    @Override
    @Transactional
    public DomicilioResponseDTO createDomicilio(@Valid DomicilioRequestDTO dto) throws Exception {
        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new Exception("Localidad no encontrada con ID: " + dto.getLocalidadId()));

        Domicilio domicilio = new Domicilio();
        domicilio.setCalle(dto.getCalle());
        domicilio.setNumero(dto.getNumero());
        domicilio.setCp(dto.getCp());
        domicilio.setLocalidad(localidad);

        return convertToDto(super.save(domicilio));
    }

    @Override
    @Transactional
    public DomicilioResponseDTO updateDomicilio(Integer id, @Valid DomicilioRequestDTO dto) throws Exception {
        Domicilio domicilioExistente = super.findById(id);

        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new Exception("Localidad no encontrada con ID: " + dto.getLocalidadId()));

        domicilioExistente.setCalle(dto.getCalle());
        domicilioExistente.setNumero(dto.getNumero());
        domicilioExistente.setCp(dto.getCp());
        domicilioExistente.setLocalidad(localidad);

        return convertToDto(super.update(id, domicilioExistente));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) throws Exception {
        Domicilio domicilioExistente = super.findById(id);
        if (domicilioExistente.getClientes() != null && !domicilioExistente.getClientes().isEmpty()) {
            throw new Exception("No se puede eliminar el Domicilio ID " + id + " porque está siendo utilizado por uno o más clientes.");
        }
        return super.delete(id);
    }

    private DomicilioResponseDTO convertToDto(Domicilio domicilio) {
        DomicilioResponseDTO dto = new DomicilioResponseDTO();
        dto.setId(domicilio.getId());
        dto.setCalle(domicilio.getCalle());
        dto.setNumero(domicilio.getNumero());
        dto.setCp(domicilio.getCp());
        if (domicilio.getLocalidad() != null) {
            dto.setLocalidad(convertLocalidadToDto(domicilio.getLocalidad()));
        }
        return dto;
    }

    // Mappers anidados
    private LocalidadResponseDTO convertLocalidadToDto(Localidad localidad) { if (localidad == null) return null; LocalidadResponseDTO dto = new LocalidadResponseDTO(); dto.setId(localidad.getId()); dto.setNombre(localidad.getNombre()); dto.setProvincia(convertProvinciaToDto(localidad.getProvincia())); return dto; }
    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) { if (provincia == null) return null; ProvinciaResponseDTO dto = new ProvinciaResponseDTO(); dto.setId(provincia.getId()); dto.setNombre(provincia.getNombre()); dto.setPais(convertPaisToDto(provincia.getPais())); return dto; }
    private PaisResponseDTO convertPaisToDto(Pais pais) { if (pais == null) return null; PaisResponseDTO dto = new PaisResponseDTO(); dto.setId(pais.getId()); dto.setNombre(pais.getNombre()); return dto; }
}