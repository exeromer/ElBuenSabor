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

    // MODIFICADO: Se añade {sucursalId} a la ruta
    @GetMapping("/sucursal/{sucursalId}/ranking-clientes/por-cantidad")
    public ResponseEntity<?> getRankingClientesPorCantidad(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // MODIFICADO: Se pasa sucursalId al servicio
            List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorCantidadPedidos(sucursalId, fechaDesde, fechaHasta, page, size);
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

    // MODIFICADO: Se añade {sucursalId} a la ruta
    @GetMapping("/sucursal/{sucursalId}/ranking-clientes/por-monto")
    public ResponseEntity<?> getRankingClientesPorMonto(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // MODIFICADO: Se pasa sucursalId al servicio
            List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorMontoTotal(sucursalId, fechaDesde, fechaHasta, page, size);
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

    // MODIFICADO: Ruta y nombre del método cambiados para "Productos de Cocina"
    @GetMapping("/sucursal/{sucursalId}/productos-cocina/ranking")
    public ResponseEntity<?> getRankingProductosCocina(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // MODIFICADO: Se llama al nuevo método del servicio
            List<ArticuloManufacturadoRankingDTO> ranking = estadisticaService.getRankingProductosCocina(sucursalId, fechaDesde, fechaHasta, page, size);
            if (ranking.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al procesar el ranking de productos de cocina.");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // MODIFICADO: Ruta y nombre del método cambiados para "Bebidas"
    @GetMapping("/sucursal/{sucursalId}/bebidas/ranking")
    public ResponseEntity<?> getRankingBebidas(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // MODIFICADO: Se llama al nuevo método del servicio
            List<ArticuloInsumoRankingDTO> ranking = estadisticaService.getRankingBebidas(sucursalId, fechaDesde, fechaHasta, page, size);
            if (ranking.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Error al procesar el ranking de bebidas.");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // MODIFICADO: Se añade {sucursalId} a la ruta
    @GetMapping("/sucursal/{sucursalId}/movimientos-monetarios")
    public ResponseEntity<?> getMovimientosMonetarios(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            // MODIFICADO: Se pasa sucursalId al servicio
            MovimientosMonetariosDTO movimientos = estadisticaService.getMovimientosMonetarios(sucursalId, fechaDesde, fechaHasta);
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

    // --- Endpoints de Exportación a Excel (también modificados) ---

    // MODIFICADO: Se añade {sucursalId} a la ruta
    @GetMapping("/sucursal/{sucursalId}/ranking-clientes/export/excel")
    public ResponseEntity<byte[]> exportClientesRankingExcel(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            // MODIFICADO: Se pasa sucursalId al servicio
            List<ClienteRankingDTO> ranking = estadisticaService.getRankingClientesPorCantidadPedidos(sucursalId, fechaDesde, fechaHasta, 0, Integer.MAX_VALUE);
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

    // MODIFICADO: Ruta y lógica para "Productos de Cocina"
    @GetMapping("/sucursal/{sucursalId}/productos-cocina/export/excel")
    public ResponseEntity<byte[]> exportProductosCocinaRankingExcel(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            // MODIFICADO: Se llama al nuevo método del servicio
            List<ArticuloManufacturadoRankingDTO> ranking = estadisticaService.getRankingProductosCocina(sucursalId, fechaDesde, fechaHasta, 0, Integer.MAX_VALUE);
            byte[] excelBytes = excelExportService.exportArticulosManufacturadosRankingToExcel(ranking);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "ranking_productos_cocina.xlsx");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // MODIFICADO: Ruta y lógica para "Bebidas"
    @GetMapping("/sucursal/{sucursalId}/bebidas/export/excel")
    public ResponseEntity<byte[]> exportBebidasRankingExcel(
            @PathVariable Integer sucursalId, // MODIFICADO: Se recibe el ID de la sucursal
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        try {
            // MODIFICADO: Se llama al nuevo método del servicio
            List<ArticuloInsumoRankingDTO> ranking = estadisticaService.getRankingBebidas(sucursalId, fechaDesde, fechaHasta, 0, Integer.MAX_VALUE);
            byte[] excelBytes = excelExportService.exportArticulosInsumosRankingToExcel(ranking);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "ranking_bebidas.xlsx");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // MODIFICADO: Se añade {sucursalId} a la ruta
    @GetMapping("/sucursal/{sucursalId}/movimientos-monetarios/export/excel")
public ResponseEntity<byte[]> exportMovimientosMonetariosExcel(
        @PathVariable Integer sucursalId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {

    try {
        // 1. Obtener los datos necesarios
        MovimientosMonetariosDTO movimientos = estadisticaService.getMovimientosMonetarios(sucursalId, fechaDesde, fechaHasta);

        // 2. Llamar al servicio de Excel para generar el archivo en bytes
        byte[] excelBytes = excelExportService.exportMovimientosMonetariosToExcel(movimientos);

        // 3. Preparar los encabezados de la respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // Tipo de contenido para descarga de archivo binario
        headers.setContentDispositionFormData("attachment", "movimientos_monetarios.xlsx"); // Nombre del archivo

        // 4. Devolver la respuesta con los bytes del archivo, los encabezados y el estado OK
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

    } catch (Exception e) {
        // En caso de error, devolver un error interno del servidor
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
}