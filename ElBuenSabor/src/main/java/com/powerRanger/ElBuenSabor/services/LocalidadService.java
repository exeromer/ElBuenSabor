package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Localidad;
// import jakarta.validation.Valid; // Si usaras DTO para request

import java.util.List;

public interface LocalidadService {
    List<LocalidadResponseDTO> obtenerTodas(); // Devuelve lista de DTOs
    LocalidadResponseDTO obtenerPorId(Integer id) throws Exception; // Devuelve un DTO

    Localidad guardar(Localidad localidad) throws Exception;
    Localidad actualizar(Integer id, Localidad localidadDetalles) throws Exception;
    boolean borrar(Integer id) throws Exception;
}