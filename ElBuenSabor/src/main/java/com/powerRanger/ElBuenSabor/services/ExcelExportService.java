package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.MovimientosMonetariosDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] exportClientesRankingToExcel(List<ClienteRankingDTO> clientes) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ranking Clientes");

        // Header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID Cliente");
        headerRow.createCell(1).setCellValue("Nombre Completo");
        headerRow.createCell(2).setCellValue("Email");
        headerRow.createCell(3).setCellValue("Cantidad Pedidos");
        headerRow.createCell(4).setCellValue("Monto Total Comprado");

        // Data
        int rowNum = 1;
        for (ClienteRankingDTO cliente : clientes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(cliente.getClienteId());
            row.createCell(1).setCellValue(cliente.getNombreCompleto());
            row.createCell(2).setCellValue(cliente.getEmail());
            row.createCell(3).setCellValue(cliente.getCantidadPedidos());
            row.createCell(4).setCellValue(cliente.getMontoTotalComprado());
        }

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    public byte[] exportArticulosManufacturadosRankingToExcel(List<ArticuloManufacturadoRankingDTO> articulos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ranking Art. Manufacturados");

        // Header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID Artículo");
        headerRow.createCell(1).setCellValue("Denominación");
        headerRow.createCell(2).setCellValue("Cantidad Vendida");

        // Data
        int rowNum = 1;
        for (ArticuloManufacturadoRankingDTO articulo : articulos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(articulo.getArticuloId());
            row.createCell(1).setCellValue(articulo.getDenominacion());
            row.createCell(2).setCellValue(articulo.getCantidadVendida());
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    public byte[] exportArticulosInsumosRankingToExcel(List<ArticuloInsumoRankingDTO> articulos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Ranking Art. Insumos (Bebidas)");

        // Header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID Artículo");
        headerRow.createCell(1).setCellValue("Denominación");
        headerRow.createCell(2).setCellValue("Cantidad Vendida");

        // Data
        int rowNum = 1;
        for (ArticuloInsumoRankingDTO articulo : articulos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(articulo.getArticuloId());
            row.createCell(1).setCellValue(articulo.getDenominacion());
            row.createCell(2).setCellValue(articulo.getCantidadVendida());
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    public byte[] exportMovimientosMonetariosToExcel(MovimientosMonetariosDTO movimientos) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Movimientos Monetarios");

        // Data
        sheet.createRow(0).createCell(0).setCellValue("Ingresos Totales:");
        sheet.createRow(0).createCell(1).setCellValue(movimientos.getIngresosTotales());

        sheet.createRow(1).createCell(0).setCellValue("Costos Totales:");
        sheet.createRow(1).createCell(1).setCellValue(movimientos.getCostosTotales());

        sheet.createRow(2).createCell(0).setCellValue("Ganancias Netas:");
        sheet.createRow(2).createCell(1).setCellValue(movimientos.getGananciasNetas());

        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
}