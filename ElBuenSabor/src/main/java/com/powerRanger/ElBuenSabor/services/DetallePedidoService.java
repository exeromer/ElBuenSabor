package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.DetallePedido; // Usa el nombre de entidad correcto
// import com.powerRanger.ElBuenSabor.dtos.DetallePedidoRequestDTO; // Si usaras DTOs
import jakarta.validation.Valid;
import java.util.List;

public interface DetallePedidoService {
    List<DetallePedido> getAllDetallePedidos();
    DetallePedido getDetallePedidoById(Integer id) throws Exception;
    // Si aceptaras la entidad directamente (menos recomendado para POST/PUT):
    DetallePedido createDetallePedido(@Valid DetallePedido detallePedido) throws Exception;
    DetallePedido updateDetallePedido(Integer id, @Valid DetallePedido detallePedido) throws Exception;
    void deleteDetallePedido(Integer id) throws Exception;
}