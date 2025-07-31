package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpleadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpleadoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Empleado;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
import com.powerRanger.ElBuenSabor.exceptions.InvalidOperationException;
import com.powerRanger.ElBuenSabor.repository.EmpleadoRepository;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private EmpleadoResponseDTO convertToResponseDto(Empleado empleado) {
        if (empleado == null) return null;
        EmpleadoResponseDTO dto = new EmpleadoResponseDTO();
        dto.setId(empleado.getId());
        dto.setNombre(empleado.getNombre());
        dto.setApellido(empleado.getApellido());
        dto.setTelefono(empleado.getTelefono());
        dto.setRolEmpleado(empleado.getRolEmpleado());
        dto.setEstadoActivo(empleado.getEstadoActivo());
        dto.setFechaBaja(empleado.getFechaBaja());
        if (empleado.getUsuario() != null) {
            dto.voidSetUsuarioId(empleado.getUsuario().getId());
            dto.setUsernameUsuario(empleado.getUsuario().getUsername());
        }
        return dto;
    }

    private void mapRequestDtoToEntity(EmpleadoRequestDTO dto, Empleado empleado) throws Exception {
        empleado.setNombre(dto.getNombre());
        empleado.setApellido(dto.getApellido());
        empleado.setTelefono(dto.getTelefono());
        empleado.setRolEmpleado(dto.getRolEmpleado());
        empleado.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + dto.getUsuarioId()));

            empleadoRepository.findByUsuarioId(usuario.getId()).ifPresent(existingEmpleado -> {
                if (empleado.getId() == null || !existingEmpleado.getId().equals(empleado.getId())) {
                    throw new RuntimeException("El Usuario ID " + dto.getUsuarioId() + " ya está asociado al empleado ID: " + existingEmpleado.getId());
                }
            });
            empleado.setUsuario(usuario);
        } else {
            throw new Exception("El ID de Usuario es obligatorio para el empleado.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDTO> getAll(String searchTerm, RolEmpleado rolEmpleado) {
        List<Empleado> empleados;
        String trimmedSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        if (trimmedSearchTerm != null || rolEmpleado != null) {
            empleados = empleadoRepository.searchByTermAndRol(trimmedSearchTerm, rolEmpleado);
        } else {
            empleados = empleadoRepository.findByEstadoActivoTrue();
        }
        return empleados.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO getById(Integer id) throws Exception {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Empleado no encontrado con ID: " + id));
        return convertToResponseDto(empleado);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO create(@Valid EmpleadoRequestDTO dto) throws Exception {
        if (empleadoRepository.findByUsuarioId(dto.getUsuarioId()).isPresent()) {
            throw new Exception("El Usuario ID " + dto.getUsuarioId() + " ya está asociado a otro empleado.");
        }

        Empleado empleado = new Empleado();
        mapRequestDtoToEntity(dto, empleado);
        Empleado empleadoGuardado = empleadoRepository.save(empleado);
        return convertToResponseDto(empleadoGuardado);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO update(Integer id, @Valid EmpleadoRequestDTO dto) throws Exception {
        Empleado empleadoExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Empleado no encontrado con ID: " + id));

        // Validación para evitar asociar un usuario ya asociado a otro empleado
        if (dto.getUsuarioId() != null && !dto.getUsuarioId().equals(empleadoExistente.getUsuario().getId())) {
            empleadoRepository.findByUsuarioId(dto.getUsuarioId()).ifPresent(e -> {
                if (!e.getId().equals(id)) {
                    throw new RuntimeException("El Usuario ID " + dto.getUsuarioId() + " ya está registrado por otro empleado.");
                }
            });
        }
        mapRequestDtoToEntity(dto, empleadoExistente);
        Empleado empleadoActualizado = empleadoRepository.save(empleadoExistente);
        return convertToResponseDto(empleadoActualizado);
    }

    @Override
    @Transactional
    public EmpleadoResponseDTO updateMiPerfil(String auth0Id, EmpleadoRequestDTO dto) throws Exception {
        try {
            Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                    .orElseThrow(() -> new Exception("No se encontró un usuario con el Auth0 ID proporcionado."));

            // **CORRECCIÓN 1: Usar 'empleadoRepository'**
            Empleado empleado = empleadoRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new Exception("No se encontró un perfil de empleado para este usuario."));

            empleado.setNombre(dto.getNombre());
            empleado.setApellido(dto.getApellido());
            empleado.setTelefono(dto.getTelefono());

            // **CORRECCIÓN 2: Usar 'empleadoRepository'**
            Empleado empleadoGuardado = empleadoRepository.save(empleado);

            // **CORRECCIÓN 3: Usar 'convertToResponseDto'**
            return convertToResponseDto(empleadoGuardado);

        } catch (Exception e) {
            throw new Exception("Error al actualizar el perfil del empleado: " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Empleado no encontrado con ID: " + id + " para borrado lógico"));
        empleado.setEstadoActivo(false);
        empleado.setFechaBaja(LocalDate.now());
        empleadoRepository.save(empleado);
    }
    @Override
    @Transactional
    public EmpleadoResponseDTO cambiarRol(Integer empleadoId, String nuevoRolStr) throws Exception {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new InvalidOperationException("Empleado no encontrado con ID: " + empleadoId));

        RolEmpleado rolEnum;
        try {
            rolEnum = RolEmpleado.valueOf(nuevoRolStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException("El rol especificado '" + nuevoRolStr + "' no es válido.");
        }

        // Validamos que el rol sea uno de los roles de empleado permitidos
        if (rolEnum != RolEmpleado.COCINA && rolEnum != RolEmpleado.CAJERO && rolEnum != RolEmpleado.DELIVERY) {
            throw new InvalidOperationException("Solo se puede asignar un rol de tipo COCINA, CAJERO o DELIVERY.");
        }

        empleado.setRolEmpleado(rolEnum);
        Empleado empleadoGuardado = empleadoRepository.save(empleado);

        return convertToResponseDto(empleadoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO getByUsuarioId(Integer usuarioId) throws Exception {
        Empleado empleado = empleadoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new Exception("Empleado no encontrado para el Usuario ID: " + usuarioId));
        return convertToResponseDto(empleado);
    }
    @Override
    @Transactional
    public EmpleadoResponseDTO findOrCreateEmpleadoPorAuth0Id(String auth0Id) throws Exception {
        // 1. Buscamos el usuario por su auth0Id. Asumimos que el JWTConverter ya lo creó.
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario de Auth0 no encontrado en la base de datos local."));

        // 2. Intentamos encontrar un empleado asociado a ese usuario.
        return empleadoRepository.findByUsuarioId(usuario.getId())
                .map(this::convertToResponseDto)
                .orElseGet(() -> {
                    try {
                        Empleado nuevoEmpleado = new Empleado();
                        nuevoEmpleado.setNombre("Nuevo"); // Valores por defecto
                        nuevoEmpleado.setApellido("Empleado");
                        nuevoEmpleado.setUsuario(usuario);
                        nuevoEmpleado.setEstadoActivo(true);
                        // Asigna un rol de empleado por defecto si es necesario, por ejemplo:
                        nuevoEmpleado.setRolEmpleado(RolEmpleado.CAJERO);

                        Empleado empleadoGuardado = empleadoRepository.save(nuevoEmpleado);
                        return convertToResponseDto(empleadoGuardado);
                    } catch (Exception e) {
                        throw new RuntimeException("Error al crear el perfil de empleado por defecto.", e);
                    }
                });
    }

}