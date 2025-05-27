package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Cliente; // Ya no se devuelve directamente
import jakarta.validation.Valid;
import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> getAllClientes();
    ClienteResponseDTO getClienteById(Integer id) throws Exception;
    // Opcional: ClienteResponseDTO getClienteByUsuarioAuth0Id(String auth0Id) throws Exception;

    ClienteResponseDTO createCliente(@Valid ClienteRequestDTO dto) throws Exception;
    ClienteResponseDTO updateCliente(Integer id, @Valid ClienteRequestDTO dto) throws Exception;
    void softDeleteCliente(Integer id) throws Exception;
}