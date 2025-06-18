package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente; // Importar la entidad Cliente
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import jakarta.validation.Valid;
import java.util.List;
import com.powerRanger.ElBuenSabor.dtos.MercadoPagoCreatePreferenceDTO; // Importar el nuevo DTO


public interface PedidoService {
    List<PedidoResponseDTO> getAll();
    PedidoResponseDTO getById(Integer id) throws Exception;
    List<PedidoResponseDTO> getPedidosByClienteId(Integer clienteId) throws Exception;
    List<PedidoResponseDTO> getPedidosByClienteAuth0Id(String auth0Id) throws Exception;

    PedidoResponseDTO create(@Valid PedidoRequestDTO dto) throws Exception;
    PedidoResponseDTO createForAuthenticatedClient(String auth0Id, @Valid PedidoRequestDTO dto) throws Exception;


    PedidoResponseDTO crearPedidoDesdeCarrito(Cliente cliente, @Valid CrearPedidoRequestDTO pedidoRequest) throws Exception; // Nuevo método


    String createPreferenceMp(String auth0Id, @Valid MercadoPagoCreatePreferenceDTO dto) throws Exception; // Método nuevo

    PedidoResponseDTO updateEstado(Integer id, Estado nuevoEstado) throws Exception;
    void softDelete(Integer id) throws Exception;
}