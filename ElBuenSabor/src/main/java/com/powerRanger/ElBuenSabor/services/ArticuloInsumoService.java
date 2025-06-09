package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloInsumoService extends BaseService<ArticuloInsumo, Integer> {

    List<ArticuloInsumoResponseDTO> findAllInsumos(String searchTerm, Boolean estadoActivo);
    ArticuloInsumoResponseDTO findInsumoById(Integer id) throws Exception;
    ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumoRequestDTO dto) throws Exception;
    ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumoRequestDTO dto) throws Exception;
}