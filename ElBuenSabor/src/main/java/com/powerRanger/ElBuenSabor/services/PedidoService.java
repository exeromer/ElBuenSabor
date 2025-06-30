package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import jakarta.validation.Valid;
import java.time.LocalDate; // Importar LocalDate
import java.time.LocalTime; // Importar LocalTime
import java.util.List;
import java.util.Map;

import com.powerRanger.ElBuenSabor.dtos.MercadoPagoCreatePreferenceDTO;


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

    PedidoResponseDTO updateEstadoParaEmpleado(Integer pedidoId, Estado nuevoEstado, Integer sucursalId, String auth0Id) throws Exception;

    PedidoResponseDTO addTiempoCocina(Integer pedidoId, Integer minutosToAdd, Integer sucursalId) throws Exception;


    void softDelete(Integer id) throws Exception;
    void handleMercadoPagoNotification(Map<String, String> notification) throws Exception;

    // --- Nuevos métodos para los roles ---


    List<PedidoResponseDTO> getPedidosParaCajero(Integer sucursalId, Estado estado, Integer pedidoId, LocalDate fechaDesde, LocalDate fechaHasta) throws Exception;


    List<PedidoResponseDTO> getPedidosParaCocina(Integer sucursalId) throws Exception;

    List<PedidoResponseDTO> getPedidosParaDelivery(Integer sucursalId) throws Exception;
}