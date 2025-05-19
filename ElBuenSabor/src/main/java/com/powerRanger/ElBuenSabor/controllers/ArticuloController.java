/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.service.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulos")
public class ArticuloController {

    @Autowired
    private ArticuloService articuloService;

    // Obtener todos los artículos
    @GetMapping
    public List<Articulo> getAllArticulos() {
        return articuloService.getAllArticulos();
    }

    // Obtener un artículo por ID
    @GetMapping("/{id}")
    public Articulo getArticuloById(@PathVariable Integer id) {
        return articuloService.getArticuloById(id);
    }

    // Crear un nuevo artículo
    @PostMapping
    public Articulo createArticulo(@RequestBody Articulo articulo) {
        return articuloService.createArticulo(articulo);
    }

    // Actualizar un artículo
    @PutMapping("/{id}")
    public Articulo updateArticulo(@PathVariable Integer id, @RequestBody Articulo articulo) {
        return articuloService.updateArticulo(id, articulo);
    }

    // Eliminar un artículo
    @DeleteMapping("/{id}")
    public void deleteArticulo(@PathVariable Integer id) {
        articuloService.deleteArticulo(id);
    }
}

