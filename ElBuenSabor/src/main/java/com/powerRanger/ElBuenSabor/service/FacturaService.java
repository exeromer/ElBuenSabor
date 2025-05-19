package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Factura;
// import com.powerRanger.ElBuenSabor.entities.Pedido; // No es necesario importar Pedido aquí si solo se usa el ID

import java.util.List;

public interface FacturaService {

    List<Factura> getAllActivas(); // Solo facturas activas

    List<Factura> getAll(); // Todas las facturas, incluyendo anuladas

    Factura findByIdActiva(Integer id); // Busca por ID solo si está activa

    Factura findByIdIncludingAnuladas(Integer id); // Busca por ID sin importar estado

    Factura generarFacturaParaPedido(Integer pedidoId) throws Exception;

    // Método para creación directa de una factura cuando el objeto Factura ya está construido.
    Factura saveManualFactura(Factura factura);

    // La actualización de facturas es controversial. Las facturas emitidas NO deberían modificarse.
    // Se anulan y se emiten notas de crédito/débito.
    // Factura updateMetadatosFactura(Integer id, Factura facturadetails) throws Exception; // Para metadatos no críticos y solo si está ACTIVA y no "cerrada"

    Factura anularFactura(Integer id) throws Exception;
}