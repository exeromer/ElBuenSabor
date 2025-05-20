package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.Articulo;    // Necesario si validas el artículo
import com.powerRanger.ElBuenSabor.entities.DetallePedido; // ✅ Usa el nombre de entidad correcto
import com.powerRanger.ElBuenSabor.entities.Pedido;      // Necesario si validas el pedido
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.DetallePedidoRepository; // ✅ Usa el nombre de repo correcto
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;


import java.util.List;
// Optional no se usa aquí si arrojamos excepción

@Service
@Validated // Para que @Valid en los parámetros funcione
public class DetallePedidoServiceImpl implements DetallePedidoService { // Implementa la interfaz

    @Autowired
    private DetallePedidoRepository detallePedidoRepository; // ✅ Inyecta el Repositorio

    @Autowired
    private ArticuloRepository articuloRepository; // Para validar el artículo

    @Autowired
    private PedidoRepository pedidoRepository; // Para validar el pedido

    @Override
    @Transactional(readOnly = true)
    public List<DetallePedido> getAllDetallePedidos() {
        return detallePedidoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public DetallePedido getDetallePedidoById(Integer id) throws Exception {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new Exception("DetallePedido no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public DetallePedido createDetallePedido(@Valid DetallePedido detallePedido) throws Exception {
        // Validaciones importantes si este servicio se usa para crear detalles independientemente:
        if (detallePedido.getArticulo() == null || detallePedido.getArticulo().getId() == null) {
            throw new Exception("El Artículo es obligatorio para el detalle.");
        }
        Articulo articulo = articuloRepository.findById(detallePedido.getArticulo().getId())
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + detallePedido.getArticulo().getId()));
        detallePedido.setArticulo(articulo); // Asocia la entidad gestionada

        if (detallePedido.getPedido() == null || detallePedido.getPedido().getId() == null) {
            throw new Exception("El Pedido es obligatorio para el detalle.");
        }
        Pedido pedido = pedidoRepository.findById(detallePedido.getPedido().getId())
                .orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + detallePedido.getPedido().getId()));
        detallePedido.setPedido(pedido); // Asocia la entidad gestionada

        // Calcular subtotal si no viene calculado o para asegurar consistencia
        if (articulo.getPrecioVenta() != null && detallePedido.getCantidad() != null) {
            detallePedido.setSubTotal(articulo.getPrecioVenta() * detallePedido.getCantidad());
        } else {
            throw new Exception("No se puede calcular el subtotal: falta precio de venta o cantidad.");
        }

        return detallePedidoRepository.save(detallePedido);
    }

    @Override
    @Transactional
    public DetallePedido updateDetallePedido(Integer id, @Valid DetallePedido detallePedidoDetails) throws Exception {
        DetallePedido detalleExistente = getDetallePedidoById(id); // Verifica si existe

        // Actualizar campos permitidos (usualmente cantidad, quizás artículo si la lógica lo permite)
        detalleExistente.setCantidad(detallePedidoDetails.getCantidad());

        if (detallePedidoDetails.getArticulo() != null && detallePedidoDetails.getArticulo().getId() != null) {
            Articulo articulo = articuloRepository.findById(detallePedidoDetails.getArticulo().getId())
                    .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + detallePedidoDetails.getArticulo().getId()));
            detalleExistente.setArticulo(articulo);
        } else {
            throw new Exception("El Artículo es obligatorio para el detalle.");
        }

        // Recalcular subtotal
        if (detalleExistente.getArticulo().getPrecioVenta() != null && detalleExistente.getCantidad() != null) {
            detalleExistente.setSubTotal(detalleExistente.getArticulo().getPrecioVenta() * detalleExistente.getCantidad());
        } else {
            throw new Exception("No se puede calcular el subtotal: falta precio de venta o cantidad.");
        }

        // Normalmente el Pedido de un DetallePedido no se cambia una vez creado.

        return detallePedidoRepository.save(detalleExistente);
    }

    @Override
    @Transactional
    public void deleteDetallePedido(Integer id) throws Exception {
        if (!detallePedidoRepository.existsById(id)) {
            throw new Exception("DetallePedido no encontrado con ID: " + id + " para eliminar.");
        }
        // Importante: Si borras un detalle individualmente, considera si necesitas
        // actualizar el total del Pedido padre. Esto se complica si los detalles
        // se gestionan individualmente en lugar de a través del Pedido.
        detallePedidoRepository.deleteById(id);
    }
}