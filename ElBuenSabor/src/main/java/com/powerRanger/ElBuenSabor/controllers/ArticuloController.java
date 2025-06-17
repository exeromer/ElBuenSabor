package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloBaseResponseDTO; // DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Articulo; // Ya no se devuelve entidad
import com.powerRanger.ElBuenSabor.services.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
// import jakarta.validation.Valid; // No se usa @Valid aquí si POST/PUT no aceptan Articulo directamente
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articulos")
@Validated
public class ArticuloController {

    @Autowired
    private ArticuloService articuloService;

    // POST y PUT para Articulo base generalmente no se exponen directamente
    // si todos los artículos son Insumo o Manufacturado.
    // Si necesitas crear un Articulo base, necesitarías un ArticuloRequestDTO y ajustar el servicio.
    // Por ahora, este controlador se enfoca en GET y DELETE de Articulo (base).

    @GetMapping
    public ResponseEntity<List<ArticuloBaseResponseDTO>> getAllArticulos() {
        try {
            List<ArticuloBaseResponseDTO> articulos = articuloService.getAllArticulos();
            if (articulos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(articulos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticuloById(@PathVariable Integer id) {
        try {
            ArticuloBaseResponseDTO articuloDto = articuloService.getArticuloById(id);
            return ResponseEntity.ok(articuloDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorDenominacion(@RequestParam String denominacion) {
        try {
            ArticuloBaseResponseDTO articuloDto = articuloService.findByDenominacion(denominacion);
            return ResponseEntity.ok(articuloDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticulo(@PathVariable Integer id) {
        try {
            articuloService.deleteArticulo(id); // Esto borrará Articulo y sus tablas JOINED
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