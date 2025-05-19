/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    // Obtener todas las categorías
    @GetMapping
    public List<Categoria> getAllCategorias() {
        return categoriaService.getAllCategorias();
    }

    // Obtener una categoría por ID
    @GetMapping("/{id}")
    public Categoria getCategoriaById(@PathVariable Integer id) {
        return categoriaService.getCategoriaById(id);
    }

    // Crear una nueva categoría
    @PostMapping
    public Categoria createCategoria(@RequestBody Categoria categoria) {
        return categoriaService.createCategoria(categoria);
    }

    // Actualizar una categoría
    @PutMapping("/{id}")
    public Categoria updateCategoria(@PathVariable Integer id, @RequestBody Categoria categoria) {
        return categoriaService.updateCategoria(id, categoria);
    }

    // Eliminar una categoría
    @DeleteMapping("/{id}")
    public void deleteCategoria(@PathVariable Integer id) {
        categoriaService.deleteCategoria(id);
    }
}

