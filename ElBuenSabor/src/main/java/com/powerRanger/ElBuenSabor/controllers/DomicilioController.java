/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.service.DomicilioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/domicilios")
public class DomicilioController {

    @Autowired
    private DomicilioService domicilioService;

    // Obtener todos los domicilios
    @GetMapping
    public List<Domicilio> getAllDomicilios() {
        return domicilioService.getAllDomicilios();
    }

    // Obtener un domicilio por ID
    @GetMapping("/{id}")
    public Domicilio getDomicilioById(@PathVariable Integer id) {
        return domicilioService.getDomicilioById(id);
    }

    // Crear un nuevo domicilio
    @PostMapping
    public Domicilio createDomicilio(@RequestBody Domicilio domicilio) {
        return domicilioService.createDomicilio(domicilio);
    }

    // Actualizar un domicilio
    @PutMapping("/{id}")
    public Domicilio updateDomicilio(@PathVariable Integer id, @RequestBody Domicilio domicilio) {
        return domicilioService.updateDomicilio(id, domicilio);
    }

    // Eliminar un domicilio
    @DeleteMapping("/{id}")
    public void deleteDomicilio(@PathVariable Integer id) {
        domicilioService.deleteDomicilio(id);
    }
}
