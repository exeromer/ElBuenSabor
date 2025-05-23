package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO; // Importar DTO de respuesta
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.services.DomicilioService;
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
@RequestMapping("/api/domicilios")
@Validated
public class DomicilioController {

    @Autowired
    private DomicilioService domicilioService;

    @PostMapping
    public ResponseEntity<?> createDomicilio(@Valid @RequestBody DomicilioRequestDTO dto) {
        try {
            Domicilio nuevoDomicilio = domicilioService.create(dto);
            // Mapear la entidad guardada a DomicilioResponseDTO para la respuesta
            DomicilioResponseDTO responseDto = domicilioService.getById(nuevoDomicilio.getId());
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            // ... (manejo de ConstraintViolationException como lo tenías)
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
    public ResponseEntity<List<DomicilioResponseDTO>> getAllDomicilios() { // Devuelve Lista de DTOs
        try {
            List<DomicilioResponseDTO> domicilios = domicilioService.getAll();
            if (domicilios.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(domicilios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDomicilioById(@PathVariable Integer id) { // Devuelve DTO o Error
        try {
            DomicilioResponseDTO domicilioDto = domicilioService.getById(id);
            return ResponseEntity.ok(domicilioDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDomicilio(@PathVariable Integer id, @Valid @RequestBody DomicilioRequestDTO dto) {
        try {
            Domicilio domicilioActualizado = domicilioService.update(id, dto);
            // Mapear la entidad actualizada a DomicilioResponseDTO para la respuesta
            DomicilioResponseDTO responseDto = domicilioService.getById(domicilioActualizado.getId());
            return ResponseEntity.ok(responseDto);
        } catch (ConstraintViolationException e) {
            // ... (manejo de ConstraintViolationException como lo tenías) ...
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación al actualizar");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDomicilio(@PathVariable Integer id) {
        try {
            domicilioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}