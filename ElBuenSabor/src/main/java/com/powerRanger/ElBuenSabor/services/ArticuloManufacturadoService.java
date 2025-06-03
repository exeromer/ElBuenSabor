package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO; // DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado; // Ya no se devuelve entidad
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloManufacturadoService {
    List<ArticuloManufacturadoResponseDTO> getAllArticuloManufacturados(String searchTerm, Boolean estadoActivo);

    ArticuloManufacturadoResponseDTO getArticuloManufacturadoById(Integer id) throws Exception;
    ArticuloManufacturadoResponseDTO createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception;
    ArticuloManufacturadoResponseDTO updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception;

    void deleteArticuloManufacturado(Integer id) throws Exception;

}
