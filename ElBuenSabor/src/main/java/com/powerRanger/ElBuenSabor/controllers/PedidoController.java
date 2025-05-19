/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Obtener todos los pedidos
    @GetMapping
    public List<Pedido> getAllPedidos() {
        return pedidoService.getAllPedidos();
    }

    // Obtener un pedido por ID
    @GetMapping("/{id}")
    public Pedido getPedidoById(@PathVariable Integer id) {
        return pedidoService.getPedidoById(id);
    }

    // Crear un nuevo pedido
    @PostMapping
    public Pedido createPedido(@RequestBody Pedido pedido) {
        return pedidoService.createPedido(pedido);
    }

    // Actualizar un pedido
    @PutMapping("/{id}")
    public Pedido updatePedido(@PathVariable Integer id, @RequestBody Pedido pedido) {
        return pedidoService.updatePedido(id, pedido);
    }

    // Eliminar un pedido
    @DeleteMapping("/{id}")
    public void deletePedido(@PathVariable Integer id) {
        pedidoService.deletePedido(id);
    }
}

