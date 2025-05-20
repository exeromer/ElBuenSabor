package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.entities.Factura;
import com.powerRanger.ElBuenSabor.entities.FacturaDetalle;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.entities.enums.EstadoFactura;
import com.powerRanger.ElBuenSabor.exceptions.InvalidOperationException;
import com.powerRanger.ElBuenSabor.exceptions.ResourceNotFoundException;
import com.powerRanger.ElBuenSabor.repository.FacturaRepository;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FacturaServiceImpl implements FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

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
    public Factura findByIdActiva(Integer id) {
        return facturaRepository.findByIdAndEstadoFactura(id, EstadoFactura.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Factura activa con ID " + id + " no encontrada."));
    }

    @Override
    @Transactional(readOnly = true)
    public Factura findByIdIncludingAnuladas(Integer id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura con ID " + id + " no encontrada."));
    }

    @Override
    @Transactional
    public Factura generarFacturaParaPedido(Integer pedidoId) throws Exception {
        Pedido pedidoAFacturar = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido con ID " + pedidoId + " no encontrado."));

        if (pedidoAFacturar.getFactura() != null && pedidoAFacturar.getFactura().getEstadoFactura() == EstadoFactura.ACTIVA) {
            throw new InvalidOperationException("El pedido con ID " + pedidoId + " ya tiene una factura activa (ID: " + pedidoAFacturar.getFactura().getId() + ").");
        }
        if (pedidoAFacturar.getDetalles() == null || pedidoAFacturar.getDetalles().isEmpty()) {
            throw new InvalidOperationException("El pedido con ID " + pedidoId + " no tiene detalles y no puede ser facturado.");
        }

        Factura nuevaFactura = new Factura(); // estadoFactura se setea a ACTIVA y fechaFacturacion a now() por defecto en la entidad
        nuevaFactura.setPedido(pedidoAFacturar);
        nuevaFactura.setFormaPago(pedidoAFacturar.getFormaPago());
        // Establecer otros datos de la factura si es necesario (ej. mpPaymentId si se conocen en este punto)


        double totalGeneralFactura = 0.0;

        for (DetallePedido detallePedidoItem : pedidoAFacturar.getDetalles()) {
            Articulo articuloDelPedido = detallePedidoItem.getArticulo();
            if (articuloDelPedido == null) {
                throw new InvalidOperationException("Error de datos: Artículo no encontrado para el DetallePedido ID: " + detallePedidoItem.getId());
            }

            Integer cantidad = detallePedidoItem.getCantidad();
            String denominacionArticulo = articuloDelPedido.getDenominacion();
            Double precioUnitarioEnFactura = articuloDelPedido.getPrecioVenta(); // Precio al momento de facturar
            Double subTotalLinea = cantidad * precioUnitarioEnFactura;

            FacturaDetalle facturaDetalleItem = new FacturaDetalle(
                    cantidad,
                    denominacionArticulo,
                    precioUnitarioEnFactura,
                    subTotalLinea,
                    nuevaFactura, // La relación se establece aquí y al añadir a la lista de la factura
                    articuloDelPedido
            );
            nuevaFactura.addDetalleFactura(facturaDetalleItem); // El método addDetalleFactura se encarga de facturaDetalleItem.setFactura(nuevaFactura)
            totalGeneralFactura += subTotalLinea;
        }

        nuevaFactura.setTotalVenta(totalGeneralFactura);
        Factura facturaGuardada = facturaRepository.save(nuevaFactura);

        pedidoAFacturar.setFactura(facturaGuardada);
        pedidoRepository.save(pedidoAFacturar);

        return facturaGuardada;
    }

    @Override
    @Transactional
    public Factura saveManualFactura(Factura factura) {
        if (factura.getId() == null) { // Es una nueva factura
            factura.setEstadoFactura(EstadoFactura.ACTIVA);
            // La entidad Factura ya setea fechaFacturacion a LocalDate.now() en su constructor por defecto.
            // Si se recibe una fecha en el 'factura' DTO/entidad, se usará esa.
        } else {
            // Si se permite "actualizar" una factura existente a través de este endpoint de "creación/actualización manual"
            Factura existente = facturaRepository.findById(factura.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Factura con ID " + factura.getId() + " no encontrada para actualizar."));
            if (existente.getEstadoFactura() == EstadoFactura.ANULADA && factura.getEstadoFactura() == EstadoFactura.ACTIVA) {
                throw new InvalidOperationException("No se puede reactivar una factura anulada de esta manera. ID: " + factura.getId());
            }
            if (existente.getEstadoFactura() == EstadoFactura.ANULADA && factura.getEstadoFactura() == EstadoFactura.ANULADA) {
                // No hay mucho que hacer si se "actualiza" una factura anulada con los mismos datos de anulación
            } else if (existente.getEstadoFactura() == EstadoFactura.ACTIVA && factura.getEstadoFactura() == EstadoFactura.ANULADA) {
                throw new InvalidOperationException("Para anular una factura, por favor use el endpoint/método de anulación. ID: " + factura.getId());
            }
            // Aquí se copiarían los campos actualizables de 'factura' a 'existente'
            // Por ahora, este método se enfoca en que si tiene ID, existe.
            // La lógica de qué campos se actualizan es compleja y depende del negocio.
            // Este método es más para CREAR con un objeto ya formado.
        }

        double totalCalculado = 0.0;
        boolean tieneDetallesValidos = false;
        if (factura.getDetallesFactura() != null && !factura.getDetallesFactura().isEmpty()) {
            tieneDetallesValidos = true;
            for (FacturaDetalle detalle : factura.getDetallesFactura()) {
                detalle.setFactura(factura);

                if (detalle.getCantidad() == null || detalle.getPrecioUnitarioArticulo() == null) {
                    throw new InvalidOperationException("Cada detalle de factura debe tener cantidad y precio unitario. Detalle problemático podría ser: " + detalle.getDenominacionArticulo());
                }
                // Calculamos o recalculamos el subtotal del detalle para asegurar consistencia
                detalle.setSubTotal(detalle.getCantidad() * detalle.getPrecioUnitarioArticulo());
                totalCalculado += detalle.getSubTotal();
            }
        }

        // Si es una nueva factura (sin ID) y no tiene detalles, es un error.
        if (factura.getId() == null && !tieneDetallesValidos) {
            throw new InvalidOperationException("Una nueva factura manual debe incluir detalles válidos.");
        }

        // Si no hay detalles, pero la factura ya existe (tiene ID), no recalculamos el total a partir de detalles.
        // Se asume que el totalVenta que trae es el correcto o no se modifica.
        // Si hay detalles, el total se recalcula.
        if (tieneDetallesValidos) {
            factura.setTotalVenta(totalCalculado);
        } else if (factura.getId() == null) { // Nueva factura sin detalles (ya cubierto por el throw anterior, pero por claridad)
            throw new InvalidOperationException("Nueva factura manual sin detalles no puede tener total 0 a menos que se especifique explícitamente y sea permitido.");
        }
        // Si es una factura existente sin detalles en el payload, mantenemos su totalVenta original
        // a menos que la lógica de negocio dicte otra cosa.

        return facturaRepository.save(factura);
    }

    @Override
    @Transactional
    public Factura anularFactura(Integer id) throws Exception {
        Factura facturaAAnular = findByIdIncludingAnuladas(id); // Permite buscarla aunque ya esté anulada (para evitar error de no encontrada)

        if (facturaAAnular.getEstadoFactura() == EstadoFactura.ANULADA) {
            throw new InvalidOperationException("La factura con ID " + id + " ya se encuentra anulada.");
        }

        facturaAAnular.setEstadoFactura(EstadoFactura.ANULADA);
        facturaAAnular.setFechaAnulacion(LocalDate.now());

        Pedido pedidoAsociado = facturaAAnular.getPedido();
        if (pedidoAsociado != null) {
            if (pedidoAsociado.getFactura() != null && pedidoAsociado.getFactura().getId().equals(facturaAAnular.getId())) {
                pedidoAsociado.setFactura(null);
                pedidoRepository.save(pedidoAsociado);
            }
        }
        return facturaRepository.save(facturaAAnular);
    }
}