package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.repository.DetallePedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    // Obtener todos los detalles de pedidos
    public List<DetallePedido> getAllDetallePedidos() {
        return detallePedidoRepository.findAll();
    }

    // Obtener un detalle de pedido por ID
    public DetallePedido getDetallePedidoById(Integer id) {
        Optional<DetallePedido> detalle = detallePedidoRepository.findById(id);
        return detalle.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo detalle de pedido
    public DetallePedido createDetallePedido(DetallePedido detallePedido) {
        return detallePedidoRepository.save(detallePedido);
    }

    // Actualizar un detalle de pedido
    public DetallePedido updateDetallePedido(Integer id, DetallePedido detallePedido) {
        if (detallePedidoRepository.existsById(id)) {
            // No es necesario setear el ID manualmente
            return detallePedidoRepository.save(detallePedido);
        }
        return null;
    }

    // Eliminar un detalle de pedido
    public void deleteDetallePedido(Integer id) {
        detallePedidoRepository.deleteById(id);
    }
}
