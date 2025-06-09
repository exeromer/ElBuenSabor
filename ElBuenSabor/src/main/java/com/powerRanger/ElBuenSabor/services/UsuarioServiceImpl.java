package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
public class UsuarioServiceImpl extends BaseServiceImpl<Usuario, UsuarioRepository> implements UsuarioService {

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findAllUsuarios(String searchTerm) {
        List<Usuario> usuarios;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            usuarios = baseRepository.searchByUsernameActivos(searchTerm.trim());
        } else {
            usuarios = baseRepository.findByEstadoActivoTrue();
        }
        return usuarios.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO findUsuarioById(Integer id) throws Exception {
        return convertToResponseDto(super.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO getByUsername(String username) throws Exception {
        Usuario usuario = baseRepository.findByUsername(username)
                .orElseThrow(() -> new Exception("Usuario no encontrado con username: " + username));
        return convertToResponseDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO getByAuth0Id(String auth0Id) throws Exception {
        Usuario usuario = baseRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id));
        return convertToResponseDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findActualByAuth0Id(String auth0Id) {
        return baseRepository.findByAuth0Id(auth0Id);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO createUsuario(@Valid UsuarioRequestDTO dto) throws Exception {
        if (baseRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new Exception("El username '" + dto.getUsername() + "' ya está en uso.");
        }
        if (dto.getAuth0Id() != null && baseRepository.findByAuth0Id(dto.getAuth0Id()).isPresent()) {
            throw new Exception("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado.");
        }
        Usuario usuario = new Usuario();
        usuario.setAuth0Id(dto.getAuth0Id());
        usuario.setUsername(dto.getUsername());
        usuario.setRol(dto.getRol());
        usuario.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);
        return convertToResponseDto(super.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO updateUsuario(Integer id, @Valid UsuarioRequestDTO dto) throws Exception {
        Usuario usuarioExistente = super.findById(id);
        if (!usuarioExistente.getUsername().equals(dto.getUsername())) {
            baseRepository.findByUsername(dto.getUsername()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw new RuntimeException("El username '" + dto.getUsername() + "' ya está en uso por otro usuario.");
                }
            });
        }
        if (dto.getAuth0Id() != null && !dto.getAuth0Id().equals(usuarioExistente.getAuth0Id())) {
            baseRepository.findByAuth0Id(dto.getAuth0Id()).ifPresent(u -> {
                if(!u.getId().equals(id)){
                    throw new RuntimeException("El Auth0 ID '" + dto.getAuth0Id() + "' ya está registrado por otro usuario.");
                }
            });
        }
        usuarioExistente.setAuth0Id(dto.getAuth0Id());
        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setRol(dto.getRol());
        usuarioExistente.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : usuarioExistente.getEstadoActivo());
        return convertToResponseDto(super.update(id, usuarioExistente));
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Usuario usuario = super.findById(id);
        usuario.setEstadoActivo(false);
        usuario.setFechaBaja(LocalDate.now());
        super.save(usuario);
    }

    @Override
    @Transactional
    public Usuario findOrCreateUsuario(String auth0Id, String username, String email) throws Exception {
        if (auth0Id == null || auth0Id.trim().isEmpty()) {
            throw new IllegalArgumentException("Auth0 ID no puede ser nulo o vacío.");
        }
        Optional<Usuario> optionalUsuario = baseRepository.findByAuth0Id(auth0Id);
        if (optionalUsuario.isPresent()) {
            System.out.println("FIND_OR_CREATE: Usuario encontrado con auth0Id: " + auth0Id);
            return optionalUsuario.get();
        } else {
            System.out.println("FIND_OR_CREATE: Usuario NO encontrado con auth0Id: " + auth0Id + ". Creando nuevo usuario.");
            String finalUsername = username;
            if (finalUsername == null || finalUsername.trim().isEmpty()) {
                if (email != null && !email.isEmpty() && email.contains("@")) {
                    finalUsername = email.split("@")[0];
                } else {
                    String baseAuth0Id = auth0Id.replaceAll("[^a-zA-Z0-9]", "");
                    finalUsername = "user_" + baseAuth0Id.substring(0, Math.min(15, baseAuth0Id.length()));
                }
            }
            if (baseRepository.findByUsername(finalUsername).isPresent()) {
                String baseUsername = finalUsername;
                int count = 1;
                do {
                    finalUsername = baseUsername + "_" + count++;
                } while (baseRepository.findByUsername(finalUsername).isPresent());
                System.out.println("FIND_OR_CREATE: Username original '" + baseUsername + "' ya existía. Nuevo username generado: '" + finalUsername + "'");
            }
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setAuth0Id(auth0Id);
            nuevoUsuario.setUsername(finalUsername);
            nuevoUsuario.setRol(Rol.CLIENTE);
            nuevoUsuario.setEstadoActivo(true);
            try {
                return super.save(nuevoUsuario);
            } catch (DataIntegrityViolationException e) {
                System.err.println("FIND_OR_CREATE_ERROR: DataIntegrityViolationException al guardar nuevo usuario.");
                return baseRepository.findByAuth0Id(auth0Id)
                        .orElseThrow(() -> new Exception("Error crítico: Falló la creación del usuario para auth0Id: " + auth0Id, e));
            }
        }
    }

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
}