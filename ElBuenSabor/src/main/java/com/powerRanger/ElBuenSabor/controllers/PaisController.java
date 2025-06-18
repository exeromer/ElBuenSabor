package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.PaisResponseDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.services.PaisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; // Para el cuerpo de error
import java.util.List;
import java.util.Map;   // Para el cuerpo de error

@RestController
@RequestMapping("/api/paises")
public class PaisController {

    @Autowired
    private PaisService paisService;

    @PostMapping
    public ResponseEntity<?> crearPais(@RequestBody Pais pais) { // Podría aceptar un PaisRequestDTO
        if (pais.getNombre() == null || pais.getNombre().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "El nombre del país no puede estar vacío.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            Pais nuevoPais = paisService.guardar(pais);
            // Para consistencia, podrías convertir a DTO antes de devolver
            // PaisResponseDTO responseDto = paisService.obtenerPorId(nuevoPais.getId());
            // return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
            return new ResponseEntity<>(nuevoPais, HttpStatus.CREATED); // Devuelve entidad por ahora
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al crear país: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<PaisResponseDTO>> obtenerTodosLosPaises() { // Devuelve Lista de DTOs
        try {
            List<PaisResponseDTO> paises = paisService.obtenerTodos();
            if (paises.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(paises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPaisPorId(@PathVariable Integer id) { // Devuelve DTO o Error
        try {
            PaisResponseDTO paisDto = paisService.obtenerPorId(id);
            return ResponseEntity.ok(paisDto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPais(@PathVariable Integer id, @RequestBody Pais paisDetalles) { // Podría aceptar DTO
        if (paisDetalles.getNombre() == null || paisDetalles.getNombre().trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "El nombre del país no puede estar vacío.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            Pais paisActualizado = paisService.actualizar(id, paisDetalles);
            // Para consistencia, podrías convertir a DTO antes de devolver
            // PaisResponseDTO responseDto = paisService.obtenerPorId(paisActualizado.getId());
            // return ResponseEntity.ok(responseDto);
            return ResponseEntity.ok(paisActualizado); // Devuelve entidad por ahora
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrarPais(@PathVariable Integer id) {
        try {
            paisService.borrar(id); // El servicio ahora devuelve boolean pero no lo usamos aquí, o void
            return ResponseEntity.noContent().build(); // Estándar para DELETE exitoso
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            // Si la excepción es por tener provincias asociadas, es un BAD_REQUEST (o CONFLICT 409)
            // Si es "no encontrado", es NOT_FOUND
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}