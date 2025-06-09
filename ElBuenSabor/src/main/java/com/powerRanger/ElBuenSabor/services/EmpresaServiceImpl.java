package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpresaRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpresaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Empresa;
import com.powerRanger.ElBuenSabor.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class EmpresaServiceImpl extends BaseServiceImpl<Empresa, EmpresaRepository> implements EmpresaService {

    public EmpresaServiceImpl(EmpresaRepository empresaRepository) {
        super(empresaRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> findAllEmpresas() {
        try {
            return super.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar empresas: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponseDTO findEmpresaById(Integer id) throws Exception {
        return convertToDto(super.findById(id));
    }

    @Override
    @Transactional
    public EmpresaResponseDTO createEmpresa(@Valid EmpresaRequestDTO dto) throws Exception {
        Empresa empresa = new Empresa();
        mapDtoToEntity(dto, empresa);
        return convertToDto(super.save(empresa));
    }

    @Override
    @Transactional
    public EmpresaResponseDTO updateEmpresa(Integer id, @Valid EmpresaRequestDTO dto) throws Exception {
        Empresa empresaExistente = super.findById(id);
        mapDtoToEntity(dto, empresaExistente);
        return convertToDto(super.update(id, empresaExistente));
    }

    @Override
    @Transactional
    public boolean delete(Integer id) throws Exception {
        Empresa empresa = super.findById(id);
        if (empresa.getSucursales() != null && !empresa.getSucursales().isEmpty()) {
            throw new Exception("No se puede eliminar la empresa ID " + id + " porque tiene sucursales asociadas.");
        }
        return super.delete(id);
    }

    private EmpresaResponseDTO convertToDto(Empresa empresa) {
        EmpresaResponseDTO dto = new EmpresaResponseDTO();
        dto.setId(empresa.getId());
        dto.setNombre(empresa.getNombre());
        dto.setRazonSocial(empresa.getRazonSocial());
        dto.setCuil(empresa.getCuil());
        return dto;
    }

    private void mapDtoToEntity(EmpresaRequestDTO dto, Empresa empresa) {
        empresa.setNombre(dto.getNombre());
        empresa.setRazonSocial(dto.getRazonSocial());
        empresa.setCuil(dto.getCuil());
    }
}