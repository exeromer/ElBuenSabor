/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.powerRanger.ElBuenSabor.controllers;

/**
 *
 * @author Hitman
 */
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Obtener todos los usuarios
    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.getAllUsuarios();
    }

    // Obtener un usuario por ID
    @GetMapping("/{id}")
    public Usuario getUsuarioById(@PathVariable Integer id) {
        return usuarioService.getUsuarioById(id);
    }

    // Crear un nuevo usuario
    @PostMapping
    public Usuario createUsuario(@RequestBody Usuario usuario) {
        return usuarioService.createUsuario(usuario);
    }

    // Actualizar un usuario
    @PutMapping("/{id}")
    public Usuario updateUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return usuarioService.updateUsuario(id, usuario);
    }

    // Eliminar un usuario
    @DeleteMapping("/{id}")
    public void deleteUsuario(@PathVariable Integer id) {
        usuarioService.deleteUsuario(id);
    }
}
