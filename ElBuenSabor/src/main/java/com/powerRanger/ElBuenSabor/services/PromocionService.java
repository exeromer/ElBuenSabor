package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PromocionResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Promocion;
import jakarta.validation.Valid;
import java.util.List;

public interface PromocionService extends BaseService<Promocion, Integer> {

    // Métodos específicos que devuelven DTOs y que la implementación debe definir
    List<PromocionResponseDTO> findAllPromociones();
    PromocionResponseDTO findPromocionById(Integer id) throws Exception;
    PromocionResponseDTO createPromocion(@Valid PromocionRequestDTO dto) throws Exception;
    PromocionResponseDTO updatePromocion(Integer id, @Valid PromocionRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;
}