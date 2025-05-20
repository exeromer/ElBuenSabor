package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.SucursalRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Sucursal;
import jakarta.validation.Valid;
import java.util.List;

public interface SucursalService {
    List<Sucursal> getAll();
    Sucursal getById(Integer id) throws Exception;
    Sucursal create(@Valid SucursalRequestDTO dto) throws Exception;
    Sucursal update(Integer id, @Valid SucursalRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;
}