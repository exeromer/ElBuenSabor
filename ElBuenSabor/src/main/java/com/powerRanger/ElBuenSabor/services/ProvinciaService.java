package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Provincia;
// import jakarta.validation.Valid; // Si usaras DTO para request

import java.util.List;

public interface ProvinciaService {
    List<ProvinciaResponseDTO> obtenerTodas(); // Devuelve lista de DTOs
    ProvinciaResponseDTO obtenerPorId(Integer id) throws Exception; // Devuelve un DTO

    // Métodos de creación/actualización pueden seguir usando la entidad o un RequestDTO
    Provincia guardar(Provincia provincia) throws Exception;
    Provincia actualizar(Integer id, Provincia provinciaDetalles) throws Exception;
    boolean borrar(Integer id) throws Exception;
}