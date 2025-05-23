package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO; // DTO de respuesta para Domicilio
import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO; // DTO de respuesta para Localidad
import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO; // DTO de respuesta para Provincia
import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;      // DTO de respuesta para Pais
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import com.powerRanger.ElBuenSabor.repository.LocalidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
// import java.util.stream.Collectors; // No se usa directamente si mapeas individualmente

@Service
@Validated
public class DomicilioServiceImpl implements DomicilioService {

    @Autowired
    private DomicilioRepository domicilioRepository;
    @Autowired
    private LocalidadRepository localidadRepository; // Para buscar Localidad al crear/actualizar

    // Métodos de Mapeo Anidados (podrían estar en sus respectivos servicios o en una clase Mapper)
    private PaisResponseDTO convertPaisToDto(Pais pais) {
        if (pais == null) return null;
        PaisResponseDTO dto = new PaisResponseDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        return dto;
    }

    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) {
        if (provincia == null) return null;
        ProvinciaResponseDTO dto = new ProvinciaResponseDTO();
        dto.setId(provincia.getId());
        dto.setNombre(provincia.getNombre());
        dto.setPais(convertPaisToDto(provincia.getPais())); // Anidar DTO de Pais
        return dto;
    }

    private LocalidadResponseDTO convertLocalidadToDto(Localidad localidad) {
        if (localidad == null) return null;
        LocalidadResponseDTO dto = new LocalidadResponseDTO();
        dto.setId(localidad.getId());
        dto.setNombre(localidad.getNombre());
        dto.setProvincia(convertProvinciaToDto(localidad.getProvincia())); // Anidar DTO de Provincia
        return dto;
    }

    // Método de Mapeo Principal de Entidad Domicilio a DomicilioResponseDTO
    private DomicilioResponseDTO convertToDto(Domicilio domicilio) {
        DomicilioResponseDTO dto = new DomicilioResponseDTO();
        dto.setId(domicilio.getId());
        dto.setCalle(domicilio.getCalle());
        dto.setNumero(domicilio.getNumero());
        dto.setCp(domicilio.getCp());
        if (domicilio.getLocalidad() != null) {
            dto.setLocalidad(convertLocalidadToDto(domicilio.getLocalidad()));
        }
        // Si decides incluir IDs de clientes:
        // if (domicilio.getClientes() != null) {
        //     dto.setClienteIds(domicilio.getClientes().stream().map(Cliente::getId).collect(Collectors.toList()));
        // }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomicilioResponseDTO> getAll() {
        List<Domicilio> domicilios = domicilioRepository.findAll();
        return domicilios.stream()
                .map(this::convertToDto)
                .collect(java.util.stream.Collectors.toList()); // Explicit Collectors import
    }

    @Override
    @Transactional(readOnly = true)
    public DomicilioResponseDTO getById(Integer id) throws Exception {
        Domicilio domicilio = domicilioRepository.findById(id)
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + id));
        return convertToDto(domicilio);
    }

    @Override
    @Transactional
    public Domicilio create(@Valid DomicilioRequestDTO dto) throws Exception {
        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new Exception("Localidad no encontrada con ID: " + dto.getLocalidadId()));

        Domicilio domicilio = new Domicilio();
        domicilio.setCalle(dto.getCalle());
        domicilio.setNumero(dto.getNumero());
        domicilio.setCp(dto.getCp());
        domicilio.setLocalidad(localidad);
        return domicilioRepository.save(domicilio);
    }

    @Override
    @Transactional
    public Domicilio update(Integer id, @Valid DomicilioRequestDTO dto) throws Exception {
        Domicilio domicilioExistente = domicilioRepository.findById(id) // Buscar primero
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + id));

        Localidad localidad = localidadRepository.findById(dto.getLocalidadId())
                .orElseThrow(() -> new Exception("Localidad no encontrada con ID: " + dto.getLocalidadId()));

        domicilioExistente.setCalle(dto.getCalle());
        domicilioExistente.setNumero(dto.getNumero());
        domicilioExistente.setCp(dto.getCp());
        domicilioExistente.setLocalidad(localidad);
        return domicilioRepository.save(domicilioExistente);
    }

    @Override
    @Transactional
    public void delete(Integer id) throws Exception {
        Domicilio domicilioExistente = domicilioRepository.findById(id)
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + id + " para eliminar."));

        if (domicilioExistente.getClientes() != null && !domicilioExistente.getClientes().isEmpty()) {
            throw new Exception("No se puede eliminar el Domicilio ID " + id + " porque está siendo utilizado por uno o más clientes.");
        }
        // Aquí también deberías verificar si está en uso por Sucursal antes de borrar.
        // if (sucursalRepository.existsByDomicilioId(id)) { // Necesitarías SucursalRepository y el método
        //     throw new Exception("No se puede eliminar el Domicilio ID " + id + " porque está siendo utilizado por una sucursal.");
        // }
        domicilioRepository.delete(domicilioExistente);
    }
}