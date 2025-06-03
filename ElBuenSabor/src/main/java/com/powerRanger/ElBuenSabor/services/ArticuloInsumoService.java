package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO; // DTO de respuesta
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo; // Para create/update
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloInsumoService {

    List<ArticuloInsumoResponseDTO> getAllArticuloInsumo(String searchTerm, Boolean estadoActivo);

    ArticuloInsumoResponseDTO getArticuloInsumoById(Integer id) throws Exception;
    ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumoRequestDTO dto) throws Exception;
    ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumoRequestDTO dto) throws Exception;
    void deleteArticuloInsumo(Integer id) throws Exception;
}
