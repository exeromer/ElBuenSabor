package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
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

    private UsuarioResponseDTO convertToResponseDto(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setEstadoActivo(usuario.getEstadoActivo());
        dto.setFechaBaja(usuario.getFechaBaja());
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
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id));
        return convertToResponseDto(usuario);
    }

    // Implementación del nuevo método
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findActualByAuth0Id(String auth0Id) {
        return usuarioRepository.findByAuth0Id(auth0Id);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO create(@Valid UsuarioRequestDTO dto) throws Exception {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new Exception("El username '" + dto.getUsername() + "' ya está en uso.");
        }
        // Asumiendo que el auth0Id debe ser único al crear directamente.
        if (dto.getAuth0Id() != null && usuarioRepository.findByAuth0Id(dto.getAuth0Id()).isPresent()) {
            throw new Exception("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setAuth0Id(dto.getAuth0Id()); // Puede ser nulo si la creación no siempre lo requiere aquí
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

        if (!usuarioExistente.getUsername().equals(dto.getUsername())) {
            usuarioRepository.findByUsername(dto.getUsername()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw new RuntimeException("El username '" + dto.getUsername() + "' ya está en uso por otro usuario.");
                }
            });
        }
        if (dto.getAuth0Id() != null && !dto.getAuth0Id().equals(usuarioExistente.getAuth0Id())) {
            usuarioRepository.findByAuth0Id(dto.getAuth0Id()).ifPresent(u -> {
                if(!u.getId().equals(id)){
                    throw new RuntimeException("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado por otro usuario.");
                }
            });
        }

        usuarioExistente.setAuth0Id(dto.getAuth0Id());
        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setRol(dto.getRol());
        usuarioExistente.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : usuarioExistente.getEstadoActivo());
        // No se actualiza fechaBaja aquí directamente, eso se maneja en softDelete.

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
            boolean modificado = false;
            if (username != null && !username.isEmpty() && !username.equals(usuarioExistente.getUsername())) {
                // Antes de cambiar el username, verificar si el nuevo ya existe para OTRO auth0Id
                Optional<Usuario> userWithNewUsername = usuarioRepository.findByUsername(username);
                if(userWithNewUsername.isPresent() && !userWithNewUsername.get().getAuth0Id().equals(auth0Id)){
                    // El nuevo username ya está tomado por otro usuario, manejar el conflicto
                    // Por ejemplo, podrías añadir un sufijo o lanzar una excepción.
                    // Aquí, para simplificar, no lo cambiamos si hay conflicto.
                    System.err.println("Intento de actualizar a username '" + username + "' que ya existe para otro usuario.");
                } else if (!userWithNewUsername.isPresent() || userWithNewUsername.get().getAuth0Id().equals(auth0Id)) {
                    usuarioExistente.setUsername(username);
                    modificado = true;
                }
            }
            // Lógica similar para el email si lo guardas y quieres sincronizarlo
            if (modificado) {
                return usuarioRepository.save(usuarioExistente);
            }
            return usuarioExistente;
        } else {
            String finalUsername = username;
            if (finalUsername == null || finalUsername.trim().isEmpty()) {
                finalUsername = (email != null && !email.isEmpty()) ? email.split("@")[0] : "user_" + auth0Id.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(10, auth0Id.length()));
            }

            if (usuarioRepository.findByUsername(finalUsername).isPresent()) {
                String baseUsername = finalUsername;
                int count = 1;
                do {
                    finalUsername = baseUsername + "_" + count++;
                } while (usuarioRepository.findByUsername(finalUsername).isPresent());
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setAuth0Id(auth0Id);
            nuevoUsuario.setUsername(finalUsername);
            nuevoUsuario.setRol(Rol.CLIENTE);
            nuevoUsuario.setEstadoActivo(true);
            // nuevoUsuario.setEmail(email); // Si tienes campo email en Usuario y lo quieres guardar
            return usuarioRepository.save(nuevoUsuario);
        }
    }
}