package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Provincia;
import com.powerRanger.ElBuenSabor.services.ProvinciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provincias")
public class ProvinciaController {

    @Autowired
    private ProvinciaService provinciaService;

    @PostMapping
    public ResponseEntity<?> crearProvincia(@RequestBody Provincia provincia) {
        // Validación básica
        if (provincia.getNombre() == null || provincia.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la provincia es obligatorio.");
        }
        if (provincia.getPais() == null || provincia.getPais().getId() == null) {
            return ResponseEntity.badRequest().body("Es obligatorio especificar el ID del país al que pertenece la provincia.");
        }

        try {
            Provincia nuevaProvincia = provinciaService.guardar(provincia);
            return new ResponseEntity<>(nuevaProvincia, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Provincia>> obtenerTodasLasProvincias() {
        try {
            List<Provincia> provincias = provinciaService.obtenerTodas();
            if (provincias.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(provincias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProvinciaPorId(@PathVariable Integer id) {
        try {
            Provincia provincia = provinciaService.obtenerPorId(id);
            return ResponseEntity.ok(provincia);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProvincia(@PathVariable Integer id, @RequestBody Provincia provinciaDetalles) {
        if (provinciaDetalles.getNombre() == null || provinciaDetalles.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la provincia es obligatorio para actualizar.");
        }
        // La validación del país se hace en el servicio
        try {
            Provincia provinciaActualizada = provinciaService.actualizar(id, provinciaDetalles);
            return ResponseEntity.ok(provinciaActualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> borrarProvincia(@PathVariable Integer id) {
        try {
            provinciaService.borrar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}