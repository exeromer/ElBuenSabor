package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.SucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.SucursalResponseDTO; // Importar el nuevo DTO
// import com.powerRanger.ElBuenSabor.entities.Sucursal; // Ya no se devuelve la entidad directamente
import jakarta.validation.Valid;
import java.util.List;

public interface SucursalService {
    List<SucursalResponseDTO> getAll(); // Devuelve lista de DTOs
    SucursalResponseDTO getById(Integer id) throws Exception; // Devuelve un DTO

    // Los métodos de creación y actualización aceptan RequestDTO y devuelven ResponseDTO
    SucursalResponseDTO create(@Valid SucursalRequestDTO dto) throws Exception;
    SucursalResponseDTO update(Integer id, @Valid SucursalRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;
}