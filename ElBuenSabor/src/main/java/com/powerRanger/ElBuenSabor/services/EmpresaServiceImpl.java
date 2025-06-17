package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpresaRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpresaResponseDTO; // Importar DTO
// Si EmpresaResponseDTO incluye SucursalSimpleResponseDTO, necesitaríamos el mapper de Sucursal
// import com.powerRanger.ElBuenSabor.dtos.SucursalSimpleResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Empresa;
// import com.powerRanger.ElBuenSabor.entities.Sucursal; // Si mapeamos sucursales
import com.powerRanger.ElBuenSabor.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    // Método de Mapeo de Entidad a DTO
    private EmpresaResponseDTO convertToDto(Empresa empresa) {
        EmpresaResponseDTO dto = new EmpresaResponseDTO();
        dto.setId(empresa.getId());
        dto.setNombre(empresa.getNombre());
        dto.setRazonSocial(empresa.getRazonSocial());
        dto.setCuil(empresa.getCuil());
        // Si decidimos incluir sucursales en el DTO (ej. una lista de DTOs de sucursal simples):
        // if (empresa.getSucursales() != null) {
        //     List<SucursalSimpleResponseDTO> sucursalDtos = empresa.getSucursales().stream()
        //         .map(sucursal -> {
        //             SucursalSimpleResponseDTO sucDto = new SucursalSimpleResponseDTO();
        //             sucDto.setId(sucursal.getId());
        //             sucDto.setNombre(sucursal.getNombre());
        //             // ... otros campos simples de sucursal ...
        //             return sucDto;
        //         }).collect(Collectors.toList());
        //     dto.setSucursales(sucursalDtos);
        // }
        return dto;
    }

    private void mapDtoToEntity(EmpresaRequestDTO dto, Empresa empresa) {
        empresa.setNombre(dto.getNombre());
        empresa.setRazonSocial(dto.getRazonSocial());
        empresa.setCuil(dto.getCuil());
        // Las sucursales se gestionan creando/actualizando Sucursal y asignándole esta Empresa.
        // No se gestionan directamente desde el DTO de Empresa.
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> getAll() {
        List<Empresa> empresas = empresaRepository.findAll();
        return empresas.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponseDTO getById(Integer id) throws Exception {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new Exception("Empresa no encontrada con ID: " + id));
        return convertToDto(empresa);
    }

    @Override
    @Transactional
    public Empresa create(@Valid EmpresaRequestDTO dto) throws Exception {
        // Validar unicidad de CUIT o nombre si es necesario
        // if(empresaRepository.findByCuil(dto.getCuil()).isPresent()){ ... }
        Empresa empresa = new Empresa();
        mapDtoToEntity(dto, empresa);
        return empresaRepository.save(empresa);
    }

    @Override
    @Transactional
    public Empresa update(Integer id, @Valid EmpresaRequestDTO dto) throws Exception {
        Empresa empresaExistente = empresaRepository.findById(id) // Obtener la entidad primero
                .orElseThrow(() -> new Exception("Empresa no encontrada con ID: " + id));

        // Validar unicidad de CUIT o nombre para otros registros si es necesario
        // empresaRepository.findByCuil(dto.getCuil()).ifPresent(e -> {
        //     if(!e.getId().equals(id)) throw new RuntimeException("El CUIT ya está en uso por otra empresa.");
        // });

        mapDtoToEntity(dto, empresaExistente);
        return empresaRepository.save(empresaExistente);
    }

    @Override
    @Transactional
    public void delete(Integer id) throws Exception {
        Empresa empresa = empresaRepository.findById(id) // Obtener la entidad primero
                .orElseThrow(() -> new Exception("Empresa no encontrada con ID: " + id));

        // La cascada (CascadeType.ALL y orphanRemoval=true en Empresa.sucursales)
        // se encargará de borrar las sucursales asociadas.
        // Si no se quisiera este comportamiento, se debería validar aquí:
        // if (empresa.getSucursales() != null && !empresa.getSucursales().isEmpty()) {
        //    throw new Exception("No se puede eliminar la empresa ID " + id + " porque tiene sucursales asociadas.");
        // }
        empresaRepository.delete(empresa);
    }
}