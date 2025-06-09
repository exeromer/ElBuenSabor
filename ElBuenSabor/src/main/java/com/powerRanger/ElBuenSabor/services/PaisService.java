package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Pais;
import java.util.List;

public interface PaisService extends BaseService<Pais, Integer> {
    List<PaisResponseDTO> findAllPaises();
    PaisResponseDTO findPaisById(Integer id) throws Exception;
}