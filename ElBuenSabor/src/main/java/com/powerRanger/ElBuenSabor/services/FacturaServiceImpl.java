package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*; // Todos los DTOs
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado; // Para validar estado del pedido
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.repository.FacturaRepository;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.ArticuloManufacturadoRepository;
import com.powerRanger.ElBuenSabor.mappers.Mappers; // Asumiendo que Mappers.java existe y está aquí
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory

import java.time.LocalDate;
import java.util.ArrayList;
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
    @Autowired private StockInsumoSucursalService stockInsumoSucursalService;
    @Autowired private EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(FacturaServiceImpl.class); // Inicializar Logger

    // --- MAPPERS ---
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
        double subtotalBruto = 0;
        if (factura.getDetallesFactura() != null) {
            for (FacturaDetalle detalle : factura.getDetallesFactura()) {
                subtotalBruto += detalle.getPrecioUnitarioArticulo() * detalle.getCantidad();
            }
        }
        dto.setSubtotal(subtotalBruto);
        dto.setTotalDescuentos(subtotalBruto - factura.getTotalVenta());

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
        logger.info("Iniciando generación de factura para Pedido ID: {}", dto.getPedidoId());

        Pedido pedidoAFacturar = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new Exception("Pedido con ID " + dto.getPedidoId() + " no encontrado."));

        if (facturaRepository.findByPedidoId(pedidoAFacturar.getId())
                .map(f -> f.getEstadoFactura() == EstadoFactura.ACTIVA).orElse(false)) {
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " ya tiene una factura activa.");
        }

        if (pedidoAFacturar.getEstado() != Estado.ENTREGADO) {
            logger.warn("Intento de facturar Pedido ID: {} con estado incorrecto: {}", dto.getPedidoId(), pedidoAFacturar.getEstado());
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " no está en estado ENTREGADO para ser facturado. Estado actual: " + pedidoAFacturar.getEstado());
        }

        if (pedidoAFacturar.getDetalles() == null || pedidoAFacturar.getDetalles().isEmpty()) {
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " no tiene detalles.");
        }

        Factura nuevaFactura = new Factura();
        nuevaFactura.setPedido(pedidoAFacturar);
        nuevaFactura.setFormaPago(pedidoAFacturar.getFormaPago());
        nuevaFactura.setTotalVenta(pedidoAFacturar.getTotal());

        if (pedidoAFacturar.getFormaPago() == FormaPago.MERCADO_PAGO) {
            nuevaFactura.setMpPaymentId(dto.getMpPaymentId());
            nuevaFactura.setMpMerchantOrderId(dto.getMpMerchantOrderId());
            nuevaFactura.setMpPreferenceId(pedidoAFacturar.getMpPreferenceId()); // El preferenceId ya está en el pedido
            nuevaFactura.setMpPaymentType(dto.getMpPaymentType());
        }
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
        pedidoRepository.save(pedidoAFacturar);

        logger.info("Factura ID: {} creada exitosamente para Pedido ID: {}", facturaGuardada.getId(), dto.getPedidoId());
        try {
            // Le pasamos la entidad completa para que tenga toda la información
            emailService.enviarFacturaPorEmail(facturaGuardada);
        } catch (Exception e) {
            logger.error("La factura se creó (ID: {}), pero falló el envío por email: {}", facturaGuardada.getId(), e.getMessage());
        }
        return convertToResponseDto(facturaGuardada);
    }

    @Override
    @Transactional
    public FacturaResponseDTO anularFactura(Integer id) throws Exception {
        logger.info("INICIO ANULACIÓN: Solicitud para anular Factura ID: {}", id);
        Factura facturaAAnular = facturaRepository.findById(id)
                .orElseThrow(() -> new Exception("Factura con ID " + id + " no encontrada."));

        if (facturaAAnular.getEstadoFactura() == EstadoFactura.ANULADA) {
            throw new Exception("La factura con ID " + id + " ya se encuentra anulada.");
        }

        // LÓGICA CORREGIDA: Recargamos el pedido para obtener una instancia fresca y completa desde la BD.
        Pedido pedidoOriginal = pedidoRepository.findById(facturaAAnular.getPedido().getId())
                .orElseThrow(() -> new Exception("Error crítico: El pedido asociado a la factura no fue encontrado."));

        logger.info("Factura y Pedido ID: {} encontrados. Procediendo a anular.", pedidoOriginal.getId());

        // 1. Cambiar estados
        facturaAAnular.setEstadoFactura(EstadoFactura.ANULADA);
        facturaAAnular.setFechaAnulacion(LocalDate.now());
        pedidoOriginal.setEstado(Estado.CANCELADO);
        logger.info("-> PASO 1: Factura ID {} marcada como ANULADA. Pedido ID: {} marcado como CANCELADO.", id, pedidoOriginal.getId());

        // 2. Restituir Stock
        restituirStockDeFacturaAnulada(pedidoOriginal);

        // 3. Guardar los cambios
        facturaRepository.save(facturaAAnular);
        pedidoRepository.save(pedidoOriginal);
        logger.info("-> PASO 3: Cambios en Factura y Pedido guardados en la base de datos.");

        // 4. Envio Nota Credito
        try {
            emailService.enviarNotaDeCreditoPorEmail(facturaAAnular);
        } catch (Exception e) {
            logger.error("La factura se anuló (ID: {}), pero falló el envío de la nota de crédito por email: {}", id, e.getMessage());
        }

        logger.info("FIN ANULACIÓN: Proceso completado para Factura ID: {}", id);
        return convertToResponseDto(facturaAAnular);
    }

    private void restituirStockDeFacturaAnulada(Pedido pedido) throws Exception {
        logger.info("--> PASO 2.1: Iniciando restitución de stock para Pedido ID: {}", pedido.getId());

        // LÓGICA REFORZADA: Se vuelve a cargar el pedido completo con todos sus detalles dentro de la misma transacción.
        Pedido pedidoConDetalles = pedidoRepository.findById(pedido.getId())
                .orElseThrow(() -> new Exception("Error crítico: El pedido no pudo ser recargado."));

        if (pedidoConDetalles.getDetalles() == null || pedidoConDetalles.getDetalles().isEmpty()) {
            logger.warn("ADVERTENCIA: El Pedido ID {} no tiene detalles. No se restituirá stock.", pedido.getId());
            return;
        }

        Integer sucursalId = pedidoConDetalles.getSucursal().getId();
        logger.info("--> Stock a restituir en Sucursal ID: {}", sucursalId);

        // Iteramos sobre la lista de detalles recién cargada y fresca.
        for (DetallePedido detallePedido : pedidoConDetalles.getDetalles()) {
            Articulo articulo = detallePedido.getArticulo();
            double cantidadPedida = detallePedido.getCantidad();

            logger.debug("--- Procesando Artículo: '{}' (ID: {}), Cantidad pedida: {}", articulo.getDenominacion(), articulo.getId(), cantidadPedida);

            // ¿Es un manufacturado?
            Optional<ArticuloManufacturado> optManuf = articuloManufacturadoRepository.findById(articulo.getId());
            if (optManuf.isPresent()) {
                ArticuloManufacturado manufacturado = optManuf.get();
                logger.info("    - Es MANUFACTURADO. Receta encontrada con {} insumos.", manufacturado.getManufacturadoDetalles().size());

                for (ArticuloManufacturadoDetalle detalleReceta : manufacturado.getManufacturadoDetalles()) {
                    ArticuloInsumo insumoComponente = detalleReceta.getArticuloInsumo();
                    double totalInsumoARestituir = detalleReceta.getCantidad() * cantidadPedida;
                    logger.info("      - Restituyendo Insumo de receta: '{}'. Cantidad a devolver: {}", insumoComponente.getDenominacion(), totalInsumoARestituir);
                    stockInsumoSucursalService.addStock(insumoComponente.getId(), sucursalId, totalInsumoARestituir);
                }
                continue; // Pasamos al siguiente artículo del pedido
            }

            // ¿Es un insumo?
            Optional<ArticuloInsumo> optInsumo = articuloInsumoRepository.findById(articulo.getId());
            if (optInsumo.isPresent()) {
                ArticuloInsumo insumoVendido = optInsumo.get();
                if (insumoVendido.getEsParaElaborar() != null && !insumoVendido.getEsParaElaborar()) {
                    logger.info("    - Es INSUMO de venta directa: '{}'. Cantidad a devolver: {}", insumoVendido.getDenominacion(), cantidadPedida);
                    stockInsumoSucursalService.addStock(insumoVendido.getId(), sucursalId, cantidadPedida);
                }
            }
        }
        logger.info("--> PASO 2.2: Proceso de restitución de stock finalizado para Pedido ID: {}.", pedido.getId());
    }
}