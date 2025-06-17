package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PromocionResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Promocion; // Ya no se devuelve entidad
import com.powerRanger.ElBuenSabor.services.PromocionService;
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
@RequestMapping("/api/promociones")
@Validated
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    @PostMapping
    public ResponseEntity<?> createPromocion(@Valid @RequestBody PromocionRequestDTO dto) {
        try {
            PromocionResponseDTO nuevaPromocionDto = promocionService.create(dto); // Devuelve DTO
            return new ResponseEntity<>(nuevaPromocionDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<PromocionResponseDTO>> getAllPromociones() { // Devuelve Lista de DTOs
        try {
            List<PromocionResponseDTO> promociones = promocionService.getAll();
            if (promociones.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(promociones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPromocionById(@PathVariable Integer id) { // Devuelve DTO o Error
        try {
            PromocionResponseDTO promocionDto = promocionService.getById(id);
            return ResponseEntity.ok(promocionDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromocion(@PathVariable Integer id, @Valid @RequestBody PromocionRequestDTO dto) {
        try {
            PromocionResponseDTO promocionActualizadaDto = promocionService.update(id, dto); // Devuelve DTO
            return ResponseEntity.ok(promocionActualizadaDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeletePromocion(@PathVariable Integer id) {
        try {
            promocionService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Promoción con ID " + id + " marcada como inactiva (borrado lógico).");
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