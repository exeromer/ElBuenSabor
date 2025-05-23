package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Imagen; // Ya no se devuelve la entidad
import com.powerRanger.ElBuenSabor.services.ImagenService;
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
@RequestMapping("/api/imagenes") // CRUD para metadatos de imágenes
@Validated
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    @PostMapping
    public ResponseEntity<?> createImagen(@Valid @RequestBody ImagenRequestDTO dto) {
        try {
            ImagenResponseDTO nuevaImagenDto = imagenService.createImagen(dto);
            return new ResponseEntity<>(nuevaImagenDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<ImagenResponseDTO>> getAllImagenes() {
        try {
            List<ImagenResponseDTO> imagenes = imagenService.getAllImagenes();
            if (imagenes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            // Loguear e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImagenById(@PathVariable Integer id) {
        try {
            ImagenResponseDTO imagenDto = imagenService.getImagenById(id);
            return ResponseEntity.ok(imagenDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateImagen(@PathVariable Integer id, @Valid @RequestBody ImagenRequestDTO dto) {
        try {
            ImagenResponseDTO imagenActualizadaDto = imagenService.updateImagen(id, dto);
            return ResponseEntity.ok(imagenActualizadaDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImagen(@PathVariable Integer id) {
        try {
            imagenService.deleteImagen(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    // Métodos helper para manejo de errores (copiados de otros controladores)
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
        // e.printStackTrace(); // Útil para depuración en consola del servidor
        return ResponseEntity.status(status).body(errorResponse);
    }
}