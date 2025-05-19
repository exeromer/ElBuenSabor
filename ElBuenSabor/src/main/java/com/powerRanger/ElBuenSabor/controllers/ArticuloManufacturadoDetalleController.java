/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturadoDetalle;
import com.powerRanger.ElBuenSabor.service.ArticuloManufacturadoDetalleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulosmanufacturadosdetalle")
public class ArticuloManufacturadoDetalleController {

    @Autowired
    private ArticuloManufacturadoDetalleService articuloManufacturadoDetalleService;

    // Obtener todos los detalles de artículos manufacturados
    @GetMapping
    public List<ArticuloManufacturadoDetalle> getAllArticuloManufacturadoDetalles() {
        return articuloManufacturadoDetalleService.getAllArticuloManufacturadoDetalles();
    }

    // Obtener un detalle de artículo manufacturado por ID
    @GetMapping("/{id}")
    public ArticuloManufacturadoDetalle getArticuloManufacturadoDetalleById(@PathVariable Integer id) {
        return articuloManufacturadoDetalleService.getArticuloManufacturadoDetalleById(id);
    }

    // Crear un nuevo detalle de artículo manufacturado
    @PostMapping
    public ArticuloManufacturadoDetalle createArticuloManufacturadoDetalle(@RequestBody ArticuloManufacturadoDetalle articuloManufacturadoDetalle) {
        return articuloManufacturadoDetalleService.createArticuloManufacturadoDetalle(articuloManufacturadoDetalle);
    }

    // Actualizar un detalle de artículo manufacturado
    @PutMapping("/{id}")
    public ArticuloManufacturadoDetalle updateArticuloManufacturadoDetalle(@PathVariable Integer id, @RequestBody ArticuloManufacturadoDetalle articuloManufacturadoDetalle) {
        return articuloManufacturadoDetalleService.updateArticuloManufacturadoDetalle(id, articuloManufacturadoDetalle);
    }

    // Eliminar un detalle de artículo manufacturado
    @DeleteMapping("/{id}")
    public void deleteArticuloManufacturadoDetalle(@PathVariable Integer id) {
        articuloManufacturadoDetalleService.deleteArticuloManufacturadoDetalle(id);
    }
}
