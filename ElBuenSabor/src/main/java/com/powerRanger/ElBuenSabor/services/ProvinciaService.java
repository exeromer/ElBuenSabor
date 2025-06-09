package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Provincia;
import java.util.List;

public interface ProvinciaService extends BaseService<Provincia, Integer> {
    List<ProvinciaResponseDTO> findAllProvincias();
    ProvinciaResponseDTO findProvinciaById(Integer id) throws Exception;
}