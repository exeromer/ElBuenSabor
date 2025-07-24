package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByClienteIdAndEstadoActivoTrueOrderByFechaPedidoDesc(Integer clienteId);

    @Query("SELECT NEW com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO(" +
            "c.id, CONCAT(c.nombre, ' ', c.apellido), c.email, " +
            "COUNT(p.id) AS cantidadPedidos, " +
            "SUM(p.total) AS montoTotalComprado) " +
            "FROM Pedido p JOIN p.cliente c " +
            "WHERE p.sucursal.id = :sucursalId " +
            "AND p.estado = :estado " +
            "AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta) " +
            "GROUP BY c.id, c.nombre, c.apellido, c.email " +
            "ORDER BY cantidadPedidos DESC, SUM(p.total) DESC, c.apellido ASC, c.nombre ASC")
    List<ClienteRankingDTO> findRankingClientesByCantidadPedidos(
            @Param("sucursalId") Integer sucursalId,
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
            "WHERE p.sucursal.id = :sucursalId " +
            "AND p.estado = :estado " +
            "AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta) " +
            "GROUP BY c.id, c.nombre, c.apellido, c.email " +
            "ORDER BY montoTotalComprado DESC, COUNT(p.id) DESC, c.apellido ASC, c.nombre ASC")
    List<ClienteRankingDTO> findRankingClientesByMontoTotal(
            @Param("sucursalId") Integer sucursalId,
            @Param("estado") Estado estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );

    // --- NUEVOS MÉTODOS PARA CAJERO, COCINA, DELIVERY ---
    @Query("SELECT p FROM Pedido p " +
            "WHERE p.sucursal.id = :sucursalId " +
            "AND p.estadoActivo = true " + // Solo pedidos activos
            "AND (:estado IS NULL OR p.estado = :estado) " +
            "AND (:pedidoId IS NULL OR p.id = :pedidoId) " +
            "AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta) " +
            "ORDER BY p.fechaPedido DESC, p.horaEstimadaFinalizacion DESC")
    List<Pedido> findPedidosForCashier(
            @Param("sucursalId") Integer sucursalId,
            @Param("estado") Estado estado,
            @Param("pedidoId") Integer pedidoId,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta
    );


    @Query("SELECT p FROM Pedido p " +
            "WHERE p.sucursal.id = :sucursalId " +
            "AND p.estado = 'PREPARACION' " +
            "AND p.estadoActivo = true " +
            "ORDER BY p.fechaPedido ASC, p.horaEstimadaFinalizacion ASC")
    List<Pedido> findPedidosForKitchen(@Param("sucursalId") Integer sucursalId);


    @Query("SELECT p FROM Pedido p " +
            "WHERE p.sucursal.id = :sucursalId " +
            "AND p.estado = 'EN_CAMINO' " +
            "AND p.tipoEnvio = 'DELIVERY' " + // Solo DELIVERY para el delivery
            "AND p.estadoActivo = true " +
            "ORDER BY p.fechaPedido ASC, p.horaEstimadaFinalizacion ASC")
    List<Pedido> findPedidosForDelivery(@Param("sucursalId") Integer sucursalId);


    Optional<Pedido> findByIdAndSucursalId(Integer pedidoId, Integer sucursalId);
    
   
    @Query("SELECT SUM(p.total) FROM Pedido p " +
            "WHERE p.sucursal.id = :sucursalId " +
            "AND p.estadoActivo = true " +
            "AND p.estado = 'ENTREGADO' " +
            "AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta)")
    Double sumTotalByEstadoAndFechaRange(@Param("sucursalId") Integer sucursalId, @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta);

    @Query("SELECT SUM(p.totalCosto) FROM Pedido p " +
            "WHERE p.sucursal.id = :sucursalId " +
            "AND p.estadoActivo = true " +
            "AND p.estado = 'ENTREGADO' " +
            "AND (:fechaDesde IS NULL OR p.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR p.fechaPedido <= :fechaHasta)")
    Double sumTotalCostoByEstadoAndFechaRange(@Param("sucursalId") Integer sucursalId, @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta);
    
}