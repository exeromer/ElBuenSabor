package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloManufacturadoService extends BaseService<ArticuloManufacturado, Integer> {

    List<ArticuloManufacturadoResponseDTO> findAllManufacturados(String searchTerm, Boolean estadoActivo);
    ArticuloManufacturadoResponseDTO findManufacturadoById(Integer id) throws Exception;
    ArticuloManufacturadoResponseDTO createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception;
    ArticuloManufacturadoResponseDTO updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception;
}