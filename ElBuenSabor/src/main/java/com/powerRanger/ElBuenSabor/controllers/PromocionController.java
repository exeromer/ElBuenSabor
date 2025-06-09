package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PromocionResponseDTO;
import com.powerRanger.ElBuenSabor.services.PromocionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promociones")
@Validated
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    @PostMapping
    public ResponseEntity<PromocionResponseDTO> createPromocion(@Valid @RequestBody PromocionRequestDTO dto) throws Exception {
        PromocionResponseDTO nuevaPromocionDto = promocionService.createPromocion(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPromocionDto);
    }

    @GetMapping
    public ResponseEntity<List<PromocionResponseDTO>> getAllPromociones() throws Exception {
        List<PromocionResponseDTO> promociones = promocionService.findAllPromociones();
        return ResponseEntity.ok(promociones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> getPromocionById(@PathVariable Integer id) throws Exception {
        PromocionResponseDTO promocionDto = promocionService.findPromocionById(id);
        return ResponseEntity.ok(promocionDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromocionResponseDTO> updatePromocion(@PathVariable Integer id, @Valid @RequestBody PromocionRequestDTO dto) throws Exception {
        PromocionResponseDTO promocionActualizadaDto = promocionService.updatePromocion(id, dto);
        return ResponseEntity.ok(promocionActualizadaDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeletePromocion(@PathVariable Integer id) throws Exception {
        // CORRECCIÓN AQUÍ: Cambiado de 'softDeletePromocion' a 'softDelete'
        promocionService.softDelete(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Promoción con ID " + id + " marcada como inactiva (borrado lógico).");
        return ResponseEntity.ok(response);
    }
}