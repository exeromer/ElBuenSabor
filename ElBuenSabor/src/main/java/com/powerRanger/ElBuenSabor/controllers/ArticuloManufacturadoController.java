package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO; // DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado; // Ya no se devuelve entidad
import com.powerRanger.ElBuenSabor.services.ArticuloManufacturadoService;
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
@RequestMapping("/api/articulosmanufacturados")
@Validated
public class ArticuloManufacturadoController {

    @Autowired
    private ArticuloManufacturadoService manufacturadoService;

    @PostMapping
    public ResponseEntity<?> createArticuloManufacturado(@Valid @RequestBody ArticuloManufacturadoRequestDTO dto) {
        try {
            ArticuloManufacturadoResponseDTO nuevoAMDto = manufacturadoService.createArticuloManufacturado(dto);
            return new ResponseEntity<>(nuevoAMDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getAllArticuloManufacturados(
            @RequestParam(name = "denominacion", required = false) String searchTerm,
            @RequestParam(name = "estado", required = false) Boolean estadoActivo
    ) {
        try {
            List<ArticuloManufacturadoResponseDTO> ams = manufacturadoService.getAllArticuloManufacturados(searchTerm, estadoActivo);
            if (ams.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ams);
        } catch (Exception e) {
            System.err.println("Error en ArticuloManufacturadoController - getAll: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticuloManufacturadoById(@PathVariable Integer id) {
        try {
            ArticuloManufacturadoResponseDTO amDto = manufacturadoService.getArticuloManufacturadoById(id);
            return ResponseEntity.ok(amDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticuloManufacturado(@PathVariable Integer id, @Valid @RequestBody ArticuloManufacturadoRequestDTO dto) {
        try {
            ArticuloManufacturadoResponseDTO amActualizadoDto = manufacturadoService.updateArticuloManufacturado(id, dto);
            return ResponseEntity.ok(amActualizadoDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticuloManufacturado(@PathVariable Integer id) {
        try {
            manufacturadoService.deleteArticuloManufacturado(id);
            return ResponseEntity.noContent().build();
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