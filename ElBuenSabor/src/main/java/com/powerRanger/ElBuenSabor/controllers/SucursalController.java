/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Sucursal;
import com.powerRanger.ElBuenSabor.services.SucursalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sucursales")
public class SucursalController {

    @Autowired
    private SucursalService sucursalService;

    // Obtener todas las sucursales
    @GetMapping
    public List<Sucursal> getAllSucursales() {
        return sucursalService.getAllSucursales();
    }

    // Obtener una sucursal por ID
    @GetMapping("/{id}")
    public Sucursal getSucursalById(@PathVariable Integer id) {
        return sucursalService.getSucursalById(id);
    }

    // Crear una nueva sucursal
    @PostMapping
    public Sucursal createSucursal(@RequestBody Sucursal sucursal) {
        return sucursalService.createSucursal(sucursal);
    }

    // Actualizar una sucursal
    @PutMapping("/{id}")
    public Sucursal updateSucursal(@PathVariable Integer id, @RequestBody Sucursal sucursal) {
        return sucursalService.updateSucursal(id, sucursal);
    }

    // Eliminar una sucursal
    @DeleteMapping("/{id}")
    public void deleteSucursal(@PathVariable Integer id) {
        sucursalService.deleteSucursal(id);
    }
}

