package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Obtener todos los pedidos
    public List<Pedido> getAllPedidos() {
        return pedidoRepository.findAll();
    }

    // Obtener un pedido por ID
    public Pedido getPedidoById(Integer id) {
        Optional<Pedido> pedido = pedidoRepository.findById(id);
        return pedido.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo pedido
    public Pedido createPedido(Pedido pedido) {
        return pedidoRepository.save(pedido);
    }

    // Actualizar un pedido
    public Pedido updatePedido(Integer id, Pedido pedido) {
        if (pedidoRepository.existsById(id)) {
            return pedidoRepository.save(pedido);
        }
        return null;
    }

    // Eliminar un pedido
    public void deletePedido(Integer id) {
        pedidoRepository.deleteById(id);
    }
}
