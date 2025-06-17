package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteResponseDTO; // Importar DTO de respuesta
import jakarta.validation.Valid;
import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> getAllClientes(String searchTerm); // Modificado
    ClienteResponseDTO getClienteById(Integer id) throws Exception;
    ClienteResponseDTO getMyProfile(String auth0Id) throws Exception; // Metodo para obtener el perfil del cliente autenticado

    ClienteResponseDTO findOrCreateClienteByAuth0Id(String auth0Id, String email) throws Exception;

    ClienteResponseDTO createCliente(@Valid ClienteRequestDTO dto) throws Exception;
    ClienteResponseDTO updateCliente(Integer id, @Valid ClienteRequestDTO dto) throws Exception;
    void softDeleteCliente(Integer id) throws Exception;
}