package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO; // Importar DTO de respuesta
import jakarta.validation.Valid;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResponseDTO> getAll();
    UsuarioResponseDTO getById(Integer id) throws Exception;
    UsuarioResponseDTO getByUsername(String username) throws Exception;
    UsuarioResponseDTO getByAuth0Id(String auth0Id) throws Exception; // Este podría seguir devolviendo la entidad si es para uso interno de seguridad

    UsuarioResponseDTO create(@Valid UsuarioRequestDTO dto) throws Exception;
    UsuarioResponseDTO update(Integer id, @Valid UsuarioRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;

    // findOrCreateUsuario devuelve la entidad porque es para la lógica interna de seguridad,
    // no directamente para una respuesta de controlador.
    com.powerRanger.ElBuenSabor.entities.Usuario findOrCreateUsuario(String auth0Id, String username, String email) throws Exception;
}