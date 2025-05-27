package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*; // Todos los DTOs
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado; // Para validar estado del pedido
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.repository.FacturaRepository;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
// Asumiendo que Mappers.java existe o los mappers están aquí
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class FacturaServiceImpl implements FacturaService {

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private Mappers mappers; // Inyectar la clase Mappers

    // --- MAPPERS (o usar la clase Mappers inyectada) ---
    // Si no tienes una clase Mappers, definirías los métodos convertTo...DTO aquí.
    // Por ejemplo:
    // private ArticuloSimpleResponseDTO convertArticuloToSimpleDto(Articulo articulo) { ... }
    // private FacturaDetalleResponseDTO convertFacturaDetalleToDto(FacturaDetalle detalle) { ... }
    // private PedidoSimpleResponseDTO convertPedidoToSimpleDto(Pedido pedido) { ... }
    // etc.

    private FacturaResponseDTO convertToResponseDto(Factura factura) {
        if (factura == null) return null;
        FacturaResponseDTO dto = new FacturaResponseDTO();
        dto.setId(factura.getId());
        dto.setFechaFacturacion(factura.getFechaFacturacion());
        dto.setMpPaymentId(factura.getMpPaymentId());
        dto.setMpMerchantOrderId(factura.getMpMerchantOrderId());
        dto.setMpPreferenceId(factura.getMpPreferenceId());
        dto.setMpPaymentType(factura.getMpPaymentType());
        dto.setTotalVenta(factura.getTotalVenta());
        dto.setFormaPago(factura.getFormaPago());
        dto.setEstadoFactura(factura.getEstadoFactura());
        dto.setFechaAnulacion(factura.getFechaAnulacion());

        if (factura.getPedido() != null) {
            PedidoSimpleResponseDTO pedidoDto = new PedidoSimpleResponseDTO();
            pedidoDto.setId(factura.getPedido().getId());
            pedidoDto.setFechaPedido(factura.getPedido().getFechaPedido());
            dto.setPedido(pedidoDto);
        }

        if (factura.getDetallesFactura() != null) {
            dto.setDetallesFactura(factura.getDetallesFactura().stream()
                    .map(detalle -> {
                        FacturaDetalleResponseDTO detalleDto = new FacturaDetalleResponseDTO();
                        detalleDto.setId(detalle.getId());
                        detalleDto.setCantidad(detalle.getCantidad());
                        detalleDto.setDenominacionArticulo(detalle.getDenominacionArticulo());
                        detalleDto.setPrecioUnitarioArticulo(detalle.getPrecioUnitarioArticulo());
                        detalleDto.setSubTotal(detalle.getSubTotal());
                        if (detalle.getArticulo() != null) {
                            // Usa el mapper de la clase Mappers si lo tienes
                            detalleDto.setArticulo(mappers.convertArticuloToSimpleDto(detalle.getArticulo()));
                        }
                        return detalleDto;
                    }).collect(Collectors.toList()));
        }
        return dto;
    }
    // --- FIN MAPPERS ---


    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> getAllActivas() {
        return facturaRepository.findByEstadoFactura(EstadoFactura.ACTIVA).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponseDTO> getAll() {
        return facturaRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO findByIdActiva(Integer id) throws Exception {
        Factura factura = facturaRepository.findByIdAndEstadoFactura(id, EstadoFactura.ACTIVA)
                .orElseThrow(() -> new Exception("Factura activa con ID " + id + " no encontrada."));
        return convertToResponseDto(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponseDTO findByIdIncludingAnuladas(Integer id) throws Exception {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new Exception("Factura con ID " + id + " no encontrada."));
        return convertToResponseDto(factura);
    }

    @Override
    @Transactional
    public FacturaResponseDTO generarFacturaParaPedido(@Valid FacturaCreateRequestDTO dto) throws Exception {
        Pedido pedidoAFacturar = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new Exception("Pedido con ID " + dto.getPedidoId() + " no encontrado."));

        if (facturaRepository.findByPedidoId(pedidoAFacturar.getId())
                .map(f -> f.getEstadoFactura() == EstadoFactura.ACTIVA).orElse(false)) {
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " ya tiene una factura activa.");
        }

        if (pedidoAFacturar.getEstado() != Estado.ENTREGADO) { // O el estado que consideres facturable
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " no está en estado ENTREGADO para ser facturado. Estado actual: " + pedidoAFacturar.getEstado());
        }

        if (pedidoAFacturar.getDetalles() == null || pedidoAFacturar.getDetalles().isEmpty()) {
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " no tiene detalles.");
        }

        Factura nuevaFactura = new Factura();
        nuevaFactura.setPedido(pedidoAFacturar);
        nuevaFactura.setFormaPago(pedidoAFacturar.getFormaPago());
        nuevaFactura.setTotalVenta(pedidoAFacturar.getTotal());
        nuevaFactura.setMpPaymentId(dto.getMpPaymentId());
        nuevaFactura.setMpMerchantOrderId(dto.getMpMerchantOrderId());
        nuevaFactura.setMpPreferenceId(dto.getMpPreferenceId());
        nuevaFactura.setMpPaymentType(dto.getMpPaymentType());

        for (DetallePedido detallePedido : pedidoAFacturar.getDetalles()) {
            FacturaDetalle facturaDetalle = new FacturaDetalle();
            facturaDetalle.setCantidad(detallePedido.getCantidad());
            facturaDetalle.setDenominacionArticulo(detallePedido.getArticulo().getDenominacion());
            facturaDetalle.setPrecioUnitarioArticulo(detallePedido.getArticulo().getPrecioVenta());
            facturaDetalle.setSubTotal(detallePedido.getSubTotal());
            facturaDetalle.setArticulo(detallePedido.getArticulo());
            nuevaFactura.addDetalleFactura(facturaDetalle);
        }

        Factura facturaGuardada = facturaRepository.save(nuevaFactura);

        pedidoAFacturar.setFactura(facturaGuardada);
        // Considera si debes cambiar el estado del pedido a FACTURADO aquí
        // if (pedidoAFacturar.getEstado() == Estado.ENTREGADO) { // O el estado previo
        //    pedidoAFacturar.setEstado(Estado.FACTURADO); // Si tienes este estado
        // }
        pedidoRepository.save(pedidoAFacturar);

        return convertToResponseDto(facturaGuardada);
    }

    @Override
    @Transactional
    public FacturaResponseDTO anularFactura(Integer id) throws Exception {
        Factura facturaAAnular = facturaRepository.findById(id) // Buscar sin importar estado para anular
                .orElseThrow(() -> new Exception("Factura con ID " + id + " no encontrada."));

        if (facturaAAnular.getEstadoFactura() == EstadoFactura.ANULADA) {
            throw new Exception("La factura con ID " + id + " ya se encuentra anulada.");
        }

        facturaAAnular.setEstadoFactura(EstadoFactura.ANULADA);
        facturaAAnular.setFechaAnulacion(LocalDate.now());

        // Opcional: Lógica de negocio al anular, ej. desvincular del pedido o cambiar estado del pedido.
        // Pedido pedidoAsociado = facturaAAnular.getPedido();
        // if (pedidoAsociado != null) {
        //     pedidoAsociado.setFactura(null);
        //     // pedidoAsociado.setEstado(Estado.PENDIENTE_DE_REFACTURACION); // Ejemplo
        //     pedidoRepository.save(pedidoAsociado);
        // }
        Factura facturaGuardada = facturaRepository.save(facturaAAnular);
        return convertToResponseDto(facturaGuardada);
    }
}