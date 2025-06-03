package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO; // DTO de respuesta
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO;
import com.powerRanger.ElBuenSabor.services.ArticuloInsumoService;
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
@RequestMapping("/api/articulosinsumo")
@Validated
public class ArticuloInsumoController {

    @Autowired
    private ArticuloInsumoService articuloInsumoService;

    @PostMapping
    public ResponseEntity<?> createArticuloInsumo(@Valid @RequestBody ArticuloInsumoRequestDTO dto) {
        try {
            ArticuloInsumoResponseDTO nuevoInsumoDto = articuloInsumoService.createArticuloInsumo(dto);
            return new ResponseEntity<>(nuevoInsumoDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getAllArticuloInsumo(
            @RequestParam(name = "denominacion", required = false) String searchTerm,
            @RequestParam(name = "estado", required = false) Boolean estadoActivo // 'true', 'false', o ausente para todos
    ) {
        try {
            // Pasa el término de búsqueda y el estado al servicio
            List<ArticuloInsumoResponseDTO> insumos = articuloInsumoService.getAllArticuloInsumo(searchTerm, estadoActivo);
            if (insumos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(insumos);
        } catch (Exception e) {
            System.err.println("Error en ArticuloInsumoController - getAllArticuloInsumo: " + e.getMessage());
            e.printStackTrace(); // Para más detalle del error en el backend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticuloInsumoById(@PathVariable Integer id) {
        try {
            ArticuloInsumoResponseDTO insumoDto = articuloInsumoService.getArticuloInsumoById(id);
            return ResponseEntity.ok(insumoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticuloInsumo(@PathVariable Integer id, @Valid @RequestBody ArticuloInsumoRequestDTO dto) { // CAMBIO AQUÍ
        try {
            ArticuloInsumoResponseDTO insumoActualizadoDto = articuloInsumoService.updateArticuloInsumo(id, dto); // CAMBIO AQUÍ
            return ResponseEntity.ok(insumoActualizadoDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticuloInsumo(@PathVariable Integer id) {
        try {
            articuloInsumoService.deleteArticuloInsumo(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    // Métodos helper para manejo de errores (puedes moverlos a una clase base o @ControllerAdvice)
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