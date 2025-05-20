package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import jakarta.validation.Valid;
import java.util.List;

public interface PedidoService {
    List<Pedido> getAll();
    Pedido getById(Integer id) throws Exception;
    List<Pedido> getPedidosByClienteId(Integer clienteId) throws Exception;
    List<Pedido> getPedidosByClienteAuth0Id(String auth0Id) throws Exception;
    Pedido create(@Valid PedidoRequestDTO dto) throws Exception;
    Pedido createForAuthenticatedClient(String auth0Id, @Valid PedidoRequestDTO dto) throws Exception;
    Pedido updateEstado(Integer id, Estado nuevoEstado) throws Exception;
    void softDelete(Integer id) throws Exception;
}