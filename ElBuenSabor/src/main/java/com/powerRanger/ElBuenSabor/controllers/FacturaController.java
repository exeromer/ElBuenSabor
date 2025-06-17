package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.FacturaCreateRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.FacturaResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Factura; // Ya no se devuelve entidad
import com.powerRanger.ElBuenSabor.services.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
// import jakarta.validation.ConstraintViolationException; // No es necesario si el DTO no tiene validaciones JSR 303 directas que el controller verifique

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
@Validated
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping("/activas")
    public ResponseEntity<List<FacturaResponseDTO>> getAllFacturasActivas() {
        List<FacturaResponseDTO> facturas = facturaService.getAllActivas();
        if (facturas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(facturas);
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponseDTO>> getAllFacturasIncludingAnuladas() {
        List<FacturaResponseDTO> facturas = facturaService.getAll();
        if (facturas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(facturas);
    }

    @GetMapping("/{id}/activa")
    public ResponseEntity<?> getFacturaActivaById(@PathVariable Integer id) {
        try {
            FacturaResponseDTO facturaDto = facturaService.findByIdActiva(id);
            return ResponseEntity.ok(facturaDto);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFacturaByIdIncludingAnuladas(@PathVariable Integer id) {
        try {
            FacturaResponseDTO facturaDto = facturaService.findByIdIncludingAnuladas(id);
            return ResponseEntity.ok(facturaDto);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/generar-desde-pedido")
    public ResponseEntity<?> generarFacturaDesdePedido(@Valid @RequestBody FacturaCreateRequestDTO dto) {
        try {
            FacturaResponseDTO facturaGeneradaDto = facturaService.generarFacturaParaPedido(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaGeneradaDto);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("ya tiene una factura activa") || e.getMessage().contains("no tiene detalles") || e.getMessage().contains("no está en estado ENTREGADO")) {
                status = HttpStatus.BAD_REQUEST; // O CONFLICT (409) para "ya tiene factura"
            }
            return buildErrorResponse(e.getMessage(), status);
        }
    }

    @PutMapping("/anular/{id}")
    public ResponseEntity<?> anularFactura(@PathVariable Integer id) {
        try {
            FacturaResponseDTO facturaAnuladaDto = facturaService.anularFactura(id);
            return ResponseEntity.ok(facturaAnuladaDto);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrada") ? HttpStatus.NOT_FOUND :
                    (e.getMessage().contains("ya se encuentra anulada") ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR);
            return buildErrorResponse(e.getMessage(), status);
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", message);
        // e.printStackTrace(); // Para depuración si es necesario
        return ResponseEntity.status(status).body(errorResponse);
    }
}