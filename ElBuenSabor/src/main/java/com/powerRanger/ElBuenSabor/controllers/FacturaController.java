package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.FacturaCreateRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.FacturaResponseDTO;
import com.powerRanger.ElBuenSabor.services.FacturaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@Validated
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping("/activas")
    public ResponseEntity<List<FacturaResponseDTO>> getAllFacturasActivas() {
        return ResponseEntity.ok(facturaService.getAllActivas());
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponseDTO>> getAllFacturasIncludingAnuladas() {
        return ResponseEntity.ok(facturaService.getAll());
    }

    @GetMapping("/{id}/activa")
    public ResponseEntity<FacturaResponseDTO> getFacturaActivaById(@PathVariable Integer id) throws Exception {
        return ResponseEntity.ok(facturaService.findByIdActiva(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDTO> getFacturaByIdIncludingAnuladas(@PathVariable Integer id) throws Exception {
        return ResponseEntity.ok(facturaService.findByIdIncludingAnuladas(id));
    }

    @PostMapping("/generar-desde-pedido")
    public ResponseEntity<FacturaResponseDTO> generarFacturaDesdePedido(@Valid @RequestBody FacturaCreateRequestDTO dto) throws Exception {
        FacturaResponseDTO facturaGeneradaDto = facturaService.generarFacturaParaPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaGeneradaDto);
    }

    @PutMapping("/anular/{id}")
    public ResponseEntity<FacturaResponseDTO> anularFactura(@PathVariable Integer id) throws Exception {
        FacturaResponseDTO facturaAnuladaDto = facturaService.anularFactura(id);
        return ResponseEntity.ok(facturaAnuladaDto);
    }
}