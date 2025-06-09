package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import jakarta.validation.Valid;
import java.util.List;

public interface DomicilioService extends BaseService<Domicilio, Integer> {
    List<DomicilioResponseDTO> findAllDomicilios();
    DomicilioResponseDTO findDomicilioById(Integer id) throws Exception;
    DomicilioResponseDTO createDomicilio(@Valid DomicilioRequestDTO dto) throws Exception;
    DomicilioResponseDTO updateDomicilio(Integer id, @Valid DomicilioRequestDTO dto) throws Exception;
}