package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ProvinciaResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.services.ProvinciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; // Para el cuerpo de error
import java.util.List;
import java.util.Map;   // Para el cuerpo de error

@RestController
@RequestMapping("/api/provincias")
public class ProvinciaController {

    @Autowired
    private ProvinciaService provinciaService;

    @PostMapping
    public ResponseEntity<?> crearProvincia(@RequestBody Provincia provincia) {
        // Se mantiene el request con la entidad por ahora, podríamos crear ProvinciaRequestDTO
        if (provincia.getNombre() == null || provincia.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la provincia es obligatorio.");
        }
        if (provincia.getPais() == null || provincia.getPais().getId() == null) {
            return ResponseEntity.badRequest().body("Es obligatorio especificar el ID del país al que pertenece la provincia.");
        }

        try {
            Provincia nuevaProvincia = provinciaService.guardar(provincia);
            // Para consistencia, podríamos devolver el DTO
            ProvinciaResponseDTO responseDto = provinciaService.obtenerPorId(nuevaProvincia.getId());
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProvinciaResponseDTO>> obtenerTodasLasProvincias() { // Devuelve lista de DTOs
        try {
            List<ProvinciaResponseDTO> provincias = provinciaService.obtenerTodas();
            if (provincias.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(provincias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProvinciaPorId(@PathVariable Integer id) { // Devuelve DTO
        try {
            ProvinciaResponseDTO provinciaDto = provinciaService.obtenerPorId(id);
            return ResponseEntity.ok(provinciaDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProvincia(@PathVariable Integer id, @RequestBody Provincia provinciaDetalles) {
        // Se mantiene el request con la entidad por ahora
        if (provinciaDetalles.getNombre() == null || provinciaDetalles.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la provincia es obligatorio para actualizar.");
        }
        try {
            Provincia provinciaActualizada = provinciaService.actualizar(id, provinciaDetalles);
            // Para consistencia, podríamos devolver el DTO
            ProvinciaResponseDTO responseDto = provinciaService.obtenerPorId(provinciaActualizada.getId());
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrarProvincia(@PathVariable Integer id) {
        try {
            provinciaService.borrar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}