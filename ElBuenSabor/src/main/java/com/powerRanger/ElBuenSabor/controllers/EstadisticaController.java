package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.services.EstadisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticaController {

    @Autowired
    private EstadisticaService estadisticaService;

    @GetMapping("/ranking-clientes/por-cantidad")
    public ResponseEntity<List<ClienteRankingDTO>> getRankingClientesPorCantidad(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {

        List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorCantidadPedidos(fechaDesde, fechaHasta, page, size);
        // La validación de lista vacía es buena práctica para endpoints GET que devuelven colecciones.
        if (ranking.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/ranking-clientes/por-monto")
    public ResponseEntity<List<ClienteRankingDTO>> getRankingClientesPorMonto(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {

        List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorMontoTotal(fechaDesde, fechaHasta, page, size);
        if (ranking.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/articulos-manufacturados/ranking/mas-vendidos")
    public ResponseEntity<List<ArticuloManufacturadoRankingDTO>> getRankingArticulosManufacturadosMasVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {

        List<ArticuloManufacturadoRankingDTO> ranking = estadisticaService.getRankingArticulosManufacturadosMasVendidos(fechaDesde, fechaHasta, page, size);
        if (ranking.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ranking);
    }
}