package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO; // Importar el DTO
import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import jakarta.validation.Valid; // Asegúrate de tener este import
import java.util.List;

public interface ArticuloManufacturadoService {
    List<ArticuloManufacturado> getAllArticuloManufacturados();
    ArticuloManufacturado getArticuloManufacturadoById(Integer id) throws Exception;
    // Acepta el DTO, devuelve la entidad
    ArticuloManufacturado createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception; // ✅ AÑADIDO @Valid aquí
    // Acepta el DTO, devuelve la entidad
    ArticuloManufacturado updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception; // ✅ AÑADIDO @Valid aquí (o asegúrate que ya esté)
    void deleteArticuloManufacturado(Integer id) throws Exception;
}