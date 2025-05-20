package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Pais;
import com.powerRanger.ElBuenSabor.services.PaisService; // Importamos el servicio

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paises") // Ruta base para los endpoints de País
public class PaisController {

    @Autowired
    private PaisService paisService; // Inyectamos la instancia de PaisService

    /**
     * Endpoint para crear un nuevo País.
     * Llama al método guardar del servicio.
     * @param pais Objeto Pais enviado en el cuerpo de la petición.
     * @return ResponseEntity con el Pais creado y estado HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Pais> crearPais(@RequestBody Pais pais) {
        // Validación básica (puedes expandirla o moverla al servicio si prefieres)
        if (pais.getNombre() == null || pais.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Podrías devolver un DTO de error aquí
        }
        try {
            Pais nuevoPais = paisService.guardar(pais);
            return new ResponseEntity<>(nuevoPais, HttpStatus.CREATED);
        } catch (Exception e) {
            // Loguear el error e.g., logger.error("Error al crear país:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para obtener todos los Países.
     * Llama al método obtenerTodos del servicio.
     * @return ResponseEntity con la lista de Países y estado HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<List<Pais>> obtenerTodosLosPaises() {
        try {
            List<Pais> paises = paisService.obtenerTodos();
            if (paises.isEmpty()) {
                return ResponseEntity.noContent().build(); // HTTP 204 si no hay países
            }
            return ResponseEntity.ok(paises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para obtener un País por su ID.
     * Llama al método obtenerPorId del servicio.
     * @param id ID del Pais a buscar.
     * @return ResponseEntity con el Pais encontrado y estado HTTP 200 (OK),
     * o HTTP 404 (Not Found) si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pais> obtenerPaisPorId(@PathVariable Integer id) {
        try {
            Pais pais = paisService.obtenerPorId(id);
            return ResponseEntity.ok(pais);
        } catch (Exception e) { // Asumiendo que el servicio lanza una excepción si no lo encuentra
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // O un DTO de error personalizado
        }
    }

    /**
     * Endpoint para actualizar un País existente.
     * Llama al método actualizar del servicio.
     * @param id ID del Pais a actualizar.
     * @param paisDetalles Objeto Pais con los nuevos datos.
     * @return ResponseEntity con el Pais actualizado y estado HTTP 200 (OK),
     * o HTTP 404 (Not Found) si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Pais> actualizarPais(@PathVariable Integer id, @RequestBody Pais paisDetalles) {
        if (paisDetalles.getNombre() == null || paisDetalles.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Pais paisActualizado = paisService.actualizar(id, paisDetalles);
            return ResponseEntity.ok(paisActualizado);
        } catch (Exception e) { // Asumiendo que el servicio lanza una excepción si no lo encuentra
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Endpoint para borrar un País por su ID.
     * Llama al método borrar del servicio.
     * @param id ID del Pais a borrar.
     * @return ResponseEntity con estado HTTP 204 (No Content) si se borró,
     * o HTTP 404 (Not Found) si no existía.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarPais(@PathVariable Integer id) {
        try {
            boolean borrado = paisService.borrar(id);
            // El servicio PaisServiceImpl que te propuse lanza excepción si no lo encuentra.
            // Si llegamos aquí, significa que se borró (o no existía y el repo no lanzó error,
            // pero la implementación de mi servicio sí lo haría).
            // Por consistencia con el servicio que lanza excepción:
            // if (borrado) { return ResponseEntity.noContent().build(); }
            // else { return ResponseEntity.notFound().build(); }
            // Pero como el servicio ya lanza la excepción si no encuentra:
            return ResponseEntity.noContent().build();
        } catch (Exception e) { // Asumiendo que el servicio lanza una excepción si no lo encuentra
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}