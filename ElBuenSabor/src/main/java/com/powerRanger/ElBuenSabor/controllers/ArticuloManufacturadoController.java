package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
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
            ArticuloManufacturado nuevoAM = manufacturadoService.createArticuloManufacturado(dto);
            return new ResponseEntity<>(nuevoAM, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>(); // Usar Object para flexibilidad
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value()); // El int está bien aquí si el Map es <String, Object>
            errorResponse.put("error", "Error de validación");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>(); // Usar Object para flexibilidad
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturado>> getAllArticuloManufacturados() {
        try {
            List<ArticuloManufacturado> ams = manufacturadoService.getAllArticuloManufacturados();
            if (ams.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ams);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticuloManufacturadoById(@PathVariable Integer id) {
        try {
            ArticuloManufacturado am = manufacturadoService.getArticuloManufacturadoById(id);
            return ResponseEntity.ok(am);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>(); // Usar Object
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticuloManufacturado(@PathVariable Integer id, @Valid @RequestBody ArticuloManufacturadoRequestDTO dto) {
        try {
            ArticuloManufacturado amActualizado = manufacturadoService.updateArticuloManufacturado(id, dto);
            return ResponseEntity.ok(amActualizado);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>(); // Usar Object
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación al actualizar");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>(); // Usar Object
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticuloManufacturado(@PathVariable Integer id) {
        try {
            manufacturadoService.deleteArticuloManufacturado(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>(); // Usar Object
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}