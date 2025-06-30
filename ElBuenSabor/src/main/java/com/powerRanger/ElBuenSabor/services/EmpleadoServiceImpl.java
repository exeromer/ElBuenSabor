package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.EmpleadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.EmpleadoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Empleado;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado;
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
    public void softDelete(Integer id) throws Exception {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Empleado no encontrado con ID: " + id + " para borrado lógico"));
        empleado.setEstadoActivo(false);
        empleado.setFechaBaja(LocalDate.now());
        empleadoRepository.save(empleado);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDTO getByUsuarioId(Integer usuarioId) throws Exception {
        Empleado empleado = empleadoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new Exception("Empleado no encontrado para el Usuario ID: " + usuarioId));
        return convertToResponseDto(empleado);
    }
}