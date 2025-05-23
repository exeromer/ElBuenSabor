package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.FacturaCreateRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.FacturaResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Factura; // Ya no se devuelve entidad
import jakarta.validation.Valid;
import java.util.List;

public interface FacturaService {
    List<FacturaResponseDTO> getAllActivas();
    List<FacturaResponseDTO> getAll();
    FacturaResponseDTO findByIdActiva(Integer id) throws Exception;
    FacturaResponseDTO findByIdIncludingAnuladas(Integer id) throws Exception;
    FacturaResponseDTO generarFacturaParaPedido(@Valid FacturaCreateRequestDTO dto) throws Exception;
    FacturaResponseDTO anularFactura(Integer id) throws Exception;
}