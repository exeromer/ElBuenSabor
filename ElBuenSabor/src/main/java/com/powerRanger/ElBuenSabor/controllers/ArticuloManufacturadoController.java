/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import com.powerRanger.ElBuenSabor.service.ArticuloManufacturadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulosmanufacturados")
public class ArticuloManufacturadoController {

    @Autowired
    private ArticuloManufacturadoService articuloManufacturadoService;

    // Obtener todos los artículos manufacturados
    @GetMapping
    public List<ArticuloManufacturado> getAllArticuloManufacturados() {
        return articuloManufacturadoService.getAllArticuloManufacturados();
    }

    // Obtener un artículo manufacturado por ID
    @GetMapping("/{id}")
    public ArticuloManufacturado getArticuloManufacturadoById(@PathVariable Integer id) {
        return articuloManufacturadoService.getArticuloManufacturadoById(id);
    }

    // Crear un nuevo artículo manufacturado
    @PostMapping
    public ArticuloManufacturado createArticuloManufacturado(@RequestBody ArticuloManufacturado articuloManufacturado) {
        return articuloManufacturadoService.createArticuloManufacturado(articuloManufacturado);
    }

    // Actualizar un artículo manufacturado
    @PutMapping("/{id}")
    public ArticuloManufacturado updateArticuloManufacturado(@PathVariable Integer id, @RequestBody ArticuloManufacturado articuloManufacturado) {
        return articuloManufacturadoService.updateArticuloManufacturado(id, articuloManufacturado);
    }

    // Eliminar un artículo manufacturado
    @DeleteMapping("/{id}")
    public void deleteArticuloManufacturado(@PathVariable Integer id) {
        articuloManufacturadoService.deleteArticuloManufacturado(id);
    }
}

