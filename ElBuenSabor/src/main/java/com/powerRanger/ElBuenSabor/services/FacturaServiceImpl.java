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
import com.powerRanger.ElBuenSabor.mappers.Mappers; // Asumiendo que Mappers.java existe y está aquí
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger; // Importar Logger
import org.slf4j.LoggerFactory; // Importar LoggerFactory

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
    @Autowired private StockInsumoSucursalService stockInsumoSucursalService; // Inyectar el nuevo servicio
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
            logger.error("La factura con ID {} no tiene un pedido asociado. No se puede procesar la anulación de stock.", id);
            throw new Exception("La factura con ID " + id + " no tiene un pedido asociado. No se puede procesar la anulación de stock.");
        }

        // Recargar el pedido para asegurar que los detalles y el estado estén frescos y completamente cargados
        Pedido pedidoConDetalles = pedidoRepository.findById(pedidoOriginal.getId())
                .orElseThrow(() -> new Exception("Pedido asociado con ID " + pedidoOriginal.getId() + " no pudo ser recargado para la anulación."));

        // Reponer stock
        restituirStockDeFacturaAnulada(pedidoConDetalles);

        // 3. Actualizar estado de la factura
        facturaAAnular.setEstadoFactura(EstadoFactura.ANULADA);
        facturaAAnular.setFechaAnulacion(LocalDate.now());
        Factura facturaGuardada = facturaRepository.save(facturaAAnular);

        logger.info("Factura ID: {} anulada correctamente.", id);
        return convertToResponseDto(facturaGuardada);
    }

    /**
     * Restituye el stock de los insumos asociados a los artículos de un pedido
     * cuando una factura es anulada.
     * La restitución se realiza por sucursal.
     * @param pedido El pedido asociado a la factura anulada, con sus detalles cargados.
     * @throws Exception Si no se encuentra la sucursal del pedido, o algún artículo/insumo.
     */
    private void restituirStockDeFacturaAnulada(Pedido pedido) throws Exception {
        logger.info("DEBUG ANULACION: Iniciando restitución de stock para Pedido ID: {}. Estado original del pedido (al facturar): {} (Se asume ENTREGADO para facturación).", pedido.getId(), pedido.getEstado());

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            logger.warn("WARN ANULACION: El pedido ID {} no tiene detalles. No se repondrá stock.", pedido.getId());
            return;
        }

        Integer sucursalId = pedido.getSucursal() != null ? pedido.getSucursal().getId() : null;
        if (sucursalId == null) {
            logger.error("ERROR ANULACION: No se pudo determinar la sucursal del pedido ID {}. No se restituirá stock.", pedido.getId());
            throw new Exception("No se pudo determinar la sucursal del pedido para restituir stock.");
        }

        logger.debug("DEBUG ANULACION: Número de detalles en pedidoConDetalles: {}", pedido.getDetalles().size());
        for (DetallePedido detallePedido : pedido.getDetalles()) {
            Articulo articuloDelDetalleOriginal = detallePedido.getArticulo();
            if (articuloDelDetalleOriginal == null) {
                logger.warn("WARN ANULACION: Detalle de pedido ID {} no tiene un artículo asociado. Se omitirá para reposición.", detallePedido.getId());
                continue;
            }
            int cantidadEnPedido = detallePedido.getCantidad();

            // Es crucial recargar el artículo para que JPA lo instancie como la subclase correcta (proxy o entidad completa)
            Articulo refreshedArticulo = articuloRepository.findById(articuloDelDetalleOriginal.getId())
                    .orElseThrow(() -> new Exception("ERROR ANULACION: Artículo ID " + articuloDelDetalleOriginal.getId() + " del detalle no encontrado para restitución."));

            logger.debug("DEBUG ANULACION: Procesando detalle para Artículo ID: {}, Denominación: '{}', Cantidad en pedido: {}, Tipo Real: {}",
                    refreshedArticulo.getId(), refreshedArticulo.getDenominacion(), cantidadEnPedido, refreshedArticulo.getClass().getName());


            if (refreshedArticulo instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) refreshedArticulo;
                logger.debug("DEBUG ANULACION:   Artículo es INSUMO. Denominación: '{}', esParaElaborar: {}.", insumo.getDenominacion(), insumo.getEsParaElaborar());

                if (insumo.getEsParaElaborar() != null && !insumo.getEsParaElaborar()) {
                    stockInsumoSucursalService.addStock(insumo.getId(), sucursalId, (double) cantidadEnPedido);
                    logger.info("INFO ANULACION:     Stock de Insumo (No para elaborar) '{}' (ID: {}) en Sucursal ID {} restituido en {}. Nuevo stock gestionado por StockInsumoSucursalService.",
                            insumo.getDenominacion(), insumo.getId(), sucursalId, cantidadEnPedido);
                } else {
                    logger.info("INFO ANULACION:     Insumo '{}' (ID: {}) es 'para elaborar'. Su stock no se repone directamente para una factura de pedido entregado.", insumo.getDenominacion(), insumo.getId());
                }
            } else if (refreshedArticulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) refreshedArticulo;
                logger.debug("DEBUG ANULACION:   Es MANUFACTURADO: '{}' (ID: {}). Procesando insumos componentes.", manufacturado.getDenominacion(), manufacturado.getId());

                List<ArticuloManufacturadoDetalle> detallesReceta = manufacturado.getManufacturadoDetalles();
                // Recargar detalles de receta si no están cargados (LAZY)
                if (detallesReceta == null || detallesReceta.isEmpty()) {
                    ArticuloManufacturado loadedManufacturado = articuloManufacturadoRepository.findById(manufacturado.getId())
                            .orElseThrow(() -> new Exception("ERROR ANULACION: No se pudo recargar el manufacturado '{}' (ID: {}) para obtener detalles de receta.".formatted(manufacturado.getDenominacion(), manufacturado.getId())));
                    detallesReceta = loadedManufacturado.getManufacturadoDetalles();
                    if (detallesReceta == null || detallesReceta.isEmpty()) {
                        logger.warn("WARN ANULACION: El ArticuloManufacturado '{}' (ID: {}) no tiene detalles de receta. No se restituirán sus insumos.", manufacturado.getDenominacion(), manufacturado.getId());
                        continue;
                    }
                }

                for (ArticuloManufacturadoDetalle detalleRecetaItem : detallesReceta) {
                    ArticuloInsumo insumoComponente = detalleRecetaItem.getArticuloInsumo();
                    if (insumoComponente == null) {
                        logger.error("ERROR ANULACION: Detalle de receta para manufacturado '{}' (ID: {}) tiene insumo componente nulo.", manufacturado.getDenominacion(), manufacturado.getId());
                        throw new Exception("Error en receta de '" + manufacturado.getDenominacion() + "': insumo nulo.");
                    }
                    Double cantidadNecesariaPorReceta = detalleRecetaItem.getCantidad();
                    Double cantidadAReponerTotal = cantidadNecesariaPorReceta * cantidadEnPedido;

                    stockInsumoSucursalService.addStock(insumoComponente.getId(), sucursalId, cantidadAReponerTotal);
                    logger.info("INFO ANULACION:     Stock de insumo componente '{}' (ID: {}) en Sucursal ID {} restituido en {}. Nuevo stock gestionado por StockInsumoSucursalService.",
                            insumoComponente.getDenominacion(), insumoComponente.getId(), sucursalId, cantidadAReponerTotal);
                }
            } else {
                logger.warn("WARN ANULACION: Artículo ID {} ('{}') no es insumo ni manufacturado. No se procesa restitución de stock.", refreshedArticulo.getId(), refreshedArticulo.getDenominacion());
            }
        }
        logger.info("DEBUG ANULACION: Procesamiento de reposición de stock completada para factura asociada a Pedido ID: {}.", pedido.getId());
    }
}