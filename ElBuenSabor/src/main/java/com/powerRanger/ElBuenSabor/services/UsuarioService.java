package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import jakarta.validation.Valid;
import java.util.List;

public interface UsuarioService {
    List<Usuario> getAll();
    Usuario getById(Integer id) throws Exception;
    Usuario getByUsername(String username) throws Exception;
    Usuario getByAuth0Id(String auth0Id) throws Exception;
    Usuario create(@Valid UsuarioRequestDTO dto) throws Exception;
    Usuario update(Integer id, @Valid UsuarioRequestDTO dto) throws Exception;
    void softDelete(Integer id) throws Exception;

    /**
     * Busca un usuario por su Auth0 ID. Si no existe, lo crea con un rol por defecto (CLIENTE).
     * Este método sería llamado por la capa de seguridad después de validar un token.
     * @param auth0Id El ID único del usuario proveniente de Auth0 (usualmente el 'sub' claim del token).
     * @param username Sugerencia de username (puede ser el email o nickname del token).
     * @param email Sugerencia de email (puede ser el email del token, si tu entidad Usuario lo tuviera).
     * @return El Usuario existente o recién creado.
     * @throws Exception Si ocurre un error durante la creación.
     */
    Usuario findOrCreateUsuario(String auth0Id, String username, String email) throws Exception;
}