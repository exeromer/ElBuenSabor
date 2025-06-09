package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Localidad;
import java.util.List;

public interface LocalidadService extends BaseService<Localidad, Integer> {
    List<LocalidadResponseDTO> findAllLocalidades();
    LocalidadResponseDTO findLocalidadById(Integer id) throws Exception;
}