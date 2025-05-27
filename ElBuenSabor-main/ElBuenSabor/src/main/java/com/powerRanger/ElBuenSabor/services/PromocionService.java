package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PromocionResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Promocion; // Ya no se devuelve entidad
import jakarta.validation.Valid;
import java.util.List;

public interface PromocionService {
    List<PromocionResponseDTO> getAll(); // Devuelve lista de DTOs
    PromocionResponseDTO getById(Integer id) throws Exception; // Devuelve un DTO

    PromocionResponseDTO create(@Valid PromocionRequestDTO dto) throws Exception; // Acepta RequestDTO, devuelve ResponseDTO
    PromocionResponseDTO update(Integer id, @Valid PromocionRequestDTO dto) throws Exception; // Acepta RequestDTO, devuelve ResponseDTO

    void softDelete(Integer id) throws Exception;
}