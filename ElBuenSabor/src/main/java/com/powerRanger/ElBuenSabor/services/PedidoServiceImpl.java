package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.DetallePedidoRequestDTO;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class PedidoServiceImpl implements PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private LocalTime parseTime(String timeString, String fieldName) throws Exception {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new Exception("El " + fieldName + " no puede estar vacío.");
        }
        try {
            return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e2){
                throw new Exception("Formato de " + fieldName + " inválido. Use HH:mm:ss o HH:mm. Valor recibido: " + timeString);
            }
        }
    }

    private Pedido mapAndPreparePedido(PedidoRequestDTO dto, Cliente cliente) throws Exception {
        Pedido pedido = new Pedido();

        pedido.setFechaPedido(LocalDate.now());
        pedido.setHoraEstimadaFinalizacion(parseTime(dto.getHoraEstimadaFinalizacion(), "hora estimada de finalización"));
        pedido.setTipoEnvio(dto.getTipoEnvio());
        pedido.setFormaPago(dto.getFormaPago());
        pedido.setEstado(Estado.PENDIENTE);
        pedido.setEstadoActivo(true);
        pedido.setCliente(cliente);

        Domicilio domicilio = domicilioRepository.findById(dto.getDomicilioId())
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + dto.getDomicilioId()));
        pedido.setDomicilio(domicilio);

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + dto.getSucursalId()));
        pedido.setSucursal(sucursal);

        double totalPedido = 0.0;

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new Exception("El pedido debe contener al menos un detalle.");
        }

        for (DetallePedidoRequestDTO detalleDto : dto.getDetalles()) {
            Articulo articulo = articuloRepository.findById(detalleDto.getArticuloId())
                    .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + detalleDto.getArticuloId()));

            if (Boolean.FALSE.equals(articulo.getEstadoActivo())) {
                throw new Exception("El artículo '" + articulo.getDenominacion() + "' (ID: " + articulo.getId() + ") no está disponible.");
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setArticulo(articulo);
            detalle.setCantidad(detalleDto.getCantidad());

            if (articulo.getPrecioVenta() == null) {
                throw new Exception("El artículo " + articulo.getDenominacion() + " (ID: " + articulo.getId() + ") no tiene un precio de venta asignado.");
            }
            double subTotal = articulo.getPrecioVenta() * detalleDto.getCantidad();
            detalle.setSubTotal(subTotal);
            totalPedido += subTotal;

            pedido.addDetalle(detalle);
        }

        pedido.setTotal(totalPedido);
        // pedido.setTotalCosto(totalCostoPedido); // Implementar cálculo de costo si es necesario

        return pedido;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> getAll() {
        return pedidoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido getById(Integer id) throws Exception {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> getPedidosByClienteId(Integer clienteId) throws Exception {
        if (!clienteRepository.existsById(clienteId)) {
            throw new Exception("Cliente no encontrado con ID: " + clienteId);
        }
        return pedidoRepository.findByClienteIdAndEstadoActivoTrueOrderByFechaPedidoDesc(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> getPedidosByClienteAuth0Id(String auth0Id) throws Exception {
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()) // ✅ Usa el método añadido
                .orElseThrow(() -> new Exception("Cliente no encontrado para el usuario " + usuario.getUsername()));
        return pedidoRepository.findByClienteIdAndEstadoActivoTrueOrderByFechaPedidoDesc(cliente.getId());
    }

    @Override
    @Transactional
    public Pedido create(@Valid PedidoRequestDTO dto) throws Exception {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + dto.getClienteId()));
        Pedido pedido = mapAndPreparePedido(dto, cliente);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido createForAuthenticatedClient(String auth0Id, @Valid PedidoRequestDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario autenticado (Auth0 ID: " + auth0Id + ") no encontrado en el sistema."));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()) // ✅ Usa el método añadido
                .orElseThrow(() -> new Exception("No se encontró un perfil de Cliente para el usuario: " + usuario.getUsername()));

        Pedido pedido = mapAndPreparePedido(dto, cliente);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido updateEstado(Integer id, Estado nuevoEstado) throws Exception {
        Pedido pedidoExistente = getById(id);

        // Lógica de transición de estados ajustada a los Enums existentes
        if (pedidoExistente.getEstado() == Estado.ENTREGADO) {
            if (nuevoEstado == Estado.CANCELADO) {
                throw new Exception("No se puede cancelar un pedido que ya fue entregado.");
            }
            if (nuevoEstado != Estado.ENTREGADO) {
                throw new Exception("Un pedido entregado no puede cambiar a estado: " + nuevoEstado);
            }
        }

        if (pedidoExistente.getEstado() == Estado.CANCELADO && nuevoEstado != Estado.CANCELADO) {
            throw new Exception("No se puede cambiar el estado de un pedido cancelado.");
        }

        // Ejemplo de lógica adicional:
        if (pedidoExistente.getEstado() == Estado.PENDIENTE && nuevoEstado == Estado.ENTREGADO) {
            throw new Exception("Un pedido pendiente debe pasar por preparación antes de ser entregado.");
        }

        pedidoExistente.setEstado(nuevoEstado);
        return pedidoRepository.save(pedidoExistente);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Pedido pedido = getById(id);

        if (pedido.getEstado() == Estado.ENTREGADO) {
            throw new Exception("No se puede eliminar (borrado lógico) un pedido que ya fue entregado.");
        }

        pedido.setEstadoActivo(false);
        pedido.setFechaBaja(LocalDate.now());
        if (pedido.getEstado() != Estado.CANCELADO && pedido.getEstado() != Estado.RECHAZADO && pedido.getEstado() != Estado.ENTREGADO) {
            pedido.setEstado(Estado.CANCELADO);
        }

        pedidoRepository.save(pedido);
    }
}