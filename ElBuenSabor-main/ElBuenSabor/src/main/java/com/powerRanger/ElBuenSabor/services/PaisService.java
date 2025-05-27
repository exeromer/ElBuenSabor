package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO; // Importar el nuevo DTO
import com.powerRanger.ElBuenSabor.entities.Pais;
// Importar @Valid si create/update usan DTOs de request
// import jakarta.validation.Valid;

import java.util.List;

public interface PaisService {
    List<PaisResponseDTO> obtenerTodos(); // Devuelve lista de DTOs
    PaisResponseDTO obtenerPorId(Integer id) throws Exception; // Devuelve un DTO

    // Los métodos de creación y actualización siguen usando la entidad directamente.
    // Podríamos crear un PaisRequestDTO si fuera necesario.
    Pais guardar(Pais pais); // Asumo que quieres mantener este nombre por ahora
    Pais actualizar(Integer id, Pais pais) throws Exception;
    boolean borrar(Integer id) throws Exception; // Mantenemos boolean por ahora
}