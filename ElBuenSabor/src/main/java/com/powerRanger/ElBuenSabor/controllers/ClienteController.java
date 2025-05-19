/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Obtener todos los clientes
    @GetMapping
    public List<Cliente> getAllClientes() {
        return clienteService.getAllClientes();
    }

    // Obtener un cliente por ID
    @GetMapping("/{id}")
    public Cliente getClienteById(@PathVariable Integer id) {
        return clienteService.getClienteById(id);
    }

    // Crear un nuevo cliente
    @PostMapping
    public Cliente createCliente(@RequestBody Cliente cliente) {
        return clienteService.createCliente(cliente);
    }

    // Actualizar un cliente
    @PutMapping("/{id}")
    public Cliente updateCliente(@PathVariable Integer id, @RequestBody Cliente cliente) {
        return clienteService.updateCliente(id, cliente);
    }

    // Eliminar un cliente
    @DeleteMapping("/{id}")
    public void deleteCliente(@PathVariable Integer id) {
        clienteService.deleteCliente(id);
    }
}

