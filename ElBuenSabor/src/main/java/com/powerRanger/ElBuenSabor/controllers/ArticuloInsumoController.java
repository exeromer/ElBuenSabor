package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO;
import com.powerRanger.ElBuenSabor.services.ArticuloInsumoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/articulosinsumo")
@Validated
public class ArticuloInsumoController {

    @Autowired
    private ArticuloInsumoService articuloInsumoService;

    @PostMapping
    public ResponseEntity<ArticuloInsumoResponseDTO> createArticuloInsumo(@Valid @RequestBody ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumoResponseDTO nuevoInsumoDto = articuloInsumoService.createArticuloInsumo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoInsumoDto);
    }

    @GetMapping
    public ResponseEntity<List<ArticuloInsumoResponseDTO>> getAllArticuloInsumo(
            @RequestParam(name = "denominacion", required = false) String searchTerm,
            @RequestParam(name = "estado", required = false) Boolean estadoActivo) {
        List<ArticuloInsumoResponseDTO> insumos = articuloInsumoService.findAllInsumos(searchTerm, estadoActivo);
        return ResponseEntity.ok(insumos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloInsumoResponseDTO> getArticuloInsumoById(@PathVariable Integer id) throws Exception {
        ArticuloInsumoResponseDTO insumoDto = articuloInsumoService.findInsumoById(id);
        return ResponseEntity.ok(insumoDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticuloInsumoResponseDTO> updateArticuloInsumo(@PathVariable Integer id, @Valid @RequestBody ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumoResponseDTO insumoActualizadoDto = articuloInsumoService.updateArticuloInsumo(id, dto);
        return ResponseEntity.ok(insumoActualizadoDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticuloInsumo(@PathVariable Integer id) throws Exception {
        articuloInsumoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}