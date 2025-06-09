package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.SucursalRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.SucursalResponseDTO;
import com.powerRanger.ElBuenSabor.services.SucursalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sucursales")
@Validated
public class SucursalController {

    @Autowired
    private SucursalService sucursalService;

    @PostMapping
    public ResponseEntity<SucursalResponseDTO> createSucursal(@Valid @RequestBody SucursalRequestDTO dto) throws Exception {
        SucursalResponseDTO nuevaSucursalDto = sucursalService.createSucursal(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSucursalDto);
    }

    @GetMapping
    public ResponseEntity<List<SucursalResponseDTO>> getAllSucursales() throws Exception {
        List<SucursalResponseDTO> sucursales = sucursalService.findAllSucursales();
        return ResponseEntity.ok(sucursales);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponseDTO> getSucursalById(@PathVariable Integer id) throws Exception {
        SucursalResponseDTO sucursalDto = sucursalService.findSucursalById(id);
        return ResponseEntity.ok(sucursalDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponseDTO> updateSucursal(@PathVariable Integer id, @Valid @RequestBody SucursalRequestDTO dto) throws Exception {
        SucursalResponseDTO sucursalActualizadaDto = sucursalService.updateSucursal(id, dto);
        return ResponseEntity.ok(sucursalActualizadaDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteSucursal(@PathVariable Integer id) throws Exception {
        sucursalService.softDelete(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Sucursal con ID " + id + " marcada como inactiva (borrado lógico).");
        return ResponseEntity.ok(response);
    }
}