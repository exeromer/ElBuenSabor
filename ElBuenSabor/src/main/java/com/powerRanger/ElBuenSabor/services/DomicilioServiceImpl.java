package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import com.powerRanger.ElBuenSabor.repository.LocalidadRepository;
import com.powerRanger.ElBuenSabor.repository.ProvinciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class DomicilioServiceImpl implements DomicilioService {

    @Autowired
    private DomicilioRepository domicilioRepository;

    @Autowired
    private LocalidadRepository localidadRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;




    // Los métodos de mapeo están bien, los mantenemos para convertir la entidad a DTO de respuesta.
    private DomicilioResponseDTO convertToDto(Domicilio domicilio) {
        if (domicilio == null) return null;
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

    private LocalidadResponseDTO convertLocalidadToDto(Localidad localidad) {
        if (localidad == null) return null;
        LocalidadResponseDTO dto = new LocalidadResponseDTO();
        dto.setId(localidad.getId());
        dto.setNombre(localidad.getNombre());
        dto.setProvincia(convertProvinciaToDto(localidad.getProvincia()));
        return dto;
    }

    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) {
        if (provincia == null) return null;
        ProvinciaResponseDTO dto = new ProvinciaResponseDTO();
        dto.setId(provincia.getId());
        dto.setNombre(provincia.getNombre());
        dto.setPais(convertPaisToDto(provincia.getPais()));
        return dto;
    }

    private PaisResponseDTO convertPaisToDto(Pais pais) {
        if (pais == null) return null;
        PaisResponseDTO dto = new PaisResponseDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        return dto;
    }

    private DomicilioResponseDTO saveOrUpdate(@Valid DomicilioRequestDTO dto, Domicilio domicilio) throws Exception {
        // 1. Valida y obtiene la Provincia usando el provinciaId del DTO.
        Provincia provincia = provinciaRepository.findById(dto.getProvinciaId())
                .orElseThrow(() -> new Exception("Provincia no encontrada con ID: " + dto.getProvinciaId()));

        // 2. Busca la Localidad por su nombre dentro de esa Provincia.
        // Si no la encuentra, la crea automáticamente.
        Localidad localidad = localidadRepository.findByNombreAndProvincia(dto.getLocalidadNombre(), provincia)
                .orElseGet(() -> {
                    Localidad nuevaLocalidad = new Localidad();
                    nuevaLocalidad.setNombre(dto.getLocalidadNombre());
                    nuevaLocalidad.setProvincia(provincia);
                    return localidadRepository.save(nuevaLocalidad);
                });
        // 3. Mapea los datos del DTO a la entidad Domicilio.
        domicilio.setCalle(dto.getCalle());
        domicilio.setNumero(dto.getNumero());
        domicilio.setCp(dto.getCp());
        domicilio.setLocalidad(localidad);
        // 4. Guarda la entidad en la base de datos.
        Domicilio savedDomicilio = domicilioRepository.save(domicilio);
        // 5. Convierte la entidad guardada a un DTO de respuesta y la retorna.
        return convertToDto(savedDomicilio);
    }



    @Override
    @Transactional
    public DomicilioResponseDTO create(DomicilioRequestDTO dto) throws Exception {
        return saveOrUpdate(dto, new Domicilio());
    }

    @Override
    @Transactional
    public DomicilioResponseDTO update(Integer id, DomicilioRequestDTO dto) throws Exception {
        Domicilio domicilioExistente = domicilioRepository.findById(id)
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + id));
        return saveOrUpdate(dto, domicilioExistente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomicilioResponseDTO> getAll() {
        List<Domicilio> domicilios = domicilioRepository.findAll();
        return domicilios.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
    public void delete(Integer id) throws Exception {
        Domicilio domicilioExistente = domicilioRepository.findById(id)
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + id + " para eliminar."));

        if (domicilioExistente.getClientes() != null && !domicilioExistente.getClientes().isEmpty()) {
            throw new Exception("No se puede eliminar el Domicilio ID " + id + " porque está siendo utilizado por uno o más clientes.");
        }
        domicilioRepository.delete(domicilioExistente);
    }
}
