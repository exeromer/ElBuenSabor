package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<UsuarioResponseDTO> getAll(String searchTerm) { // Modificado
        List<Usuario> usuarios;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            usuarios = usuarioRepository.searchByUsernameActivos(searchTerm.trim());
            System.out.println("DEBUG: Buscando Usuarios con término: '" + searchTerm.trim() + "', Encontrados: " + usuarios.size());
        } else {
            usuarios = usuarioRepository.findByEstadoActivoTrue();
            System.out.println("DEBUG: Obteniendo todos los Usuarios activos, Encontrados: " + usuarios.size());
        }
        return usuarios.stream()
                .map(this::convertToResponseDto) // Asumiendo que tienes convertToResponseDto para Usuario
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
        return convertToResponseDto(usuario); // Asegúrate que convertToResponseDto está definido y es robusto
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
                if (!u.getId().equals(id)) {
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
        if (auth0Id == null || auth0Id.trim().isEmpty()) {
            throw new IllegalArgumentException("Auth0 ID no puede ser nulo o vacío.");
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByAuth0Id(auth0Id);

        if (optionalUsuario.isPresent()) {
            Usuario usuarioExistente = optionalUsuario.get();
            System.out.println("FIND_OR_CREATE: Usuario encontrado con auth0Id: " + auth0Id + ", username: " + usuarioExistente.getUsername());

            // Opcional: Lógica para actualizar username/email si han cambiado en Auth0
            // y si tu lógica de negocio lo permite y deseas sincronizarlo.
            // Por ejemplo:
            // boolean modificado = false;
            // if (username != null && !username.isEmpty() && !username.equals(usuarioExistente.getUsername())) {
            //     // Validar unicidad del nuevo username antes de cambiarlo si es necesario
            //     if (!usuarioRepository.findByUsername(username).filter(u -> !u.getAuth0Id().equals(auth0Id)).isPresent()) {
            //         usuarioExistente.setUsername(username);
            //         modificado = true;
            //     } else {
            //        System.out.println("FIND_OR_CREATE: Nuevo username " + username + " ya existe para otro usuario. No se actualiza.");
            //     }
            // }
            // if (modificado) {
            //     return usuarioRepository.save(usuarioExistente);
            // }
            return usuarioExistente;
        } else {
            System.out.println("FIND_OR_CREATE: Usuario NO encontrado con auth0Id: " + auth0Id + ". Creando nuevo usuario.");

            String finalUsername = username;
            // Asegurar que el username no sea nulo y sea único
            if (finalUsername == null || finalUsername.trim().isEmpty()) {
                // Generar un username si no se proveyó uno válido (ej. a partir del email o auth0Id)
                if (email != null && !email.isEmpty() && email.contains("@")) {
                    finalUsername = email.split("@")[0];
                } else {
                    // Remueve caracteres no alfanuméricos de auth0Id para un username base
                    String baseAuth0Id = auth0Id.replaceAll("[^a-zA-Z0-9]", "");
                    finalUsername = "user_" + baseAuth0Id.substring(0, Math.min(15, baseAuth0Id.length())); // Limita longitud
                }
            }

            // Verificar unicidad del username y añadir sufijo si es necesario
            if (usuarioRepository.findByUsername(finalUsername).isPresent()) {
                String baseUsername = finalUsername;
                int count = 1;
                do {
                    finalUsername = baseUsername + "_" + count++;
                } while (usuarioRepository.findByUsername(finalUsername).isPresent());
                System.out.println("FIND_OR_CREATE: Username original '" + baseUsername + "' ya existía. Nuevo username generado: '" + finalUsername + "'");
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setAuth0Id(auth0Id);
            nuevoUsuario.setUsername(finalUsername);
            nuevoUsuario.setRol(Rol.CLIENTE); // Rol por defecto para nuevos usuarios
            nuevoUsuario.setEstadoActivo(true);
            // nuevoUsuario.setEmail(email); // Si tienes un campo email en tu entidad Usuario y quieres persistirlo

            System.out.println("FIND_OR_CREATE: Guardando nuevo usuario: auth0Id=" + auth0Id + ", username=" + finalUsername + ", rol=" + nuevoUsuario.getRol());
            try {
                Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
                System.out.println("FIND_OR_CREATE: Nuevo usuario guardado con ID de BD: " + usuarioGuardado.getId());
                return usuarioGuardado;
            } catch (DataIntegrityViolationException e) {
                // Esto podría ocurrir si, a pesar de las verificaciones, hay una condición de carrera
                // al insertar un username/auth0Id que justo se volvió no único.
                System.err.println("FIND_OR_CREATE_ERROR: DataIntegrityViolationException al guardar nuevo usuario para auth0Id: " + auth0Id + ". ¿Posible duplicado no detectado antes de save?");
                // Intentar buscar de nuevo por si acaso se creó en otra transacción concurrente
                return usuarioRepository.findByAuth0Id(auth0Id)
                        .orElseThrow(() -> new Exception("Error crítico: Falló la creación del usuario y no se encontró después de DataIntegrityViolationException para auth0Id: " + auth0Id, e));
            } catch (Exception e) {
                System.err.println("FIND_OR_CREATE_ERROR: Excepción general al guardar nuevo usuario para auth0Id: " + auth0Id);
                throw new Exception("Error al guardar el nuevo usuario: " + e.getMessage(), e);
            }
        }
    }
}