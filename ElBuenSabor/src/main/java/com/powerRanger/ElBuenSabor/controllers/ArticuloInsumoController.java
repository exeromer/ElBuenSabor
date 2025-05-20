package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.services.ArticuloInsumoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articulosinsumo") // Ruta con /api
public class ArticuloInsumoController {

    @Autowired
    private ArticuloInsumoService articuloInsumoService;

    @PostMapping
    public ResponseEntity<?> createArticuloInsumo(@RequestBody ArticuloInsumo articuloInsumo) {
        try {
            ArticuloInsumo nuevoInsumo = articuloInsumoService.createArticuloInsumo(articuloInsumo);
            return new ResponseEntity<>(nuevoInsumo, HttpStatus.CREATED);
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
    public ResponseEntity<List<ArticuloInsumo>> getAllArticuloInsumo() {
        try {
            List<ArticuloInsumo> insumos = articuloInsumoService.getAllArticuloInsumo();
            if (insumos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(insumos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticuloInsumoById(@PathVariable Integer id) {
        try {
            ArticuloInsumo insumo = articuloInsumoService.getArticuloInsumoById(id);
            return ResponseEntity.ok(insumo);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticuloInsumo(@PathVariable Integer id, @RequestBody ArticuloInsumo articuloInsumoDetails) {
        try {
            ArticuloInsumo insumoActualizado = articuloInsumoService.updateArticuloInsumo(id, articuloInsumoDetails);
            return ResponseEntity.ok(insumoActualizado);
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
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticuloInsumo(@PathVariable Integer id) {
        try {
            articuloInsumoService.deleteArticuloInsumo(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}