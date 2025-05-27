package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario; // Importar la entidad Usuario
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional; // Importar Optional

public interface UsuarioService {
    List<UsuarioResponseDTO> getAll();
    UsuarioResponseDTO getById(Integer id) throws Exception;
    UsuarioResponseDTO getByUsername(String username) throws Exception;
    UsuarioResponseDTO getByAuth0Id(String auth0Id) throws Exception;

    // Nuevo método para obtener la entidad Usuario (o un Optional) por auth0Id para uso interno
    Optional<Usuario> findActualByAuth0Id(String auth0Id);

    UsuarioResponseDTO create(@Valid UsuarioRequestDTO dto) throws Exception;
    UsuarioResponseDTO update(Integer id, @Valid UsuarioRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;

    Usuario findOrCreateUsuario(String auth0Id, String username, String email) throws Exception;
}