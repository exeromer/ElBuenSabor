package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface StockInsumoSucursalService {
    List<StockInsumoSucursalResponseDTO> getAllStockInsumoSucursal();
    StockInsumoSucursalResponseDTO getStockInsumoSucursalById(Integer id) throws Exception;
    StockInsumoSucursalResponseDTO createStockInsumoSucursal(@Valid StockInsumoSucursalRequestDTO dto) throws Exception;
    StockInsumoSucursalResponseDTO updateStockInsumoSucursal(Integer id, @Valid StockInsumoSucursalRequestDTO dto) throws Exception;
    void deleteStockInsumoSucursal(Integer id) throws Exception;
    StockInsumoSucursalResponseDTO getStockByInsumoAndSucursal(Integer insumoId, Integer sucursalId) throws Exception;
    void reduceStock(Integer insumoId, Integer sucursalId, Double cantidad) throws Exception;
    void addStock(Integer insumoId, Integer sucursalId, Double cantidad) throws Exception;

}