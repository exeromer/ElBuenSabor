package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalResponseDTO;
import com.powerRanger.ElBuenSabor.services.StockInsumoSucursalService;
// Importaciones de excepción ya no son necesarias si se manejan globalmente
// import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List; // Ya no necesitamos Map para errores, solo para el ControllerAdvice


@RestController
@RequestMapping("/api/stockinsumosucursal")
@Validated // Habilita la validación para los parámetros de los métodos
public class StockInsumoSucursalController {

    @Autowired
    private StockInsumoSucursalService stockInsumoSucursalService;

    // Métodos helper handleConstraintViolation y handleGenericException se ELIMINAN de aquí,
    // ya que serán manejados por el @ControllerAdvice

    /**
     * Obtiene todos los registros de stock de insumos por sucursal.
     * @return ResponseEntity con la lista de StockInsumoSucursalResponseDTO.
     */
    @GetMapping
    public ResponseEntity<List<StockInsumoSucursalResponseDTO>> getAllStockInsumoSucursal() throws Exception {
        // El try-catch se simplifica o se elimina si el ControllerAdvice lo maneja todo
        List<StockInsumoSucursalResponseDTO> stocks = stockInsumoSucursalService.getAllStockInsumoSucursal();
        if (stocks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(stocks);
    }

    /**
     * Obtiene un registro de stock de insumo por sucursal por su ID.
     * @param id ID del registro de stock.
     * @return ResponseEntity con el StockInsumoSucursalResponseDTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StockInsumoSucursalResponseDTO> getStockInsumoSucursalById(@PathVariable Integer id) throws Exception {
        StockInsumoSucursalResponseDTO stockDto = stockInsumoSucursalService.getStockInsumoSucursalById(id);
        return ResponseEntity.ok(stockDto);
    }

    /**
     * Obtiene el stock de un insumo específico en una sucursal específica.
     * @param insumoId ID del ArticuloInsumo.
     * @param sucursalId ID de la Sucursal.
     * @return ResponseEntity con el StockInsumoSucursalResponseDTO.
     */
    @GetMapping("/insumo/{insumoId}/sucursal/{sucursalId}")
    public ResponseEntity<StockInsumoSucursalResponseDTO> getStockByInsumoAndSucursal(
            @PathVariable Integer insumoId,
            @PathVariable Integer sucursalId) throws Exception {
        StockInsumoSucursalResponseDTO stockDto = stockInsumoSucursalService.getStockByInsumoAndSucursal(insumoId, sucursalId);
        return ResponseEntity.ok(stockDto);
    }

    /**
     * Crea un nuevo registro de stock de insumo por sucursal.
     * @param dto DTO con los datos del stock.
     * @return ResponseEntity con el StockInsumoSucursalResponseDTO creado.
     */
    @PostMapping
    public ResponseEntity<StockInsumoSucursalResponseDTO> createStockInsumoSucursal(@Valid @RequestBody StockInsumoSucursalRequestDTO dto) throws Exception {
        StockInsumoSucursalResponseDTO createdStock = stockInsumoSucursalService.createStockInsumoSucursal(dto);
        return new ResponseEntity<>(createdStock, HttpStatus.CREATED);
    }

    /**
     * Actualiza un registro de stock de insumo por sucursal existente.
     * @param id ID del registro de stock a actualizar.
     * @param dto DTO con los datos actualizados.
     * @return ResponseEntity con el StockInsumoSucursalResponseDTO actualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StockInsumoSucursalResponseDTO> updateStockInsumoSucursal(@PathVariable Integer id, @Valid @RequestBody StockInsumoSucursalRequestDTO dto) throws Exception {
        StockInsumoSucursalResponseDTO updatedStock = stockInsumoSucursalService.updateStockInsumoSucursal(id, dto);
        return ResponseEntity.ok(updatedStock);
    }

    /**
     * Elimina un registro de stock de insumo por sucursal por su ID.
     * @param id ID del registro de stock a eliminar.
     * @return ResponseEntity sin contenido si la eliminación fue exitosa.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStockInsumoSucursal(@PathVariable Integer id) throws Exception {
        stockInsumoSucursalService.deleteStockInsumoSucursal(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reduce el stock actual de un insumo en una sucursal específica.
     * Ideal para operaciones de salida de stock.
     * @param insumoId ID del ArticuloInsumo.
     * @param sucursalId ID de la Sucursal.
     * @param cantidad Cantidad a reducir.
     * @return ResponseEntity con el StockInsumoSucursalResponseDTO actualizado.
     */
    @PutMapping("/reduceStock/insumo/{insumoId}/sucursal/{sucursalId}/cantidad/{cantidad}")
    public ResponseEntity<StockInsumoSucursalResponseDTO> reduceStock(
            @PathVariable Integer insumoId,
            @PathVariable Integer sucursalId,
            @PathVariable Double cantidad) throws Exception {
        stockInsumoSucursalService.reduceStock(insumoId, sucursalId, cantidad);
        StockInsumoSucursalResponseDTO updatedStock = stockInsumoSucursalService.getStockByInsumoAndSucursal(insumoId, sucursalId);
        return ResponseEntity.ok(updatedStock);
    }

    /**
     * Añade stock actual a un insumo en una sucursal específica.
     * Ideal para operaciones de entrada de stock.
     * @param insumoId ID del ArticuloInsumo.
     * @param sucursalId ID de la Sucursal.
     * @param cantidad Cantidad a añadir.
     * @return ResponseEntity con el StockInsumoSucursalResponseDTO actualizado.
     */
    @PutMapping("/addStock/insumo/{insumoId}/sucursal/{sucursalId}/cantidad/{cantidad}")
    public ResponseEntity<StockInsumoSucursalResponseDTO> addStock(
            @PathVariable Integer insumoId,
            @PathVariable Integer sucursalId,
            @PathVariable Double cantidad) throws Exception {
        stockInsumoSucursalService.addStock(insumoId, sucursalId, cantidad);
        StockInsumoSucursalResponseDTO updatedStock = stockInsumoSucursalService.getStockByInsumoAndSucursal(insumoId, sucursalId);
        return ResponseEntity.ok(updatedStock);
    }
}