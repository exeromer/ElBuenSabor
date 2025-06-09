package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> createUsuario(@Valid @RequestBody UsuarioRequestDTO dto) throws Exception {
        UsuarioResponseDTO nuevoUsuarioDto = usuarioService.createUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuarioDto);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios(@RequestParam(name = "searchTerm", required = false) String searchTerm) throws Exception {
        List<UsuarioResponseDTO> usuarios = usuarioService.findAllUsuarios(searchTerm);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Integer id) throws Exception {
        return ResponseEntity.ok(usuarioService.findUsuarioById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioByUsername(@PathVariable String username) throws Exception {
        return ResponseEntity.ok(usuarioService.getByUsername(username));
    }

    @GetMapping("/auth0/{auth0Id}")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioByAuth0Id(@PathVariable String auth0Id) throws Exception {
        Usuario usuario = usuarioService.findOrCreateUsuario(auth0Id, null, null);
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setEstadoActivo(usuario.getEstadoActivo());
        dto.setFechaBaja(usuario.getFechaBaja());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(@PathVariable Integer id, @Valid @RequestBody UsuarioRequestDTO dto) throws Exception {
        UsuarioResponseDTO usuarioActualizadoDto = usuarioService.updateUsuario(id, dto);
        return ResponseEntity.ok(usuarioActualizadoDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteUsuario(@PathVariable Integer id) throws Exception {
        usuarioService.softDelete(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario con ID " + id + " marcado como inactivo (borrado lógico).");
        return ResponseEntity.ok(response);
    }
}