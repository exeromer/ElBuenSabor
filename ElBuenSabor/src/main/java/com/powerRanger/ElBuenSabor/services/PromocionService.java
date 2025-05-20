package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Promocion;
import jakarta.validation.Valid;
import java.util.List;

public interface PromocionService {
    List<Promocion> getAll();
    Promocion getById(Integer id) throws Exception;
    Promocion create(@Valid PromocionRequestDTO dto) throws Exception;
    Promocion update(Integer id, @Valid PromocionRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;
}