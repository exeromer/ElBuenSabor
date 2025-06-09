package com.powerRanger.ElBuenSabor.controllers;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Este método captura las excepciones de validación (JSR 303)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e, WebRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de validación");
        errorResponse.put("mensajes", e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Este método captura todas las demás excepciones genéricas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e, WebRequest request) {
        // Determinar el status basado en el mensaje de error, como ya hacías
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (e.getMessage() != null) {
            if (e.getMessage().toLowerCase().contains("no encontrado")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().toLowerCase().contains("stock insuficiente") || e.getMessage().toLowerCase().contains("ya está en uso")) {
                status = HttpStatus.CONFLICT; // 409 Conflict es más apropiado para estos casos
            } else {
                status = HttpStatus.BAD_REQUEST; // Default para otros errores de negocio
            }
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", e.getMessage());

        System.err.println("Exception handled: " + e.getMessage());
        e.printStackTrace(); // Mantener para debugging en el servidor

        return ResponseEntity.status(status).body(errorResponse);
    }
}