package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import jakarta.validation.Valid; // Para validación
import java.util.List;

public interface ArticuloService {
    List<Articulo> getAllArticulos();
    Articulo getArticuloById(Integer id) throws Exception;
    Articulo createArticulo(@Valid Articulo articulo) throws Exception;
    Articulo updateArticulo(Integer id, @Valid Articulo articuloDetalles) throws Exception;
    void deleteArticulo(Integer id) throws Exception;
    Articulo findByDenominacion(String denominacion) throws Exception;
}