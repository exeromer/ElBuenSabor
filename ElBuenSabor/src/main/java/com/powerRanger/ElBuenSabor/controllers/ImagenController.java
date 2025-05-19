/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Imagen;
import com.powerRanger.ElBuenSabor.service.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/imagenes")
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    // Obtener todas las imágenes
    @GetMapping
    public List<Imagen> getAllImagenes() {
        return imagenService.getAllImagenes();
    }

    // Obtener una imagen por ID
    @GetMapping("/{id}")
    public Imagen getImagenById(@PathVariable Integer id) {
        return imagenService.getImagenById(id);
    }

    // Crear una nueva imagen
    @PostMapping
    public Imagen createImagen(@RequestBody Imagen imagen) {
        return imagenService.createImagen(imagen);
    }

    // Actualizar una imagen
    @PutMapping("/{id}")
    public Imagen updateImagen(@PathVariable Integer id, @RequestBody Imagen imagen) {
        return imagenService.updateImagen(id, imagen);
    }

    // Eliminar una imagen
    @DeleteMapping("/{id}")
    public void deleteImagen(@PathVariable Integer id) {
        imagenService.deleteImagen(id);
    }
}

