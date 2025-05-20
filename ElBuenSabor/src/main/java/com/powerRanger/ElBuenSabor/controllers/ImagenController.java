package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Imagen;
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
@RequestMapping("/api/imagenes")
@Validated
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    @PostMapping
    public ResponseEntity<?> createImagen(@Valid @RequestBody ImagenRequestDTO dto) {
        try {
            Imagen nuevaImagen = imagenService.createImagen(dto);
            return new ResponseEntity<>(nuevaImagen, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<Imagen>> getAllImagenes() {
        try {
            List<Imagen> imagenes = imagenService.getAllImagenes();
            if (imagenes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImagenById(@PathVariable Integer id) {
        try {
            Imagen imagen = imagenService.getImagenById(id);
            return ResponseEntity.ok(imagen);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateImagen(@PathVariable Integer id, @Valid @RequestBody ImagenRequestDTO dto) {
        try {
            Imagen imagenActualizada = imagenService.updateImagen(id, dto);
            return ResponseEntity.ok(imagenActualizada);
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
    public ResponseEntity<?> deleteImagen(@PathVariable Integer id) {
        try {
            imagenService.deleteImagen(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}