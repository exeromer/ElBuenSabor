package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO; // Importar DTO de respuesta
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método de Mapeo de Entidad a DTO de Respuesta
    private UsuarioResponseDTO convertToResponseDto(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setEstadoActivo(usuario.getEstadoActivo());
        dto.setFechaBaja(usuario.getFechaBaja());
        // No incluimos auth0Id en el DTO de respuesta general
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getAll() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO getById(Integer id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));
        return convertToResponseDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO getByUsername(String username) throws Exception {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Usuario no encontrado con username: " + username));
        return convertToResponseDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO getByAuth0Id(String auth0Id) throws Exception {
        // Este método podría ser llamado por el controlador para un endpoint específico
        // que sí devuelva el DTO. El findOrCreateUsuario devuelve la entidad para uso interno.
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id));
        return convertToResponseDto(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO create(@Valid UsuarioRequestDTO dto) throws Exception {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new Exception("El username '" + dto.getUsername() + "' ya está en uso.");
        }
        if (usuarioRepository.findByAuth0Id(dto.getAuth0Id()).isPresent()) {
            throw new Exception("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setAuth0Id(dto.getAuth0Id());
        usuario.setUsername(dto.getUsername());
        usuario.setRol(dto.getRol());
        usuario.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertToResponseDto(usuarioGuardado);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO update(Integer id, @Valid UsuarioRequestDTO dto) throws Exception {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));

        // Validar unicidad de username si cambia
        if (!usuarioExistente.getUsername().equals(dto.getUsername()) &&
                usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("El username '" + dto.getUsername() + "' ya está en uso por otro usuario.");
        }
        // Validar unicidad de auth0Id si cambia
        if (!usuarioExistente.getAuth0Id().equals(dto.getAuth0Id()) &&
                usuarioRepository.findByAuth0Id(dto.getAuth0Id()).isPresent()) {
            throw new RuntimeException("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado por otro usuario.");
        }

        usuarioExistente.setAuth0Id(dto.getAuth0Id());
        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setRol(dto.getRol());
        usuarioExistente.setEstadoActivo(dto.getEstadoActivo());

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return convertToResponseDto(usuarioActualizado);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + id));
        usuario.setEstadoActivo(false);
        usuario.setFechaBaja(LocalDate.now());
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario findOrCreateUsuario(String auth0Id, String username, String email) throws Exception {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByAuth0Id(auth0Id);

        if (optionalUsuario.isPresent()) {
            Usuario usuarioExistente = optionalUsuario.get();
            // Opcional: actualizar username o email si han cambiado en Auth0
            // y si tu lógica de negocio lo permite.
            // Por ahora, solo lo devolvemos.
            return usuarioExistente;
        } else {
            System.out.println("Creando nuevo usuario para auth0Id: " + auth0Id);

            String finalUsername = username;
            if (finalUsername == null || finalUsername.trim().isEmpty()) {
                finalUsername = (email != null && !email.isEmpty()) ? email : "user_" + auth0Id.replace("|", "_").substring(0, Math.min(10, auth0Id.length()));
            }

            // Asegurar unicidad del username generado
            if (usuarioRepository.findByUsername(finalUsername).isPresent()) {
                int i = 0;
                String baseUsername = finalUsername;
                do {
                    finalUsername = baseUsername + "_" + i++;
                } while (usuarioRepository.findByUsername(finalUsername).isPresent());
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setAuth0Id(auth0Id);
            nuevoUsuario.setUsername(finalUsername);
            nuevoUsuario.setRol(Rol.CLIENTE);
            nuevoUsuario.setEstadoActivo(true);
            return usuarioRepository.save(nuevoUsuario);
        }
    }
}