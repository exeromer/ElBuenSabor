package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.UnidadMedidaResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.services.UnidadMedidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated; // Para @Valid en parámetros de DTO
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Para anotar el @RequestBody
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/unidadesmedida")
@Validated // Habilita la validación para los métodos de este controlador
public class UnidadMedidaController {

    @Autowired
    private UnidadMedidaService unidadMedidaService;

    // Nota: Los métodos POST y PUT actualmente aceptan la entidad UnidadMedida directamente.
    // Podrías crear un UnidadMedidaRequestDTO si necesitas más control sobre la entrada.
    @PostMapping
    public ResponseEntity<?> createUnidadMedida(@Valid @RequestBody UnidadMedida unidadMedida) {
        try {
            UnidadMedida nuevaUnidad = unidadMedidaService.create(unidadMedida);
            // Para consistencia, podríamos devolver el DTO aquí también
            // UnidadMedidaResponseDTO responseDto = unidadMedidaService.getById(nuevaUnidad.getId()); // Asumiendo que getById ahora devuelve DTO
            // return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
            return new ResponseEntity<>(nuevaUnidad, HttpStatus.CREATED); // Por ahora, devuelve entidad
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>(); // Cambiado a Object
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<UnidadMedidaResponseDTO>> getAllUnidadesMedida() { // Devuelve Lista de DTOs
        try {
            List<UnidadMedidaResponseDTO> unidades = unidadMedidaService.getAll();
            if (unidades.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(unidades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUnidadMedidaById(@PathVariable Integer id) { // Devuelve DTO o Error
        try {
            UnidadMedidaResponseDTO unidadDto = unidadMedidaService.getById(id);
            return ResponseEntity.ok(unidadDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUnidadMedida(@PathVariable Integer id, @Valid @RequestBody UnidadMedida unidadMedidaDetails) { // Podría ser DTO
        try {
            UnidadMedida unidadActualizada = unidadMedidaService.update(id, unidadMedidaDetails);
            // Para consistencia, podríamos devolver el DTO aquí también
            // UnidadMedidaResponseDTO responseDto = unidadMedidaService.getById(unidadActualizada.getId());
            // return ResponseEntity.ok(responseDto);
            return ResponseEntity.ok(unidadActualizada); // Por ahora, devuelve entidad
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación al actualizar");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUnidadMedida(@PathVariable Integer id) {
        try {
            unidadMedidaService.delete(id);
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