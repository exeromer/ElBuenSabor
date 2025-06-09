package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloBaseResponseDTO;
import com.powerRanger.ElBuenSabor.services.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articulos")
@Validated
public class ArticuloController {

    @Autowired
    private ArticuloService articuloService;

    @GetMapping
    public ResponseEntity<List<ArticuloBaseResponseDTO>> getAllArticulos() throws Exception {
        List<ArticuloBaseResponseDTO> articulos = articuloService.findAllArticulos();
        return ResponseEntity.ok(articulos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticuloBaseResponseDTO> getArticuloById(@PathVariable Integer id) throws Exception {
        ArticuloBaseResponseDTO articuloDto = articuloService.findArticuloById(id);
        return ResponseEntity.ok(articuloDto);
    }

    @GetMapping("/buscar")
    public ResponseEntity<ArticuloBaseResponseDTO> buscarPorDenominacion(@RequestParam String denominacion) throws Exception {
        ArticuloBaseResponseDTO articuloDto = articuloService.findByDenominacion(denominacion);
        return ResponseEntity.ok(articuloDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticulo(@PathVariable Integer id) throws Exception {
        articuloService.delete(id);
        return ResponseEntity.noContent().build();
    }
}