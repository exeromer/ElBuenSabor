package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    List<Cliente> getAllClientes();
    Cliente getClienteById(Integer id) throws Exception;
    // Cliente getClienteByUsuarioAuth0Id(String auth0Id) throws Exception; // Podríamos añadir esto
    Optional<Cliente> getClienteByUsuarioId(Integer usuarioId); //agregado para obtener cliente por ID
    Cliente createCliente(@Valid ClienteRequestDTO dto) throws Exception;
    Cliente updateCliente(Integer id, @Valid ClienteRequestDTO dto) throws Exception;
    void softDeleteCliente(Integer id) throws Exception;
}