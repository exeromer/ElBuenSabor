package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface UsuarioService extends BaseService<Usuario, Integer> {
    List<UsuarioResponseDTO> findAllUsuarios(String searchTerm);
    UsuarioResponseDTO findUsuarioById(Integer id) throws Exception;
    UsuarioResponseDTO getByUsername(String username) throws Exception;
    UsuarioResponseDTO getByAuth0Id(String auth0Id) throws Exception;
    Optional<Usuario> findActualByAuth0Id(String auth0Id);
    UsuarioResponseDTO createUsuario(@Valid UsuarioRequestDTO dto) throws Exception;
    UsuarioResponseDTO updateUsuario(Integer id, @Valid UsuarioRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;
    Usuario findOrCreateUsuario(String auth0Id, String username, String email) throws Exception;
}