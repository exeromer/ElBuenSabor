package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import jakarta.validation.Valid; // Asegúrate de tener este import
import java.util.List;

public interface UnidadMedidaService {
    List<UnidadMedida> getAll();
    UnidadMedida getById(Integer id) throws Exception;
    UnidadMedida create(@Valid UnidadMedida unidadMedida) throws Exception; // Este ya estaba bien
    UnidadMedida update(Integer id, @Valid UnidadMedida unidadMedida) throws Exception; // ✅ AÑADIDO @Valid aquí
    void delete(Integer id) throws Exception;
}