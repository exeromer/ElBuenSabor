package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import jakarta.validation.Valid;
import java.util.List;

public interface ClienteService extends BaseService<Cliente, Integer> {

    List<ClienteResponseDTO> findAllClientes(String searchTerm); // Renombrado para claridad
    ClienteResponseDTO findClienteById(Integer id) throws Exception; // Renombrado
    ClienteResponseDTO createCliente(@Valid ClienteRequestDTO dto) throws Exception;
    ClienteResponseDTO updateCliente(Integer id, @Valid ClienteRequestDTO dto) throws Exception;
    void softDeleteCliente(Integer id) throws Exception;
}