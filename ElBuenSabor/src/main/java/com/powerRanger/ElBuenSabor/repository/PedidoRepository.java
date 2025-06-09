package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends BaseRepository<Pedido, Integer> { // Cambiado a BaseRepository

    List<Pedido> findByClienteIdAndEstadoActivoTrueOrderByFechaPedidoDesc(Integer clienteId);

    Optional<Pedido> findByMercadoPagoPreferenceId(String mercadoPagoPreferenceId);

    @Query("SELECT NEW com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO(" +
            "c.id, CONCAT(c.nombre, ' ', c.apellido), c.email, " +
            "COUNT(p.id) AS cantidadPedidos, " +
            "SUM(p.total) AS montoTotalComprado) " +
            "FROM Pedido p JOIN p.cliente c " +
            "WHERE p.estadoActivo = true " +
            "AND p.estado = :estado " +
            "AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta) " +
            "GROUP BY c.id, c.nombre, c.apellido, c.email " +
            "ORDER BY cantidadPedidos DESC, SUM(p.total) DESC, c.apellido ASC, c.nombre ASC")
    List<ClienteRankingDTO> findRankingClientesByCantidadPedidos(
            @Param("estado") Estado estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );

    @Query("SELECT NEW com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO(" +
            "c.id, CONCAT(c.nombre, ' ', c.apellido), c.email, " +
            "COUNT(p.id) AS cantidadPedidos, " +
            "SUM(p.total) AS montoTotalComprado) " +
            "FROM Pedido p JOIN p.cliente c " +
            "WHERE p.estadoActivo = true " +
            "AND p.estado = :estado " +
            "AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta) " +
            "GROUP BY c.id, c.nombre, c.apellido, c.email " +
            "ORDER BY montoTotalComprado DESC, COUNT(p.id) DESC, c.apellido ASC, c.nombre ASC")
    List<ClienteRankingDTO> findRankingClientesByMontoTotal(
            @Param("estado") Estado estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );
}