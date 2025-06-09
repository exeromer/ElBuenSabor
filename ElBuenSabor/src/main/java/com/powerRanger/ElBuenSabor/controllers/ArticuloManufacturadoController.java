package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO;
import com.powerRanger.ElBuenSabor.services.ArticuloManufacturadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/articulosmanufacturados")
@Validated
public class ArticuloManufacturadoController {

    @Autowired
    private ArticuloManufacturadoService manufacturadoService;

    @PostMapping
    public ResponseEntity<ArticuloManufacturadoResponseDTO> createArticuloManufacturado(@Valid @RequestBody ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturadoResponseDTO nuevoAMDto = manufacturadoService.createArticuloManufacturado(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoAMDto);
    }

    @GetMapping
    public ResponseEntity<List<ArticuloManufacturadoResponseDTO>> getAllArticuloManufacturados(
            @RequestParam(name = "denominacion", required = false) String searchTerm,
            @RequestParam(name = "estado", required = false) Boolean estadoActivo) {
        List<ArticuloManufacturadoResponseDTO> ams = manufacturadoService.findAllManufacturados(searchTerm, estadoActivo);
        return ResponseEntity.ok(ams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> getArticuloManufacturadoById(@PathVariable Integer id) throws Exception {
        ArticuloManufacturadoResponseDTO amDto = manufacturadoService.findManufacturadoById(id);
        return ResponseEntity.ok(amDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticuloManufacturadoResponseDTO> updateArticuloManufacturado(@PathVariable Integer id, @Valid @RequestBody ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturadoResponseDTO amActualizadoDto = manufacturadoService.updateArticuloManufacturado(id, dto);
        return ResponseEntity.ok(amActualizadoDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticuloManufacturado(@PathVariable Integer id) throws Exception {
        manufacturadoService.delete(id);
        return ResponseEntity.noContent().build();
    }

}