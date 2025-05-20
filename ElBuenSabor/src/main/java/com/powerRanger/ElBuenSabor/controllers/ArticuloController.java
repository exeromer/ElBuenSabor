package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.services.ArticuloService;
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
@RequestMapping("/api/articulos") // Ruta base con /api
public class ArticuloController {

    @Autowired
    private ArticuloService articuloService;

    @PostMapping
    public ResponseEntity<?> createArticulo(@RequestBody Articulo articulo) {
        try {
            Articulo nuevoArticulo = articuloService.createArticulo(articulo);
            return new ResponseEntity<>(nuevoArticulo, HttpStatus.CREATED);
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
    public ResponseEntity<List<Articulo>> getAllArticulos() {
        try {
            List<Articulo> articulos = articuloService.getAllArticulos();
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
            Articulo articulo = articuloService.getArticuloById(id);
            return ResponseEntity.ok(articulo);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorDenominacion(@RequestParam String denominacion) {
        try {
            Articulo articulo = articuloService.findByDenominacion(denominacion);
            return ResponseEntity.ok(articulo);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticulo(@PathVariable Integer id, @RequestBody Articulo articuloDetalles) {
        try {
            Articulo articuloActualizado = articuloService.updateArticulo(id, articuloDetalles);
            return ResponseEntity.ok(articuloActualizado);
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
    public ResponseEntity<?> deleteArticulo(@PathVariable Integer id) {
        try {
            articuloService.deleteArticulo(id);
            return ResponseEntity.noContent().build(); // 204 No Content es estándar para DELETE exitoso
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}