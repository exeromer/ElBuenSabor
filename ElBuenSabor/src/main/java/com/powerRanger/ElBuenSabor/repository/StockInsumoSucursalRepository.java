package com.powerRanger.ElBuenSabor.repository;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.StockInsumoSucursal;
import com.powerRanger.ElBuenSabor.entities.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockInsumoSucursalRepository extends JpaRepository<StockInsumoSucursal, Integer> {
    Optional<StockInsumoSucursal> findByArticuloInsumoAndSucursal(ArticuloInsumo articuloInsumo, Sucursal sucursal);
    List<StockInsumoSucursal> findByArticuloInsumo(ArticuloInsumo articuloInsumo);
    List<StockInsumoSucursal> findBySucursal(Sucursal sucursal);
}