package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRankingDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    // MODIFICADO: Ahora se llama 'findRankingProductosCocina' y filtra por sucursal y categoría
    @Query("SELECT NEW com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO(art.id, art.denominacion, SUM(dp.cantidad)) " +
            "FROM DetallePedido dp JOIN dp.articulo art " +
            "JOIN dp.pedido ped " +
            "WHERE TYPE(art) = com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado " +
            "AND ped.sucursal.id = :sucursalId " + // <-- FILTRO POR SUCURSAL
            "AND art.categoria.denominacion <> 'Bebidas' " + // <-- EXCLUYE BEBIDAS
            "AND ped.estadoActivo = true AND ped.estado = :estado " +
            "AND (:fechaDesde IS NULL OR ped.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR ped.fechaPedido <= :fechaHasta) " +
            "GROUP BY art.id, art.denominacion " +
            "ORDER BY SUM(dp.cantidad) DESC, art.denominacion ASC")
    List<ArticuloManufacturadoRankingDTO> findRankingProductosCocina(
            @Param("sucursalId") Integer sucursalId,
            @Param("estado") Estado estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );
    
    // MODIFICADO: Ahora se llama 'findRankingBebidas' y filtra por sucursal y categoría 'Bebidas'
     @Query("SELECT NEW com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRankingDTO(ai.id, ai.denominacion, SUM(dp.cantidad)) " +
            "FROM DetallePedido dp JOIN dp.articulo ai " +
            "JOIN dp.pedido ped " +
            "WHERE ped.sucursal.id = :sucursalId " + // <-- FILTRO POR SUCURSAL
            "AND ai.categoria.denominacion = 'Bebidas' " + // <-- SOLO BEBIDAS
            "AND ped.estadoActivo = true AND ped.estado = :estado " +
            "AND (:fechaDesde IS NULL OR ped.fechaPedido >= :fechaDesde) " +
            "AND (:fechaHasta IS NULL OR ped.fechaPedido <= :fechaHasta) " +
            "GROUP BY ai.id, ai.denominacion " +
            "ORDER BY SUM(dp.cantidad) DESC, ai.denominacion ASC")
    List<ArticuloInsumoRankingDTO> findRankingBebidas(
            @Param("sucursalId") Integer sucursalId,
            @Param("estado") Estado estado,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable
    );
    
}