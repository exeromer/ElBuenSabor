/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.service.ArticuloInsumoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulosinsumo")
public class ArticuloInsumoController {

    @Autowired
    private ArticuloInsumoService articuloInsumoService;

    // Obtener todos los artículos insumo
    @GetMapping
    public List<ArticuloInsumo> getAllArticuloInsumo() {
        return articuloInsumoService.getAllArticuloInsumo();
    }

    // Obtener un artículo insumo por ID
    @GetMapping("/{id}")
    public ArticuloInsumo getArticuloInsumoById(@PathVariable Integer id) {
        return articuloInsumoService.getArticuloInsumoById(id);
    }

    // Crear un nuevo artículo insumo
    @PostMapping
    public ArticuloInsumo createArticuloInsumo(@RequestBody ArticuloInsumo articuloInsumo) {
        return articuloInsumoService.createArticuloInsumo(articuloInsumo);
    }

    // Actualizar un artículo insumo
    @PutMapping("/{id}")
    public ArticuloInsumo updateArticuloInsumo(@PathVariable Integer id, @RequestBody ArticuloInsumo articuloInsumo) {
        return articuloInsumoService.updateArticuloInsumo(id, articuloInsumo);
    }

    // Eliminar un artículo insumo
    @DeleteMapping("/{id}")
    public void deleteArticuloInsumo(@PathVariable Integer id) {
        articuloInsumoService.deleteArticuloInsumo(id);
    }
}

