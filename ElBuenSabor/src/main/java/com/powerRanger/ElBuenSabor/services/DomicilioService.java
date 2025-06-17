package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO; // Importar el nuevo DTO
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import jakarta.validation.Valid;
import java.util.List;

public interface DomicilioService {
    List<DomicilioResponseDTO> getAll(); // Devuelve lista de DTOs
    DomicilioResponseDTO getById(Integer id) throws Exception; // Devuelve un DTO

    Domicilio create(@Valid DomicilioRequestDTO dto) throws Exception;
    Domicilio update(Integer id, @Valid DomicilioRequestDTO dto) throws Exception;
    void delete(Integer id) throws Exception;
}