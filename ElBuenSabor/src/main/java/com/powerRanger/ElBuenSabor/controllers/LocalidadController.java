package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.LocalidadResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.services.LocalidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; // Para el cuerpo de error
import java.util.List;
import java.util.Map;   // Para el cuerpo de error

@RestController
@RequestMapping("/api/localidades")
public class LocalidadController {

    @Autowired
    private LocalidadService localidadService;

    @PostMapping
    public ResponseEntity<?> crearLocalidad(@RequestBody Localidad localidad) {
        // Validación básica
        if (localidad.getNombre() == null || localidad.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la localidad es obligatorio.");
        }
        if (localidad.getProvincia() == null || localidad.getProvincia().getId() == null) {
            return ResponseEntity.badRequest().body("Es obligatorio especificar el ID de la provincia a la que pertenece la localidad.");
        }

        try {
            Localidad nuevaLocalidad = localidadService.guardar(localidad);
            // Para consistencia, devolver DTO
            LocalidadResponseDTO responseDto = localidadService.obtenerPorId(nuevaLocalidad.getId());
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<LocalidadResponseDTO>> obtenerTodasLasLocalidades() { // Devuelve lista de DTOs
        try {
            List<LocalidadResponseDTO> localidades = localidadService.obtenerTodas();
            if (localidades.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(localidades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerLocalidadPorId(@PathVariable Integer id) { // Devuelve DTO
        try {
            LocalidadResponseDTO localidadDto = localidadService.obtenerPorId(id);
            return ResponseEntity.ok(localidadDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarLocalidad(@PathVariable Integer id, @RequestBody Localidad localidadDetalles) {
        if (localidadDetalles.getNombre() == null || localidadDetalles.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la localidad es obligatorio para actualizar.");
        }
        try {
            Localidad localidadActualizada = localidadService.actualizar(id, localidadDetalles);
            // Para consistencia, devolver DTO
            LocalidadResponseDTO responseDto = localidadService.obtenerPorId(localidadActualizada.getId());
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
    public ResponseEntity<?> borrarLocalidad(@PathVariable Integer id) {
        try {
            localidadService.borrar(id);
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