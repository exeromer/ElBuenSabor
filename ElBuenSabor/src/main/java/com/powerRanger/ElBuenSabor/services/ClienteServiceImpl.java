package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*; // Importar todos los DTOs necesarios
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
// import com.powerRanger.ElBuenSabor.repository.ImagenRepository; // Si se maneja imagen
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring's Transactional
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ClienteServiceImpl implements ClienteService {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    // @Autowired private ImagenRepository imagenRepository; // Si manejas la entidad Imagen directamente
    // Inyectar servicios de Domicilio, etc., si los mappers están allí, o tener mappers aquí
    @Autowired private DomicilioService domicilioService; // Para obtener DomicilioResponseDTO

    // Método de Mapeo de Entidad a DTO de Respuesta
    private ClienteResponseDTO convertToResponseDto(Cliente cliente) {
        if (cliente == null) return null;
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setEstadoActivo(cliente.getEstadoActivo());
        dto.setFechaBaja(cliente.getFechaBaja());

        if (cliente.getUsuario() != null) {
            dto.setUsuarioId(cliente.getUsuario().getId());
            dto.setUsername(cliente.getUsuario().getUsername());
            dto.setRolUsuario(cliente.getUsuario().getRol());
        }

        if (cliente.getDomicilios() != null) {
            // Asumimos que DomicilioService tiene un método para convertir Domicilio a DomicilioResponseDTO
            // o que tenemos un mapper aquí. Por simplicidad, lo haré aquí.
            // Si DomicilioService.getById() devuelve DomicilioResponseDTO, sería mejor:
            // dto.setDomicilios(cliente.getDomicilios().stream()
            //          .map(dom -> {
            //              try { return domicilioService.getById(dom.getId()); }
            //              catch (Exception e) { return null; } // Manejar error
            //          })
            //          .filter(Objects::nonNull)
            //          .collect(Collectors.toList()));
            // Mapeo manual simplificado (asume que DomicilioResponseDTO se puede construir desde Domicilio)
            dto.setDomicilios(cliente.getDomicilios().stream().map(dom -> {
                DomicilioResponseDTO domDto = new DomicilioResponseDTO();
                domDto.setId(dom.getId());
                domDto.setCalle(dom.getCalle());
                domDto.setNumero(dom.getNumero());
                domDto.setCp(dom.getCp());
                // Para la localidad dentro de domicilio, necesitaríamos un mapper anidado
                // o que DomicilioResponseDTO ya la incluya si DomicilioService la mapea.
                // Por ahora, esto es simplificado.
                return domDto;
            }).collect(Collectors.toList()));
        }

        // if (cliente.getImagen() != null) {
        //     dto.setImagenUrl(cliente.getImagen().getDenominacion()); // O mapear a ImagenResponseDTO
        // }
        return dto;
    }

    private void mapRequestDtoToEntity(ClienteRequestDTO dto, Cliente cliente) throws Exception {
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail()); // Considerar validación de unicidad de email
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + dto.getUsuarioId()));
            // Validar si este usuario ya está asignado a otro cliente (si el ID del cliente actual es diferente)
            clienteRepository.findByUsuarioId(usuario.getId()).ifPresent(existingCliente -> {
                if (!existingCliente.getId().equals(cliente.getId())) { // cliente.getId() será null en creación
                    throw new RuntimeException("El Usuario ID " + dto.getUsuarioId() + " ya está asociado a otro cliente.");
                }
            });
            cliente.setUsuario(usuario);
        } else {
            throw new Exception("El ID de Usuario es obligatorio para el cliente.");
        }

        // Manejar Domicilios
        if (cliente.getDomicilios() == null) cliente.setDomicilios(new ArrayList<>());
        // Estrategia: Sincronizar la lista de domicilios
        List<Domicilio> domiciliosActuales = new ArrayList<>(cliente.getDomicilios());
        List<Domicilio> domiciliosNuevos = new ArrayList<>();

        if (dto.getDomicilioIds() != null) {
            for (Integer domicilioId : dto.getDomicilioIds()) {
                Domicilio dom = domicilioRepository.findById(domicilioId)
                        .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + domicilioId));
                domiciliosNuevos.add(dom);
            }
        }
        // Quitar los que ya no están
        for (Domicilio domActual : domiciliosActuales) {
            if (!domiciliosNuevos.contains(domActual)) {
                cliente.removeDomicilio(domActual); // Usa el helper
            }
        }
        // Añadir los nuevos
        for (Domicilio domNuevo : domiciliosNuevos) {
            if (!cliente.getDomicilios().contains(domNuevo)) { // Evitar duplicados si el helper no lo hace
                cliente.addDomicilio(domNuevo); // Usa el helper
            }
        }
        // La entidad Imagen se manejaría por separado, usualmente con subida de archivos
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> getAllClientes() {
        return clienteRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO getClienteById(Integer id) throws Exception {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id));
        return convertToResponseDto(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO createCliente(@Valid ClienteRequestDTO dto) throws Exception {
        // Validar unicidad de email
        // clienteRepository.findByEmail(dto.getEmail()).ifPresent(c -> {
        //     throw new RuntimeException("El email '" + dto.getEmail() + "' ya está registrado.");
        // });
        // Validar unicidad de usuarioId
        if (dto.getUsuarioId() != null && clienteRepository.findByUsuarioId(dto.getUsuarioId()).isPresent()) {
            throw new RuntimeException("El Usuario ID " + dto.getUsuarioId() + " ya está asociado a otro cliente.");
        }

        Cliente cliente = new Cliente();
        mapRequestDtoToEntity(dto, cliente);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return convertToResponseDto(clienteGuardado);
    }

    @Override
    @Transactional
    public ClienteResponseDTO updateCliente(Integer id, @Valid ClienteRequestDTO dto) throws Exception {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id));

        mapRequestDtoToEntity(dto, clienteExistente);
        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        return convertToResponseDto(clienteActualizado);
    }

    @Override
    @Transactional
    public void softDeleteCliente(Integer id) throws Exception {
        Cliente cliente = getClienteById(id). // Lanza excepción si no existe, getById ahora devuelve DTO
                orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id)); // Necesitamos la entidad

        // Para obtener la entidad:
        // Cliente clienteEntidad = clienteRepository.findById(id)
        //    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id));

        cliente.setEstadoActivo(false);
        cliente.setFechaBaja(LocalDate.now());
        // Considerar desactivar el Usuario asociado si es exclusivo de este Cliente
        // if (cliente.getUsuario() != null && cliente.getUsuario().getEstadoActivo()) {
        //    cliente.getUsuario().setEstadoActivo(false);
        //    cliente.getUsuario().setFechaBaja(LocalDate.now());
        //    usuarioRepository.save(cliente.getUsuario());
        // }
        clienteRepository.save(cliente);
    }
}