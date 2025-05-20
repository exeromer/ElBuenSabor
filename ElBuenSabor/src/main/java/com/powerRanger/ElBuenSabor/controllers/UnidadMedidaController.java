package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.services.UnidadMedidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.ConstraintViolationException; // Para errores de validación

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/unidadesmedida")
public class UnidadMedidaController {

    @Autowired
    private UnidadMedidaService unidadMedidaService;

    @PostMapping
    public ResponseEntity<?> createUnidadMedida(@RequestBody UnidadMedida unidadMedida) {
        try {
            UnidadMedida nuevaUnidad = unidadMedidaService.create(unidadMedida);
            return new ResponseEntity<>(nuevaUnidad, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error de validación");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<UnidadMedida>> getAllUnidadesMedida() {
        try {
            List<UnidadMedida> unidades = unidadMedidaService.getAll();
            if (unidades.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(unidades);
        } catch (Exception e) {
            // Log error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUnidadMedidaById(@PathVariable Integer id) {
        try {
            UnidadMedida unidad = unidadMedidaService.getById(id);
            return ResponseEntity.ok(unidad);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUnidadMedida(@PathVariable Integer id, @RequestBody UnidadMedida unidadMedidaDetails) {
        try {
            UnidadMedida unidadActualizada = unidadMedidaService.update(id, unidadMedidaDetails);
            return ResponseEntity.ok(unidadActualizada);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error de validación al actualizar");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUnidadMedida(@PathVariable Integer id) {
        try {
            unidadMedidaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            // Si la excepción es por estar en uso, podría ser BAD_REQUEST o CONFLICT (409)
            // Si es "no encontrada", es NOT_FOUND
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}