package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpleadoResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.UsuarioResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import com.powerRanger.ElBuenSabor.repository.EmpleadoRepository;
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
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private EmpleadoRepository  empleadoRepository;

    private UsuarioResponseDTO convertToResponseDto(Usuario usuario) {
        if (usuario == null) return null;
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setAuth0Id(usuario.getAuth0Id());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setEstadoActivo(usuario.getEstadoActivo());
        dto.setFechaBaja(usuario.getFechaBaja());
        if (usuario.getRol() == Rol.EMPLEADO) {
            empleadoRepository.findByUsuarioId(usuario.getId()).ifPresent(empleado -> {
                EmpleadoResponseDTO empleadoDto = new EmpleadoResponseDTO();
                empleadoDto.setId(empleado.getId());
                empleadoDto.setNombre(empleado.getNombre());
                empleadoDto.setApellido(empleado.getApellido());
                empleadoDto.setTelefono(empleado.getTelefono());
                empleadoDto.setRolEmpleado(empleado.getRolEmpleado());
                empleadoDto.voidSetUsuarioId(empleado.getUsuario().getId());
                empleadoDto.setUsernameUsuario(empleado.getUsuario().getUsername());
                empleadoDto.setEstadoActivo(empleado.getEstadoActivo());
                empleadoDto.setFechaBaja(empleado.getFechaBaja());

                dto.setEmpleado(empleadoDto);
            });
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getAll(String searchTerm) {
        List<Usuario> usuarios;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            usuarios = usuarioRepository.searchByUsername(searchTerm.trim());
            System.out.println("DEBUG: Buscando en TODOS los Usuarios con término: '" + searchTerm.trim() + "', Encontrados: " + usuarios.size());
        } else {
            usuarios = usuarioRepository.findAll();
            System.out.println("DEBUG: Obteniendo TODOS los Usuarios, Encontrados: " + usuarios.size());
        }
        return usuarios.stream()
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
        if (dto.getAuth0Id() != null && usuarioRepository.findByAuth0Id(dto.getAuth0Id()).isPresent()) {
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

        if (!usuarioExistente.getUsername().equals(dto.getUsername())) {
            usuarioRepository.findByUsername(dto.getUsername()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw new RuntimeException("El username '" + dto.getUsername() + "' ya está en uso por otro usuario.");
                }
            });
        }

        usuarioExistente.setUsername(dto.getUsername());
        usuarioExistente.setRol(dto.getRol());
        usuarioExistente.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : usuarioExistente.getEstadoActivo());

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
    public Usuario findOrCreateUsuario(String auth0Id, String username, String email, List<String> rolesFromToken) throws Exception {
        if (auth0Id == null || auth0Id.trim().isEmpty()) {
            throw new IllegalArgumentException("Auth0 ID no puede ser nulo o vacío.");
        }

        Optional<Usuario> optionalUsuario = usuarioRepository.findByAuth0Id(auth0Id);

        if (optionalUsuario.isPresent()) {
            Usuario usuarioExistente = optionalUsuario.get();
            System.out.println("FIND_OR_CREATE: Usuario encontrado con auth0Id: " + auth0Id + ", username: " + usuarioExistente.getUsername());

            // Lógica de sincronización de rol
            if (rolesFromToken != null && !rolesFromToken.isEmpty()) {
                try {
                    Rol rolDesdeToken = Rol.valueOf(rolesFromToken.get(0).toUpperCase());
                    if (usuarioExistente.getRol() != rolDesdeToken) {
                        System.out.println("FIND_OR_CREATE (Sync): Cambiando rol de " + usuarioExistente.getRol() + " a " + rolDesdeToken);
                        usuarioExistente.setRol(rolDesdeToken);
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("FIND_OR_CREATE (Sync): Rol en token '" + rolesFromToken.get(0) + "' no es válido.");
                }
            }
            return usuarioExistente;
        }
            System.out.println("FIND_OR_CREATE: Usuario NO encontrado con auth0Id: " + auth0Id + ". Creando nuevo usuario.");

        String finalUsername = username;
        if (finalUsername == null || finalUsername.trim().isEmpty()) {
            finalUsername = email != null && email.contains("@") ? email.split("@")[0] : "user_" + auth0Id.replaceAll("[^a-zA-Z0-9]", "");
        }
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
            //nuevoUsuario.setRol(Rol.CLIENTE); // Rol por defecto para nuevos usuarios
            nuevoUsuario.setEstadoActivo(true);
            // nuevoUsuario.setEmail(email); // Si tienes un campo email en tu entidad Usuario y quieres persistirlo

            System.out.println("FIND_OR_CREATE: Guardando nuevo usuario: auth0Id=" + auth0Id + ", username=" + finalUsername + ", rol=" + nuevoUsuario.getRol());
        if (rolesFromToken != null && !rolesFromToken.isEmpty()) {
            try {
                Rol rolDesdeToken = Rol.valueOf(rolesFromToken.get(0).toUpperCase());
                nuevoUsuario.setRol(rolDesdeToken);
                System.out.println("FIND_OR_CREATE (New): Rol asignado desde token: " + rolDesdeToken);
            } catch (IllegalArgumentException e) {
                System.err.println("FIND_OR_CREATE (New): Rol '" + rolesFromToken.get(0) + "' en token no válido. Asignando por defecto CLIENTE.");
                nuevoUsuario.setRol(Rol.CLIENTE);
            }
        } else {
            System.out.println("FIND_OR_CREATE (New): No hay rol en token. Asignando por defecto CLIENTE.");
            nuevoUsuario.setRol(Rol.CLIENTE);
        }

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
        System.out.println("FIND_OR_CREATE: Nuevo usuario guardado con ID: " + usuarioGuardado.getId());

        // Si el rol es CLIENTE, llamamos al ClienteService para que cree la entidad Cliente.
        // Esto es necesario para que el usuario pueda operar en el sistema.
        if (usuarioGuardado.getRol() == Rol.CLIENTE) {
            // En lugar de llamar directamente al repositorio de Cliente, llamamos a su servicio.
            // Esto asume que tienes un método en ClienteService para crear un cliente a partir de un usuario.
            // Si no lo tienes, podemos crearlo o usar el repositorio aquí, pero usar el servicio es más limpio.
            clienteService.findOrCreateClienteByAuth0Id(usuarioGuardado.getAuth0Id(), email);
        }

        return usuarioGuardado;
    }
}