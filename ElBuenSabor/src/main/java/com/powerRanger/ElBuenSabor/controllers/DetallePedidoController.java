/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.DetallePedido;
import com.powerRanger.ElBuenSabor.service.DetallePedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/detallepedidos")
public class DetallePedidoController {

    @Autowired
    private DetallePedidoService detallePedidoService;

    // Obtener todos los detalles de pedidos
    @GetMapping
    public List<DetallePedido> getAllDetallePedidos() {
        return detallePedidoService.getAllDetallePedidos();
    }

    // Obtener un detalle de pedido por ID
    @GetMapping("/{id}")
    public DetallePedido getDetallePedidoById(@PathVariable Integer id) {
        return detallePedidoService.getDetallePedidoById(id);
    }

    // Crear un nuevo detalle de pedido
    @PostMapping
    public DetallePedido createDetallePedido(@RequestBody DetallePedido detallePedido) {
        return detallePedidoService.createDetallePedido(detallePedido);
    }

    // Actualizar un detalle de pedido
    @PutMapping("/{id}")
    public DetallePedido updateDetallePedido(@PathVariable Integer id, @RequestBody DetallePedido detallePedido) {
        return detallePedidoService.updateDetallePedido(id, detallePedido);
    }

    // Eliminar un detalle de pedido
    @DeleteMapping("/{id}")
    public void deleteDetallePedido(@PathVariable Integer id) {
        detallePedidoService.deleteDetallePedido(id);
    }
}
