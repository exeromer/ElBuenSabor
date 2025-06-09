package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpresaRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpresaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Empresa;
import jakarta.validation.Valid;
import java.util.List;

public interface EmpresaService extends BaseService<Empresa, Integer> {
    List<EmpresaResponseDTO> findAllEmpresas();
    EmpresaResponseDTO findEmpresaById(Integer id) throws Exception;
    EmpresaResponseDTO createEmpresa(@Valid EmpresaRequestDTO dto) throws Exception;
    EmpresaResponseDTO updateEmpresa(Integer id, @Valid EmpresaRequestDTO dto) throws Exception;
}