package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.DetallePedido; // ✅ CORREGIDO: Importar DetallePedido
import com.powerRanger.ElBuenSabor.services.DetallePedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detallepedidos") // Ruta base consistente
public class DetallePedidoController {

    @Autowired
    private DetallePedidoService detallePedidoService;

    @GetMapping
    public ResponseEntity<List<DetallePedido>> getAllDetallePedidos() { // ✅ Usa DetallePedido
        try {
            List<DetallePedido> detalles = detallePedidoService.getAllDetallePedidos();
            if (detalles.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(detalles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetallePedidoById(@PathVariable Integer id) { // ✅ Usa DetallePedido
        try {
            DetallePedido detalle = detallePedidoService.getDetallePedidoById(id);
            return ResponseEntity.ok(detalle);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<?> createDetallePedido(@RequestBody DetallePedido detallePedido) { // ✅ Usa DetallePedido
        try {
            // El servicio createDetallePedido debería validar que el Pedido y Articulo asociados existan
            DetallePedido nuevoDetalle = detallePedidoService.createDetallePedido(detallePedido);
            return new ResponseEntity<>(nuevoDetalle, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDetallePedido(@PathVariable Integer id, @RequestBody DetallePedido detallePedido) { // ✅ Usa DetallePedido
        try {
            DetallePedido detalleActualizado = detallePedidoService.updateDetallePedido(id, detallePedido);
            return ResponseEntity.ok(detalleActualizado);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDetallePedido(@PathVariable Integer id) {
        try {
            detallePedidoService.deleteDetallePedido(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}