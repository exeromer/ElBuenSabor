package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UnidadMedidaResponseDTO; // Importar el nuevo DTO
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import jakarta.validation.Valid;
import java.util.List;

public interface UnidadMedidaService {
    List<UnidadMedidaResponseDTO> getAll(); // Devuelve lista de DTOs
    UnidadMedidaResponseDTO getById(Integer id) throws Exception; // Devuelve un DTO

    // Los métodos de creación y actualización siguen usando la entidad directamente
    // o podrían refactorizarse para usar un UnidadMedidaRequestDTO si fuera necesario.
    UnidadMedida create(@Valid UnidadMedida unidadMedida) throws Exception;
    UnidadMedida update(Integer id, @Valid UnidadMedida unidadMedida) throws Exception;
    void delete(Integer id) throws Exception;
}