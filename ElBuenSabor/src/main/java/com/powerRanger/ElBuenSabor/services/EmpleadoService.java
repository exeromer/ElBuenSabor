package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpleadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpleadoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
import jakarta.validation.Valid;
import java.util.List;

public interface EmpleadoService {
    List<EmpleadoResponseDTO> getAll(String searchTerm, RolEmpleado rolEmpleado);
    EmpleadoResponseDTO getById(Integer id) throws Exception;
    EmpleadoResponseDTO create(@Valid EmpleadoRequestDTO dto) throws Exception;
    EmpleadoResponseDTO update(Integer id, @Valid EmpleadoRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;
    EmpleadoResponseDTO getByUsuarioId(Integer usuarioId) throws Exception;
}