package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO; // Importar el nuevo DTO
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import jakarta.validation.Valid;
import java.util.List;

public interface DomicilioService {
    List<DomicilioResponseDTO> getAll(); // Devuelve lista de DTOs
    DomicilioResponseDTO getById(Integer id) throws Exception; // Devuelve un DTO
    DomicilioResponseDTO create(DomicilioRequestDTO domicilio) throws Exception;
    DomicilioResponseDTO update(Integer id, DomicilioRequestDTO domicilio) throws Exception;
    void delete(Integer id) throws Exception;
}