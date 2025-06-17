package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloBaseResponseDTO; // Usar DTO base
import com.powerRanger.ElBuenSabor.entities.Articulo; // Para create/update por ahora
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloService {
    List<ArticuloBaseResponseDTO> getAllArticulos();
    ArticuloBaseResponseDTO getArticuloById(Integer id) throws Exception;

    // create y update podrían aceptar DTOs de request si los tienes definidos para Articulo base
    // y devolver ArticuloBaseResponseDTO. Por ahora, mantenemos como estaban.
    Articulo createArticulo(@Valid Articulo articulo) throws Exception;
    Articulo updateArticulo(Integer id, @Valid Articulo articuloDetalles) throws Exception;

    void deleteArticulo(Integer id) throws Exception;
    ArticuloBaseResponseDTO findByDenominacion(String denominacion) throws Exception;
}