package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import jakarta.validation.Valid; // Asegúrate de tener este import
import java.util.List;

public interface DomicilioService {
    List<Domicilio> getAll();
    Domicilio getById(Integer id) throws Exception;
    Domicilio create(@Valid DomicilioRequestDTO dto) throws Exception; // Asumo que este ya tiene @Valid
    Domicilio update(Integer id, @Valid DomicilioRequestDTO dto) throws Exception; // ✅ AÑADE O VERIFICA @Valid AQUÍ
    void delete(Integer id) throws Exception;
}