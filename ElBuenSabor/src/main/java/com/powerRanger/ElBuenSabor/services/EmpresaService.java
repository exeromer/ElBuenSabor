package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpresaRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Empresa;
import jakarta.validation.Valid;
import java.util.List;

public interface EmpresaService {
    List<Empresa> getAll();
    Empresa getById(Integer id) throws Exception;
    Empresa create(@Valid EmpresaRequestDTO dto) throws Exception;
    Empresa update(Integer id, @Valid EmpresaRequestDTO dto) throws Exception;
    void delete(Integer id) throws Exception;
}