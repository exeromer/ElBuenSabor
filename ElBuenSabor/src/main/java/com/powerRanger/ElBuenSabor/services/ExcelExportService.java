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
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

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
            // 1. Crear un libro de trabajo XSSF (para colores personalizados)
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Movimientos Monetarios");

            // --- Estilos de Celda con Colores de Fondo ---

            // Estilo para Ingresos (Azul)
            XSSFCellStyle ingresosStyle = workbook.createCellStyle();
            byte[] rgbIngresos = new byte[]{(byte) 0, (byte) 136, (byte) 254}; // #0088FE
            ingresosStyle.setFillForegroundColor(new XSSFColor(rgbIngresos, null));
            ingresosStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Estilo para Costos (Rojo)
            XSSFCellStyle costosStyle = workbook.createCellStyle();
            byte[] rgbCostos = new byte[]{(byte) 219, (byte) 21, (byte) 7}; // #db1507
            costosStyle.setFillForegroundColor(new XSSFColor(rgbCostos, null));
            costosStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Estilo para Ganancias (Verde)
            XSSFCellStyle gananciasStyle = workbook.createCellStyle();
            byte[] rgbGanancias = new byte[]{(byte) 7, (byte) 219, (byte) 14}; // #07db0e
            gananciasStyle.setFillForegroundColor(new XSSFColor(rgbGanancias, null));
            gananciasStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Opcional: Poner el texto en blanco para que resalte sobre el fondo oscuro
            Font whiteFont = workbook.createFont();
            whiteFont.setColor(IndexedColors.WHITE.getIndex());
            whiteFont.setBold(true);
            ingresosStyle.setFont(whiteFont);
            costosStyle.setFont(whiteFont);
            gananciasStyle.setFont(whiteFont);

            // --- Fila 1: Encabezados ---
            Row headerRow = sheet.createRow(0);

            Cell headerCellIngresos = headerRow.createCell(0);
            headerCellIngresos.setCellValue("Ingresos Totales");
            headerCellIngresos.setCellStyle(ingresosStyle);

            Cell headerCellCostos = headerRow.createCell(1);
            headerCellCostos.setCellValue("Costos Totales");
            headerCellCostos.setCellStyle(costosStyle);

            Cell headerCellGanancias = headerRow.createCell(2);
            headerCellGanancias.setCellValue("Ganancias Netas");
            headerCellGanancias.setCellStyle(gananciasStyle);

            // --- Fila 2: Datos ---
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(movimientos.getIngresosTotales());
            dataRow.createCell(1).setCellValue(movimientos.getCostosTotales());
            dataRow.createCell(2).setCellValue(movimientos.getGananciasNetas());

            // Ajustar el ancho de las columnas para que se lea bien
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            // --- Generar el archivo ---
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            return outputStream.toByteArray();
        }
}