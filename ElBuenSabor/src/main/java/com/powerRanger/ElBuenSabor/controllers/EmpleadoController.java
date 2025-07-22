package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.EmpleadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpleadoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
import com.powerRanger.ElBuenSabor.services.EmpleadoService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/empleados")
@Validated
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    @PostMapping
    public ResponseEntity<?> createEmpleado(@Valid @RequestBody EmpleadoRequestDTO dto) {
        try {
            EmpleadoResponseDTO nuevoEmpleadoDto = empleadoService.create(dto);
            return new ResponseEntity<>(nuevoEmpleadoDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponseDTO>> getAllEmpleados(
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            @RequestParam(name = "rol", required = false) RolEmpleado rol
    ) {
        try {
            List<EmpleadoResponseDTO> empleados = empleadoService.getAll(searchTerm, rol);
            if (empleados.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(empleados);
        } catch (Exception e) {
            System.err.println("Error en EmpleadoController - getAllEmpleados: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmpleadoById(@PathVariable Integer id) {
        try {
            EmpleadoResponseDTO empleadoDto = empleadoService.getById(id);
            return ResponseEntity.ok(empleadoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> getEmpleadoByUsuarioId(@PathVariable Integer usuarioId) {
        try {
            EmpleadoResponseDTO empleadoDto = empleadoService.getByUsuarioId(usuarioId);
            return ResponseEntity.ok(empleadoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/perfil")
    public ResponseEntity<?> getMiPerfil(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            EmpleadoResponseDTO empleadoDto = empleadoService.findOrCreateEmpleadoPorAuth0Id(auth0Id);
            return ResponseEntity.ok(empleadoDto);
        } catch (Exception e) {
            // Un manejador de errores genérico. Puedes adaptarlo.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/perfil")
    public ResponseEntity<?> updateMiPerfil(Authentication authentication, @Valid @RequestBody EmpleadoRequestDTO dto) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();

            // El servicio se encargará de encontrar al empleado por auth0Id y actualizarlo
            EmpleadoResponseDTO empleadoActualizadoDto = empleadoService.updateMiPerfil(auth0Id, dto);

            return ResponseEntity.ok(empleadoActualizadoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmpleado(@PathVariable Integer id, @Valid @RequestBody EmpleadoRequestDTO dto) {
        try {
            EmpleadoResponseDTO empleadoActualizadoDto = empleadoService.update(id, dto);
            return ResponseEntity.ok(empleadoActualizadoDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteEmpleado(@PathVariable Integer id) {
        try {
            empleadoService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Empleado con ID " + id + " marcado como inactivo (borrado lógico).");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
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
        e.printStackTrace();
        return ResponseEntity.status(status).body(errorResponse);
    }

}