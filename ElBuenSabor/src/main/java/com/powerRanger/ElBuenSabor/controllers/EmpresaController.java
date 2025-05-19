/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Empresa;
import com.powerRanger.ElBuenSabor.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    // Obtener todas las empresas
    @GetMapping
    public List<Empresa> getAllEmpresas() {
        return empresaService.getAllEmpresas();
    }

    // Obtener una empresa por ID
    @GetMapping("/{id}")
    public Empresa getEmpresaById(@PathVariable Integer id) {
        return empresaService.getEmpresaById(id);
    }

    // Crear una nueva empresa
    @PostMapping
    public Empresa createEmpresa(@RequestBody Empresa empresa) {
        return empresaService.createEmpresa(empresa);
    }

    // Actualizar una empresa
    @PutMapping("/{id}")
    public Empresa updateEmpresa(@PathVariable Integer id, @RequestBody Empresa empresa) {
        return empresaService.updateEmpresa(id, empresa);
    }

    // Eliminar una empresa
    @DeleteMapping("/{id}")
    public void deleteEmpresa(@PathVariable Integer id) {
        empresaService.deleteEmpresa(id);
    }
}
