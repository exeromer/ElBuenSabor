package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Usuario; // Ya no se devuelve la entidad directamente
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> createUsuario(@Valid @RequestBody UsuarioRequestDTO dto) {
        try {
            UsuarioResponseDTO nuevoUsuarioDto = usuarioService.create(dto); // Devuelve DTO
            return new ResponseEntity<>(nuevoUsuarioDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            // ... manejo de ConstraintViolationException (sin cambios)
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
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios() { // Devuelve Lista de DTOs
        try {
            List<UsuarioResponseDTO> usuarios = usuarioService.getAll();
            if (usuarios.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Integer id) { // Devuelve DTO o Error
        try {
            UsuarioResponseDTO usuarioDto = usuarioService.getById(id);
            return ResponseEntity.ok(usuarioDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUsuarioByUsername(@PathVariable String username) { // Devuelve DTO o Error
        try {
            UsuarioResponseDTO usuarioDto = usuarioService.getByUsername(username);
            return ResponseEntity.ok(usuarioDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/auth0/{auth0Id}")
    public ResponseEntity<?> getUsuarioByAuth0Id(@PathVariable String auth0Id) {
        try {
            // Extraer username y email del token en el frontend y pasarlos como headers/params
            // es más robusto, pero para simplificar, llamaremos a findOrCreateUsuario
            // y dejaremos que el servicio maneje los nulls para username/email si es solo para buscar/crear con defaults.
            // El servicio findOrCreateUsuario ya tiene lógica para generar un username si es null.
            Usuario usuario = usuarioService.findOrCreateUsuario(auth0Id, null, null); // Pasamos null para username y email
            UsuarioResponseDTO usuarioDto = convertUsuarioToResponseDto(usuario); // Convierte la entidad a DTO
            return ResponseEntity.ok(usuarioDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value()); // Podría ser un error diferente a NOT_FOUND ahora
            errorResponse.put("error", "Error al obtener o crear usuario: " + e.getMessage());
            e.printStackTrace(); // Importante para ver el error real en la consola del backend
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Integer id, @Valid @RequestBody UsuarioRequestDTO dto) {
        try {
            UsuarioResponseDTO usuarioActualizadoDto = usuarioService.update(id, dto); // Devuelve DTO
            return ResponseEntity.ok(usuarioActualizadoDto);
        } catch (ConstraintViolationException e) {
            // ... manejo de ConstraintViolationException (sin cambios) ...
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
    public ResponseEntity<?> softDeleteUsuario(@PathVariable Integer id) {
        try {
            usuarioService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario con ID " + id + " marcado como inactivo (borrado lógico).");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    private UsuarioResponseDTO convertUsuarioToResponseDto(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setEstadoActivo(usuario.getEstadoActivo());
        dto.setFechaBaja(usuario.getFechaBaja());
        return dto;
    }


}