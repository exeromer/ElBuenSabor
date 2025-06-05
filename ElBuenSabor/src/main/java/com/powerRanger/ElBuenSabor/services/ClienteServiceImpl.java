package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
// import com.powerRanger.ElBuenSabor.repository.ImagenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Asegúrate de tener este import si usas Objects::nonNull
import java.util.stream.Collectors;

@Service
@Validated
public class ClienteServiceImpl implements ClienteService {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    // @Autowired private ImagenRepository imagenRepository;
    @Autowired private DomicilioService domicilioService; // Lo mantengo por si se usa en el futuro

    // --- Mappers auxiliares para DomicilioResponseDTO ---
    // (Similar a lo que tienes en SucursalServiceImpl, podrías centralizarlos)
    private PaisResponseDTO convertPaisToDto(Pais pais) {
        if (pais == null) return null;
        PaisResponseDTO dto = new PaisResponseDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        return dto;
    }

    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) {
        if (provincia == null) return null;
        ProvinciaResponseDTO dto = new ProvinciaResponseDTO();
        dto.setId(provincia.getId());
        dto.setNombre(provincia.getNombre());
        if (provincia.getPais() != null) {
            dto.setPais(convertPaisToDto(provincia.getPais()));
        }
        return dto;
    }

    private LocalidadResponseDTO convertLocalidadToDto(Localidad localidad) {
        if (localidad == null) return null;
        LocalidadResponseDTO dto = new LocalidadResponseDTO();
        dto.setId(localidad.getId());
        dto.setNombre(localidad.getNombre());
        if (localidad.getProvincia() != null) {
            dto.setProvincia(convertProvinciaToDto(localidad.getProvincia()));
        }
        return dto;
    }

    private DomicilioResponseDTO convertDomicilioToDto(Domicilio domicilio) {
        if (domicilio == null) return null;
        DomicilioResponseDTO dto = new DomicilioResponseDTO();
        dto.setId(domicilio.getId());
        dto.setCalle(domicilio.getCalle());
        dto.setNumero(domicilio.getNumero());
        dto.setCp(domicilio.getCp());
        if (domicilio.getLocalidad() != null) {
            dto.setLocalidad(convertLocalidadToDto(domicilio.getLocalidad()));
        }
        return dto;
    }
    // --- Fin Mappers auxiliares ---

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
            // Usando el mapper de Domicilio completo
            dto.setDomicilios(cliente.getDomicilios().stream()
                    .map(this::convertDomicilioToDto) // Usa el mapper completo
                    .collect(Collectors.toList()));
        }

        // if (cliente.getImagen() != null) {
        //     // Aquí deberías convertir Imagen a ImagenResponseDTO
        //     // dto.setImagen(convertImagenToResponseDto(cliente.getImagen()));
        // }
        return dto;
    }

    private void mapRequestDtoToEntity(ClienteRequestDTO dto, Cliente cliente) throws Exception {
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + dto.getUsuarioId()));

            // Solo validar si el usuario ya está asignado a OTRO cliente
            Integer clienteIdActual = cliente.getId(); // Puede ser null si es una creación
            clienteRepository.findByUsuarioId(usuario.getId()).ifPresent(existingCliente -> {
                if (clienteIdActual == null || !existingCliente.getId().equals(clienteIdActual)) {
                    throw new RuntimeException("El Usuario ID " + dto.getUsuarioId() + " ya está asociado al cliente ID: " + existingCliente.getId());
                }
            });
            cliente.setUsuario(usuario);
        } else {
            // Si el usuario es opcional y se puede crear un cliente sin usuario,
            // entonces esta excepción no debería lanzarse o el DTO debería permitir null.
            // Asumiendo que siempre se requiere:
            throw new Exception("El ID de Usuario es obligatorio para el cliente.");
        }

        if (cliente.getDomicilios() == null) {
            cliente.setDomicilios(new ArrayList<>());
        }

        List<Domicilio> domiciliosParaAsignar = new ArrayList<>();
        if (dto.getDomicilioIds() != null && !dto.getDomicilioIds().isEmpty()) {
            for (Integer domicilioId : dto.getDomicilioIds()) {
                Domicilio dom = domicilioRepository.findById(domicilioId)
                        .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + domicilioId));
                domiciliosParaAsignar.add(dom);
            }
        }
        // Sincronizar la lista de domicilios
        cliente.getDomicilios().clear();
        if (!domiciliosParaAsignar.isEmpty()) {
            cliente.getDomicilios().addAll(domiciliosParaAsignar);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> getAllClientes(String searchTerm) { // Modificado
        List<Cliente> clientes;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            clientes = clienteRepository.searchActivosByTerm(searchTerm.trim());
            System.out.println("DEBUG: Buscando Clientes con término: '" + searchTerm.trim() + "', Encontrados: " + clientes.size());
        } else {
            clientes = clienteRepository.findByEstadoActivoTrue();
            System.out.println("DEBUG: Obteniendo todos los Clientes activos, Encontrados: " + clientes.size());
        }
        return clientes.stream()
                .map(this::convertToResponseDto) // Asumiendo que tienes convertToResponseDto para Cliente
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
    @Transactional(readOnly = true)
    public ClienteResponseDTO getMyProfile(String auth0Id) throws Exception {
        if (auth0Id == null || auth0Id.trim().isEmpty()) {
            throw new Exception("Auth0 ID no proporcionado para obtener el perfil.");
        }

        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id + ". No se puede obtener el perfil del cliente."));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new Exception("Perfil de Cliente no encontrado para el usuario: " + usuario.getUsername()));

        return convertToResponseDto(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO createCliente(@Valid ClienteRequestDTO dto) throws Exception {
        if (dto.getEmail() != null) { // Solo validar si el email se proporciona
            clienteRepository.findByEmail(dto.getEmail()).ifPresent(c -> { // Asumiendo que tienes findByEmail
                throw new RuntimeException("El email '" + dto.getEmail() + "' ya está registrado.");
            });
        }
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

        // Validar cambio de email si es necesario
        if (dto.getEmail() != null && !dto.getEmail().equals(clienteExistente.getEmail())) {
            clienteRepository.findByEmail(dto.getEmail()).ifPresent(c -> {
                if (!c.getId().equals(id)) { // Si el email pertenece a otro cliente
                    throw new RuntimeException("El email '" + dto.getEmail() + "' ya está registrado por otro cliente.");
                }
            });
        }
        // Validar cambio de usuario si es necesario (ya se maneja en mapRequestDtoToEntity)

        mapRequestDtoToEntity(dto, clienteExistente);
        Cliente clienteActualizado = clienteRepository.save(clienteExistente);
        return convertToResponseDto(clienteActualizado);
    }

    @Override
    @Transactional
    public void softDeleteCliente(Integer id) throws Exception {
        // Obtener la ENTIDAD Cliente directamente del repositorio
        Cliente clienteEntidad = clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id + " para borrado lógico"));

        clienteEntidad.setEstadoActivo(false);
        clienteEntidad.setFechaBaja(LocalDate.now());

        // Opcional: Considerar desactivar el Usuario asociado si es exclusivo de este Cliente
        // y si la lógica de negocio lo requiere.
        // if (clienteEntidad.getUsuario() != null && clienteEntidad.getUsuario().getEstadoActivo()) {
        //    Usuario usuarioAsociado = clienteEntidad.getUsuario();
        //    usuarioAsociado.setEstadoActivo(false);
        //    usuarioAsociado.setFechaBaja(LocalDate.now());
        //    usuarioRepository.save(usuarioAsociado); // Guardar el usuario actualizado
        // }

        clienteRepository.save(clienteEntidad); // Guardar la entidad Cliente actualizada
    }
}