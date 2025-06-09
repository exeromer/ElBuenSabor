package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*; // Todos los DTOs
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado; // Para validar estado del pedido
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.repository.FacturaRepository;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.ArticuloManufacturadoRepository;
// Asumiendo que Mappers.java existe o los mappers están aquí
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class FacturaServiceImpl implements FacturaService {

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private Mappers mappers; // Inyectar la clase Mappers
    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private ArticuloManufacturadoRepository articuloManufacturadoRepository;


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
        // 1. Obtener la factura y validar su estado
        Factura facturaAAnular = facturaRepository.findById(id)
                .orElseThrow(() -> new Exception("Factura con ID " + id + " no encontrada."));

        if (facturaAAnular.getEstadoFactura() == EstadoFactura.ANULADA) {
            throw new Exception("La factura con ID " + id + " ya se encuentra anulada.");
        }

        Pedido pedidoOriginal = facturaAAnular.getPedido();
        if (pedidoOriginal == null) {
            // Esto no debería ocurrir si la lógica de creación de factura es correcta.
            throw new Exception("La factura con ID " + id + " no tiene un pedido asociado. No se puede procesar la anulación de stock.");
        }

        // Recargar el pedido para asegurar que los detalles y el estado estén frescos y completamente cargados
        Pedido pedidoConDetalles = pedidoRepository.findById(pedidoOriginal.getId())
                .orElseThrow(() -> new Exception("Pedido asociado con ID " + pedidoOriginal.getId() + " no pudo ser recargado para la anulación."));

        // El estado del pedido al momento de la facturación era ENTREGADO.
        // La lógica de reposición se basará principalmente en si el insumo es para elaborar o no.
        System.out.println("DEBUG ANULACION: Anulando factura ID: " + id + " para Pedido ID: " + pedidoConDetalles.getId() + ". Estado original del pedido (al facturar): " + pedidoConDetalles.getEstado() + " (Se asume ENTREGADO para facturación).");

        if (pedidoConDetalles.getDetalles() == null || pedidoConDetalles.getDetalles().isEmpty()) {
            System.out.println("WARN ANULACION: El pedido ID " + pedidoConDetalles.getId() + " no tiene detalles. No se repondrá stock.");
        } else {
            System.out.println("DEBUG ANULACION: Número de detalles en pedidoConDetalles: " + pedidoConDetalles.getDetalles().size()); // LOG AÑADIDO
            for (DetallePedido detallePedido : pedidoConDetalles.getDetalles()) {
                Articulo articuloDelDetalleOriginal = detallePedido.getArticulo();
                if (articuloDelDetalleOriginal == null) {
                    System.err.println("WARN ANULACION: Detalle de pedido ID " + detallePedido.getId() + " no tiene un artículo asociado. Se omitirá para reposición.");
                    continue;
                }
                int cantidadEnPedido = detallePedido.getCantidad();
                // Log para ver qué tipo de proxy es inicialmente
                System.out.println("DEBUG ANULACION: Procesando detalle para Artículo ID: " + articuloDelDetalleOriginal.getId() +
                        ", Denominación: " + articuloDelDetalleOriginal.getDenominacion() +
                        ", Cantidad en pedido: " + cantidadEnPedido +
                        ", Tipo Original del Detalle: " + articuloDelDetalleOriginal.getClass().getName());

                // Intentar obtener la instancia concreta
                ArticuloInsumo insumoConcreto = null;
                ArticuloManufacturado manufacturadoConcreto = null;

                // Primero intenta como ArticuloInsumo
                Optional<ArticuloInsumo> optInsumo = articuloInsumoRepository.findById(articuloDelDetalleOriginal.getId());
                if (optInsumo.isPresent()) {
                    insumoConcreto = optInsumo.get();
                    System.out.println("DEBUG ANULACION:   Artículo es INSUMO. Denominación: " + insumoConcreto.getDenominacion() + ", esParaElaborar: " + insumoConcreto.getEsParaElaborar());

                    if (insumoConcreto.getEsParaElaborar() != null && !insumoConcreto.getEsParaElaborar()) {
                        double stockPrevio = insumoConcreto.getStockActual() != null ? insumoConcreto.getStockActual() : 0.0;
                        insumoConcreto.setStockActual(stockPrevio + cantidadEnPedido);
                        articuloInsumoRepository.save(insumoConcreto); // Guardar la instancia concreta
                        System.out.println("DEBUG ANULACION:     Stock de Insumo (No para elaborar) " + insumoConcreto.getDenominacion() + " actualizado de " + stockPrevio + " a " + insumoConcreto.getStockActual());
                    } else {
                        System.out.println("DEBUG ANULACION:     Insumo " + insumoConcreto.getDenominacion() + " es para elaborar o flag es nulo. No se repone stock para factura de pedido entregado.");
                    }
                } else {
                    // Si no es Insumo, intenta como ArticuloManufacturado
                    Optional<ArticuloManufacturado> optManuf = articuloManufacturadoRepository.findById(articuloDelDetalleOriginal.getId());
                    if (optManuf.isPresent()) {
                        manufacturadoConcreto = optManuf.get();
                        System.out.println("DEBUG ANULACION:   Es MANUFACTURADO: " + manufacturadoConcreto.getDenominacion() + ". Los componentes no se reponen para una factura de un pedido entregado.");
                        // No se reponen componentes aquí para un pedido ENTREGADO.
                    } else {
                        // Si no es ninguno de los dos, es un Articulo base o un error.
                        System.err.println("WARN ANULACION:   Artículo ID " + articuloDelDetalleOriginal.getId() + " no se encontró como Insumo ni como Manufacturado específico. No se procesa para reposición de stock.");
                    }
                }
            }
            System.out.println("DEBUG ANULACION: Procesamiento de reposición de stock (condicionada) completada para factura ID: " + id);
        }

        // 3. Actualizar estado de la factura
        facturaAAnular.setEstadoFactura(EstadoFactura.ANULADA);
        facturaAAnular.setFechaAnulacion(LocalDate.now());
        Factura facturaGuardada = facturaRepository.save(facturaAAnular);

        System.out.println("INFO: Factura ID: " + id + " anulada correctamente.");
        return convertToResponseDto(facturaGuardada);
    }

}