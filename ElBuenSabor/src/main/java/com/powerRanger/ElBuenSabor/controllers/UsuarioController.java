package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    // Solo un ADMIN puede crear usuarios manualmente
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> createUsuario(@Valid @RequestBody UsuarioRequestDTO dto) throws Exception {
        UsuarioResponseDTO nuevoUsuarioDto = usuarioService.createUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuarioDto);
    }

    @GetMapping
    // Solo un ADMIN puede ver la lista de todos los usuarios
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuarios(@RequestParam(name = "searchTerm", required = false) String searchTerm) throws Exception {
        List<UsuarioResponseDTO> usuarios = usuarioService.findAllUsuarios(searchTerm);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    // Un ADMIN puede ver cualquier usuario, o un usuario puede verse a sí mismo (requiere lógica más compleja aquí)
    // Por ahora, lo dejamos solo para ADMIN para simplificar.
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioById(@PathVariable Integer id) throws Exception {
        return ResponseEntity.ok(usuarioService.findUsuarioById(id));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioByUsername(@PathVariable String username) throws Exception {
        return ResponseEntity.ok(usuarioService.getByUsername(username));
    }

    @GetMapping("/auth0/{auth0Id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (isAuthenticated() and #auth0Id == principal.claims['sub'])")
    public ResponseEntity<UsuarioResponseDTO> getUsuarioByAuth0Id(@PathVariable String auth0Id) throws Exception {
        UsuarioResponseDTO usuarioDto = usuarioService.getByAuth0Id(auth0Id);
        return ResponseEntity.ok(usuarioDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> updateUsuario(@PathVariable Integer id, @Valid @RequestBody UsuarioRequestDTO dto) throws Exception {
        UsuarioResponseDTO usuarioActualizadoDto = usuarioService.updateUsuario(id, dto);
        return ResponseEntity.ok(usuarioActualizadoDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> softDeleteUsuario(@PathVariable Integer id) throws Exception {
        usuarioService.softDelete(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario con ID " + id + " marcado como inactivo (borrado lógico).");
        return ResponseEntity.ok(response);
    }
}