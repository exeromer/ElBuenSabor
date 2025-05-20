package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.FacturaCreateRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Factura;
import com.powerRanger.ElBuenSabor.services.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
// Quita los imports de tus excepciones custom si no las estás usando/definiendo por ahora
// import com.powerRanger.ElBuenSabor.exceptions.ResourceNotFoundException;
// import com.powerRanger.ElBuenSabor.exceptions.InvalidOperationException;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas") // Cambiado a /api/facturas para consistencia
@Validated
// @CrossOrigin(origins = "*") // Considera configurar CORS globalmente en SecurityConfig
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping("/activas")
    public ResponseEntity<List<Factura>> getAllFacturasActivas() {
        List<Factura> facturas = facturaService.getAllActivas();
        if (facturas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(facturas);
    }

    @GetMapping
    public ResponseEntity<List<Factura>> getAllFacturasIncludingAnuladas() {
        List<Factura> facturas = facturaService.getAll();
        if (facturas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}/activa")
    public ResponseEntity<?> getFacturaActivaById(@PathVariable Integer id) {
        try {
            Factura factura = facturaService.findByIdActiva(id);
            return ResponseEntity.ok(factura);
        } catch (Exception e) { // Captura genérica, ResourceNotFoundException sería más específica
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFacturaByIdIncludingAnuladas(@PathVariable Integer id) {
        try {
            Factura factura = facturaService.findByIdIncludingAnuladas(id);
            return ResponseEntity.ok(factura);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/generar-desde-pedido")
    public ResponseEntity<?> generarFacturaDesdePedido(@Valid @RequestBody FacturaCreateRequestDTO dto) {
        try {
            Factura facturaGenerada = facturaService.generarFacturaParaPedido(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaGenerada);
        } catch (Exception e) { // Captura genérica
            // Podrías diferenciar el HttpStatus basado en el tipo de excepción si usaras excepciones custom
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return buildErrorResponse(e.getMessage(), status);
        }
    }

    // El endpoint POST para crear una factura "manualmente" usualmente no se expone o es muy restringido.
    // La generación a partir de un pedido es el flujo principal.
    // Si lo necesitas, asegúrate que el servicio saveManualFactura valide todo correctamente.
    // @PostMapping
    // public ResponseEntity<?> createManualFactura(@Valid @RequestBody Factura factura) { ... }


    @PutMapping("/anular/{id}") // Usar PUT para una acción de cambio de estado es común, o POST
    public ResponseEntity<?> anularFactura(@PathVariable Integer id) {
        try {
            Factura facturaAnulada = facturaService.anularFactura(id);
            return ResponseEntity.ok(facturaAnulada);
        } catch (Exception e) { // Captura genérica
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return buildErrorResponse(e.getMessage(), status);
        }
    }

    // Método helper para construir respuestas de error
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", message);
        return ResponseEntity.status(status).body(errorResponse);
    }
}