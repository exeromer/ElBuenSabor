package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.MovimientosMonetariosDTO;
import com.powerRanger.ElBuenSabor.services.EstadisticaService;
import com.powerRanger.ElBuenSabor.services.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticaController {

    @Autowired
    private EstadisticaService estadisticaService;
    @Autowired
    private ExcelExportService excelExportService;

    @GetMapping("/ranking-clientes/por-cantidad")
    public ResponseEntity<?> getRankingClientesPorCantidad(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorCantidadPedidos(fechaDesde, fechaHasta, page, size);
            if (ranking.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al procesar la solicitud de ranking por cantidad.");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/ranking-clientes/por-monto")
    public ResponseEntity<?> getRankingClientesPorMonto(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorMontoTotal(fechaDesde, fechaHasta, page, size);
            if (ranking.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al procesar la solicitud de ranking por monto.");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/articulos-manufacturados/ranking/mas-vendidos")
    public ResponseEntity<?> getRankingArticulosManufacturadosMasVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<ArticuloManufacturadoRankingDTO> ranking = estadisticaService.getRankingArticulosManufacturadosMasVendidos(fechaDesde, fechaHasta, page, size);
            if (ranking.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al procesar la solicitud de ranking de artículos manufacturados.");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
     // **INICIO DE CÓDIGO NUEVO**
    // New endpoint for ranking insumo articles (bebidas)
    @GetMapping("/articulos-insumos/ranking/mas-vendidos")
    public ResponseEntity<?> getRankingArticulosInsumosMasVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<ArticuloInsumoRankingDTO> ranking = estadisticaService.getRankingArticulosInsumosMasVendidos(fechaDesde, fechaHasta, page, size);
            if (ranking.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al procesar la solicitud de ranking de artículos insumos.");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // New endpoint for monetary movements
    @GetMapping("/movimientos-monetarios")
    public ResponseEntity<?> getMovimientosMonetarios(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            MovimientosMonetariosDTO movimientos = estadisticaService.getMovimientosMonetarios(fechaDesde, fechaHasta);
            return ResponseEntity.ok(movimientos);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al procesar la solicitud de movimientos monetarios.");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // New endpoint for exporting Client Ranking to Excel
    @GetMapping("/ranking-clientes/export/excel")
    public ResponseEntity<byte[]> exportClientesRankingExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorCantidadPedidos(fechaDesde, fechaHasta, 0, Integer.MAX_VALUE); // Fetch all data
            byte[] excelBytes = excelExportService.exportClientesRankingToExcel(ranking);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "ranking_clientes.xlsx");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // New endpoint for exporting Manufactured Product Ranking to Excel
    @GetMapping("/articulos-manufacturados/export/excel")
    public ResponseEntity<byte[]> exportArticulosManufacturadosRankingExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            List<ArticuloManufacturadoRankingDTO> ranking = estadisticaService.getRankingArticulosManufacturadosMasVendidos(fechaDesde, fechaHasta, 0, Integer.MAX_VALUE); // Fetch all data
            byte[] excelBytes = excelExportService.exportArticulosManufacturadosRankingToExcel(ranking);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "ranking_articulos_manufacturados.xlsx");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // New endpoint for exporting Insumo Product Ranking to Excel
    @GetMapping("/articulos-insumos/export/excel")
    public ResponseEntity<byte[]> exportArticulosInsumosRankingExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            List<ArticuloInsumoRankingDTO> ranking = estadisticaService.getRankingArticulosInsumosMasVendidos(fechaDesde, fechaHasta, 0, Integer.MAX_VALUE); // Fetch all data
            byte[] excelBytes = excelExportService.exportArticulosInsumosRankingToExcel(ranking);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "ranking_articulos_insumos.xlsx");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // New endpoint for exporting Monetary Movements to Excel
    @GetMapping("/movimientos-monetarios/export/excel")
    public ResponseEntity<byte[]> exportMovimientosMonetariosExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            MovimientosMonetariosDTO movimientos = estadisticaService.getMovimientosMonetarios(fechaDesde, fechaHasta);
            byte[] excelBytes = excelExportService.exportMovimientosMonetariosToExcel(movimientos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "movimientos_monetarios.xlsx");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // **FIN DE CÓDIGO NUEVO**
}