package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalResponseDTO;
import com.powerRanger.ElBuenSabor.services.StockInsumoSucursalService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stockinsumosucursal")
@Validated
public class StockInsumoSucursalController {

    @Autowired
    private StockInsumoSucursalService stockInsumoSucursalService;

    // --- MÉTODOS HELPER (sin cambios) ---
    private ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de validación");
        errorResponse.put("mensajes", e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<Map<String, Object>> handleGenericException(Exception e, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }

    // --- ENDPOINTS CORREGIDOS ---

    @GetMapping
    public ResponseEntity<?> getAllStockInsumoSucursal() { // <--- TIPO CORREGIDO
        try {
            List<StockInsumoSucursalResponseDTO> stocks = stockInsumoSucursalService.getAllStockInsumoSucursal();
            if (stocks.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(stocks);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStockInsumoSucursalById(@PathVariable Integer id) { // <--- TIPO CORREGIDO
        try {
            StockInsumoSucursalResponseDTO stockDto = stockInsumoSucursalService.getStockInsumoSucursalById(id);
            return ResponseEntity.ok(stockDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/insumo/{insumoId}/sucursal/{sucursalId}")
    public ResponseEntity<?> getStockByInsumoAndSucursal( // <--- TIPO CORREGIDO
                                                          @PathVariable Integer insumoId,
                                                          @PathVariable Integer sucursalId) {
        try {
            StockInsumoSucursalResponseDTO stockDto = stockInsumoSucursalService.getStockByInsumoAndSucursal(insumoId, sucursalId);
            return ResponseEntity.ok(stockDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> createStockInsumoSucursal(@Valid @RequestBody StockInsumoSucursalRequestDTO dto) { // <--- TIPO CORREGIDO
        try {
            StockInsumoSucursalResponseDTO createdStock = stockInsumoSucursalService.createStockInsumoSucursal(dto);
            return new ResponseEntity<>(createdStock, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("ya existe") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStockInsumoSucursal(@PathVariable Integer id, @Valid @RequestBody StockInsumoSucursalRequestDTO dto) { // <--- TIPO CORREGIDO
        try {
            StockInsumoSucursalResponseDTO updatedStock = stockInsumoSucursalService.updateStockInsumoSucursal(id, dto);
            return ResponseEntity.ok(updatedStock);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStockInsumoSucursal(@PathVariable Integer id) { // <--- TIPO CORREGIDO
        try {
            stockInsumoSucursalService.deleteStockInsumoSucursal(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/reduceStock/insumo/{insumoId}/sucursal/{sucursalId}/cantidad/{cantidad}")
    public ResponseEntity<?> reduceStock( // <--- TIPO CORREGIDO
                                          @PathVariable Integer insumoId,
                                          @PathVariable Integer sucursalId,
                                          @PathVariable Double cantidad) {
        try {
            stockInsumoSucursalService.reduceStock(insumoId, sucursalId, cantidad);
            StockInsumoSucursalResponseDTO updatedStock = stockInsumoSucursalService.getStockByInsumoAndSucursal(insumoId, sucursalId);
            return ResponseEntity.ok(updatedStock);
        } catch (Exception e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("no encontrado")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("Stock insuficiente")) {
                status = HttpStatus.CONFLICT;
            }
            return handleGenericException(e, status);
        }
    }

    @PutMapping("/addStock/insumo/{insumoId}/sucursal/{sucursalId}/cantidad/{cantidad}")
    public ResponseEntity<?> addStock( // <--- TIPO CORREGIDO
                                       @PathVariable Integer insumoId,
                                       @PathVariable Integer sucursalId,
                                       @PathVariable Double cantidad) {
        try {
            stockInsumoSucursalService.addStock(insumoId, sucursalId, cantidad);
            StockInsumoSucursalResponseDTO updatedStock = stockInsumoSucursalService.getStockByInsumoAndSucursal(insumoId, sucursalId);
            return ResponseEntity.ok(updatedStock);
        } catch (Exception e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("no encontrado")) {
                status = HttpStatus.NOT_FOUND;
            }
            return handleGenericException(e, status);
        }
    }
}