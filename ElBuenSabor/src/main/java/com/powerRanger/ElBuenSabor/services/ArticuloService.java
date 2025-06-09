package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloBaseResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Articulo;
import jakarta.validation.Valid;
import java.util.List;

public interface ArticuloService extends BaseService<Articulo, Integer> {
    List<ArticuloBaseResponseDTO> findAllArticulos();
    ArticuloBaseResponseDTO findArticuloById(Integer id) throws Exception;
    ArticuloBaseResponseDTO findByDenominacion(String denominacion) throws Exception;

    Articulo createArticulo(@Valid Articulo articulo) throws Exception;
    Articulo updateArticulo(Integer id, @Valid Articulo articuloDetalles) throws Exception;
}