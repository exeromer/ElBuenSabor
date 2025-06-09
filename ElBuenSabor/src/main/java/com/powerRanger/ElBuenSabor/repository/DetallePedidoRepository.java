package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DetallePedidoRepository extends BaseRepository<DetallePedido, Integer> { // Cambiado a BaseRepository

    @Query("SELECT NEW com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO(art.id, art.denominacion, SUM(dp.cantidad)) " +
            "FROM DetallePedido dp JOIN dp.articulo art " +
            "JOIN dp.pedido ped " +
            "WHERE TYPE(art) = com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado " +
            "AND ped.estadoActivo = true AND ped.estado = :estado " +
            "AND (:fechaDesde IS NULL OR ped.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR ped.fechaPedido <= :fechaHasta) " +
            "GROUP BY art.id, art.denominacion " +
            "ORDER BY SUM(dp.cantidad) DESC, art.denominacion ASC")
    List<ArticuloManufacturadoRankingDTO> findRankingArticulosManufacturadosMasVendidos(
            @Param("estado") Estado estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );
}