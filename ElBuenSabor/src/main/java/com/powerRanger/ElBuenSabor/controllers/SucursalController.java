package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.SucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.SucursalResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Sucursal; // Ya no se devuelve entidad
import com.powerRanger.ElBuenSabor.services.SucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sucursales")
@Validated
public class SucursalController {

    @Autowired
    private SucursalService sucursalService;

    @PostMapping
    public ResponseEntity<?> createSucursal(@Valid @RequestBody SucursalRequestDTO dto) {
        try {
            SucursalResponseDTO nuevaSucursalDto = sucursalService.create(dto); // Devuelve DTO
            return new ResponseEntity<>(nuevaSucursalDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<SucursalResponseDTO>> getAllSucursales() { // Devuelve Lista de DTOs
        try {
            List<SucursalResponseDTO> sucursales = sucursalService.getAll();
            if (sucursales.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(sucursales);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSucursalById(@PathVariable Integer id) { // Devuelve DTO o Error
        try {
            SucursalResponseDTO sucursalDto = sucursalService.getById(id);
            return ResponseEntity.ok(sucursalDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSucursal(@PathVariable Integer id, @Valid @RequestBody SucursalRequestDTO dto) {
        try {
            SucursalResponseDTO sucursalActualizadaDto = sucursalService.update(id, dto); // Devuelve DTO
            return ResponseEntity.ok(sucursalActualizadaDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteSucursal(@PathVariable Integer id) {
        try {
            sucursalService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Sucursal con ID " + id + " marcada como inactiva (borrado lógico).");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    // Métodos helper para manejo de errores
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
        e.printStackTrace();
        return ResponseEntity.status(status).body(errorResponse);
    }
}