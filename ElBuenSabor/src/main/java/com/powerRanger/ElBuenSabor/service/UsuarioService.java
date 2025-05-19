package com.powerRanger.ElBuenSabor.service;

import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Obtener todos los usuarios
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    // Obtener un usuario por ID
    public Usuario getUsuarioById(Integer id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null);  // Retorna null si no se encuentra
    }

    // Crear un nuevo usuario
    public Usuario createUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Actualizar un usuario
    public Usuario updateUsuario(Integer id, Usuario usuario) {
        if (usuarioRepository.existsById(id)) {
            return usuarioRepository.save(usuario);
        }
        return null;
    }

    // Eliminar un usuario
    public void deleteUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
