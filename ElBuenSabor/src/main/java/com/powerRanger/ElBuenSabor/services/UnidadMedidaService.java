package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UnidadMedidaResponseDTO;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import jakarta.validation.Valid;
import java.util.List;

public interface UnidadMedidaService extends BaseService<UnidadMedida, Integer> {
    List<UnidadMedidaResponseDTO> findAllUnidades();
    UnidadMedidaResponseDTO findUnidadById(Integer id) throws Exception;
}