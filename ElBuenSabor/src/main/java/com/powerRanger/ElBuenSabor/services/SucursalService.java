package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.SucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.SucursalResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Sucursal;
import jakarta.validation.Valid;
import java.util.List;

public interface SucursalService extends BaseService<Sucursal, Integer> {
    List<SucursalResponseDTO> findAllSucursales();
    SucursalResponseDTO findSucursalById(Integer id) throws Exception;
    SucursalResponseDTO createSucursal(@Valid SucursalRequestDTO dto) throws Exception;
    SucursalResponseDTO updateSucursal(Integer id, @Valid SucursalRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;
}