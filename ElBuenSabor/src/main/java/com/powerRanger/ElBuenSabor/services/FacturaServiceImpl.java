package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.FacturaCreateRequestDTO;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.repository.FacturaRepository;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Service
@Validated
public class FacturaServiceImpl implements FacturaService {

    @Autowired private FacturaRepository facturaRepository;
    @Autowired private PedidoRepository pedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Factura> getAllActivas() {
        return facturaRepository.findByEstadoFactura(EstadoFactura.ACTIVA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> getAll() {
        return facturaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Factura findByIdActiva(Integer id) throws Exception {
        return facturaRepository.findByIdAndEstadoFactura(id, EstadoFactura.ACTIVA)
                .orElseThrow(() -> new Exception("Factura activa con ID " + id + " no encontrada."));
    }

    @Override
    @Transactional(readOnly = true)
    public Factura findByIdIncludingAnuladas(Integer id) throws Exception {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new Exception("Factura con ID " + id + " no encontrada."));
    }

    @Override
    @Transactional
    public Factura generarFacturaParaPedido(@Valid FacturaCreateRequestDTO dto) throws Exception {
        Pedido pedidoAFacturar = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new Exception("Pedido con ID " + dto.getPedidoId() + " no encontrado."));

        if (pedidoAFacturar.getFactura() != null &&
                pedidoAFacturar.getFactura().getEstadoFactura() == EstadoFactura.ACTIVA) {
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " ya tiene una factura activa (ID: " + pedidoAFacturar.getFactura().getId() + ").");
        }

        // Validar que el pedido esté en un estado facturable (ej. ENTREGADO o PAGADO si tuvieras ese estado)
        if (pedidoAFacturar.getEstado() != Estado.ENTREGADO) {
            // Ajusta esta lógica según tus estados de pedido.
            // Quizás un pedido en PREPARACION o PENDIENTE también se puede facturar si es pago adelantado.
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " no está en un estado que permita la facturación (Estado actual: " + pedidoAFacturar.getEstado() + "). Debe estar ENTREGADO.");
        }

        if (pedidoAFacturar.getDetalles() == null || pedidoAFacturar.getDetalles().isEmpty()) {
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " no tiene detalles y no puede ser facturado.");
        }

        Factura nuevaFactura = new Factura(); // Constructor setea fecha y estado ACTIVA
        nuevaFactura.setPedido(pedidoAFacturar);
        nuevaFactura.setFormaPago(pedidoAFacturar.getFormaPago());
        nuevaFactura.setTotalVenta(pedidoAFacturar.getTotal()); // El total ya está calculado en el pedido

        // Setear datos de MercadoPago si vienen en el DTO
        nuevaFactura.setMpPaymentId(dto.getMpPaymentId());
        nuevaFactura.setMpMerchantOrderId(dto.getMpMerchantOrderId());
        nuevaFactura.setMpPreferenceId(dto.getMpPreferenceId());
        nuevaFactura.setMpPaymentType(dto.getMpPaymentType());

        for (DetallePedido detallePedido : pedidoAFacturar.getDetalles()) {
            Articulo articuloDelPedido = detallePedido.getArticulo();
            if (articuloDelPedido == null) {
                throw new Exception("Error de datos: Artículo no encontrado para el DetallePedido ID: " + detallePedido.getId());
            }

            FacturaDetalle facturaDetalle = new FacturaDetalle();
            facturaDetalle.setCantidad(detallePedido.getCantidad());
            facturaDetalle.setDenominacionArticulo(articuloDelPedido.getDenominacion()); // Foto del nombre
            facturaDetalle.setPrecioUnitarioArticulo(articuloDelPedido.getPrecioVenta()); // Foto del precio
            facturaDetalle.setSubTotal(detallePedido.getSubTotal()); // Ya calculado en DetallePedido
            facturaDetalle.setArticulo(articuloDelPedido); // Referencia al artículo original (opcional)

            nuevaFactura.addDetalleFactura(facturaDetalle); // El helper establece la relación bidireccional
        }

        Factura facturaGuardada = facturaRepository.save(nuevaFactura);

        // Actualizar el estado del pedido a FACTURADO
        pedidoAFacturar.setFactura(facturaGuardada);
        // Si tienes el estado FACTURADO en tu Enum Estado de Pedido:
        // pedidoAFacturar.setEstado(Estado.FACTURADO);
        pedidoRepository.save(pedidoAFacturar);

        return facturaGuardada;
    }

    @Override
    @Transactional
    public Factura anularFactura(Integer id) throws Exception {
        Factura facturaAAnular = findByIdIncludingAnuladas(id);

        if (facturaAAnular.getEstadoFactura() == EstadoFactura.ANULADA) {
            throw new Exception("La factura con ID " + id + " ya se encuentra anulada.");
        }
        // Aquí podrías añadir lógica de negocio, ej. no anular facturas muy antiguas, etc.

        facturaAAnular.setEstadoFactura(EstadoFactura.ANULADA);
        facturaAAnular.setFechaAnulacion(LocalDate.now());

        // Desvincular del pedido (el pedido sigue existiendo, pero sin esta factura activa)
        // Pedido pedidoAsociado = facturaAAnular.getPedido();
        // if (pedidoAsociado != null) {
        //     pedidoAsociado.setFactura(null); // Opcional, depende de cómo quieras manejar la relación
        //     // También podrías querer cambiar el estado del pedido a uno que refleje la anulación de la factura.
        //     pedidoRepository.save(pedidoAsociado);
        // }
        return facturaRepository.save(facturaAAnular);
    }
}