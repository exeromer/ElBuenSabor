package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.FacturaCreateRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Factura;
import jakarta.validation.Valid;
import java.util.List;

public interface FacturaService {
    List<Factura> getAllActivas();
    List<Factura> getAll();
    Factura findByIdActiva(Integer id) throws Exception;
    Factura findByIdIncludingAnuladas(Integer id) throws Exception;
    Factura generarFacturaParaPedido(@Valid FacturaCreateRequestDTO dto) throws Exception;
    Factura anularFactura(Integer id) throws Exception;
    // El saveManualFactura y updateFactura son complejos y usualmente no se exponen así.
    // La creación es a partir de un pedido, y las actualizaciones son anulaciones.
}