package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Localidad;
import com.powerRanger.ElBuenSabor.services.LocalidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localidades")
public class LocalidadController {

    @Autowired
    private LocalidadService localidadService;

    @PostMapping
    public ResponseEntity<?> crearLocalidad(@RequestBody Localidad localidad) {
        // Validación básica
        if (localidad.getNombre() == null || localidad.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la localidad es obligatorio.");
        }
        if (localidad.getProvincia() == null || localidad.getProvincia().getId() == null) {
            return ResponseEntity.badRequest().body("Es obligatorio especificar el ID de la provincia a la que pertenece la localidad.");
        }

        try {
            Localidad nuevaLocalidad = localidadService.guardar(localidad);
            return new ResponseEntity<>(nuevaLocalidad, HttpStatus.CREATED);
        } catch (Exception e) {
            // Considera usar un @ControllerAdvice para un manejo de excepciones más centralizado
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Localidad>> obtenerTodasLasLocalidades() {
        try {
            List<Localidad> localidades = localidadService.obtenerTodas();
            if (localidades.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(localidades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerLocalidadPorId(@PathVariable Integer id) {
        try {
            Localidad localidad = localidadService.obtenerPorId(id);
            return ResponseEntity.ok(localidad);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarLocalidad(@PathVariable Integer id, @RequestBody Localidad localidadDetalles) {
        if (localidadDetalles.getNombre() == null || localidadDetalles.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la localidad es obligatorio para actualizar.");
        }
        // La validación de la provincia se hace en el servicio
        try {
            Localidad localidadActualizada = localidadService.actualizar(id, localidadDetalles);
            return ResponseEntity.ok(localidadActualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrarLocalidad(@PathVariable Integer id) {
        try {
            localidadService.borrar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}