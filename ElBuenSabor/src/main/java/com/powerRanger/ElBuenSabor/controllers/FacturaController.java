package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.entities.Factura;
import com.powerRanger.ElBuenSabor.service.FacturaService; // La interfaz
import com.powerRanger.ElBuenSabor.exceptions.ResourceNotFoundException;
import com.powerRanger.ElBuenSabor.exceptions.InvalidOperationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/facturas")
@CrossOrigin(origins = "*")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping
    public ResponseEntity<List<Factura>> getAllFacturasActivas() {
        List<Factura> facturas = facturaService.getAllActivas();
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Factura>> getAllFacturasIncludingAnuladas() {
        List<Factura> facturas = facturaService.getAll();
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFacturaActivaById(@PathVariable Integer id) {
        try {
            Factura factura = facturaService.findByIdActiva(id);
            return ResponseEntity.ok(factura);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/generar")
    public ResponseEntity<?> generarFacturaDesdePedido(@RequestBody Map<String, Integer> payload) {
        Integer pedidoId = payload.get("pedidoId");
        if (pedidoId == null) {
            return ResponseEntity.badRequest().body("El campo 'pedidoId' es requerido en el cuerpo del request.");
        }
        try {
            Factura facturaGenerada = facturaService.generarFacturaParaPedido(pedidoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaGenerada);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidOperationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Loguear el error es importante en un sistema real
            // logger.error("Error al generar factura desde pedido: " + pedidoId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error interno al generar la factura: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createManualFactura(@RequestBody Factura factura) {
        try {
            Factura nuevaFactura = facturaService.saveManualFactura(factura);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaFactura);
        } catch (InvalidOperationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // logger.error("Error al crear factura manualmente", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error interno al crear la factura manualmente: " + e.getMessage());
        }
    }

    @DeleteMapping("/anular/{id}")
    public ResponseEntity<?> anularFactura(@PathVariable Integer id) {
        try {
            Factura facturaAnulada = facturaService.anularFactura(id);
            return ResponseEntity.ok(facturaAnulada);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidOperationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // logger.error("Error al anular factura: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error interno al anular la factura: " + e.getMessage());
        }
    }

    /*
    // El endpoint PUT para "actualizar" una factura genérica se mantiene comentado.
    // Si se necesita actualizar campos muy específicos, se deben crear endpoints dedicados.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFactura(@PathVariable Integer id, @RequestBody Factura facturaDetails) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                             .body("La actualización general de facturas no está permitida. Utilice la anulación o endpoints específicos para metadatos si existen.");
    }
    */
}