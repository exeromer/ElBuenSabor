package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DomicilioResponseDTO;
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
            DomicilioResponseDTO responseDto = domicilioService.create(dto);
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDomicilio(@PathVariable Integer id, @Valid @RequestBody DomicilioRequestDTO dto) {
        try {
            DomicilioResponseDTO responseDto = domicilioService.update(id, dto);
            return ResponseEntity.ok(responseDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<DomicilioResponseDTO>> getAllDomicilios() {
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
    public ResponseEntity<?> getDomicilioById(@PathVariable Integer id) {
        try {
            DomicilioResponseDTO domicilioDto = domicilioService.getById(id);
            return ResponseEntity.ok(domicilioDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDomicilio(@PathVariable Integer id) {
        try {
            domicilioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

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
        return ResponseEntity.status(status).body(errorResponse);
    }
}
