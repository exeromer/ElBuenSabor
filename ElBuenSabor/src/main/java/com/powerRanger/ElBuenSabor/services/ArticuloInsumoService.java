package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO; // DTO de respuesta
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo; // Para create/update
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloInsumoService {
    List<ArticuloInsumoResponseDTO> getAllArticuloInsumo();
    ArticuloInsumoResponseDTO getArticuloInsumoById(Integer id) throws Exception;

    // Asumimos que el create/update acepta la entidad y devuelve DTO
    ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumo articuloInsumo) throws Exception;
    ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumo articuloInsumoDetails) throws Exception;

    void deleteArticuloInsumo(Integer id) throws Exception;
}