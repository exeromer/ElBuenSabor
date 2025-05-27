package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO; // Importar el nuevo DTO
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente; // Importar la entidad Cliente
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import jakarta.validation.Valid;
import java.util.List;

public interface PedidoService {
    List<PedidoResponseDTO> getAll();
    PedidoResponseDTO getById(Integer id) throws Exception;
    List<PedidoResponseDTO> getPedidosByClienteId(Integer clienteId) throws Exception;
    List<PedidoResponseDTO> getPedidosByClienteAuth0Id(String auth0Id) throws Exception;

    PedidoResponseDTO create(@Valid PedidoRequestDTO dto) throws Exception;
    PedidoResponseDTO createForAuthenticatedClient(String auth0Id, @Valid PedidoRequestDTO dto) throws Exception;

    /**
     * Crea un nuevo Pedido a partir del carrito activo del Cliente y la información proporcionada.
     * Este proceso incluye la validación de stock, cálculo de costos, y limpieza del carrito.
     * @param cliente El Cliente que realiza el pedido.
     * @param pedidoRequest DTO con la información adicional para el pedido (domicilio, envío, pago, sucursal).
     * @return PedidoResponseDTO del pedido recién creado.
     * @throws Exception Si el carrito está vacío, hay stock insuficiente, o cualquier otro error de validación/proceso.
     */
    PedidoResponseDTO crearPedidoDesdeCarrito(Cliente cliente, @Valid CrearPedidoRequestDTO pedidoRequest) throws Exception; // Nuevo método

    PedidoResponseDTO updateEstado(Integer id, Estado nuevoEstado) throws Exception;
    void softDelete(Integer id) throws Exception;
}