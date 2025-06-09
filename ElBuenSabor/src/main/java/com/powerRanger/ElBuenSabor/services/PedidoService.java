package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map; // Para la respuesta de MP

public interface PedidoService {
    List<PedidoResponseDTO> getAll();
    PedidoResponseDTO getById(Integer id) throws Exception;
    List<PedidoResponseDTO> getPedidosByClienteId(Integer clienteId) throws Exception;
    List<PedidoResponseDTO> getPedidosByClienteAuth0Id(String auth0Id) throws Exception;

    PedidoResponseDTO create(@Valid PedidoRequestDTO dto) throws Exception;
    PedidoResponseDTO createForAuthenticatedClient(String auth0Id, @Valid PedidoRequestDTO dto) throws Exception;

    PedidoResponseDTO crearPedidoDesdeCarrito(Cliente cliente, @Valid CrearPedidoRequestDTO pedidoRequest) throws Exception;

    PedidoResponseDTO updateEstado(Integer id, Estado nuevoEstado) throws Exception;
    void softDelete(Integer id) throws Exception;


    PedidoResponseDTO procesarNotificacionMercadoPago(String paymentId, String status, String externalReference) throws Exception;

    // Método para que el controller llame y guarde el preferenceId en el pedido
    // Es una alternativa a que el controller lo haga directamente con el repositorio
    void actualizarPreferenciaMercadoPago(Integer pedidoId, String preferenceId) throws Exception;
}