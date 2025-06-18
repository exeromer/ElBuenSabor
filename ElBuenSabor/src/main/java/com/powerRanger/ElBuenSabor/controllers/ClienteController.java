package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteResponseDTO; // Importar DTO de respuesta
import com.powerRanger.ElBuenSabor.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/clientes")
@Validated
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<?> createCliente(@Valid @RequestBody ClienteRequestDTO dto) {
        try {
            ClienteResponseDTO nuevoClienteDto = clienteService.createCliente(dto); // Devuelve DTO
            return new ResponseEntity<>(nuevoClienteDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> getAllClientes(
            @RequestParam(name = "searchTerm", required = false) String searchTerm
    ) {
        try {
            List<ClienteResponseDTO> clientes = clienteService.getAllClientes(searchTerm);
            if (clientes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            System.err.println("Error en ClienteController - getAllClientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> getMiPerfil(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "No autenticado o token inválido."));
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            String email = jwt.getClaimAsString("email"); // Obtener el email del JWT si está disponible

            // Esta llamada ahora intentará crear el cliente si no existe
            ClienteResponseDTO clienteDto = clienteService.findOrCreateClienteByAuth0Id(auth0Id, email);
            return ResponseEntity.ok(clienteDto);
        } catch (Exception e) {
            return handleGenericException(e, e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClienteById(@PathVariable Integer id) {
        try {
            ClienteResponseDTO clienteDto = clienteService.getClienteById(id);
            return ResponseEntity.ok(clienteDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCliente(@PathVariable Integer id, @Valid @RequestBody ClienteRequestDTO dto) {
        try {
            ClienteResponseDTO clienteActualizadoDto = clienteService.updateCliente(id, dto);
            return ResponseEntity.ok(clienteActualizadoDto);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteCliente(@PathVariable Integer id) {
        try {
            clienteService.softDeleteCliente(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Cliente con ID " + id + " marcado como inactivo (borrado lógico).");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    // Métodos helper para manejo de errores (ya los tenías)
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
        e.printStackTrace(); // Para depuración en consola del servidor
        return ResponseEntity.status(status).body(errorResponse);
    }
}