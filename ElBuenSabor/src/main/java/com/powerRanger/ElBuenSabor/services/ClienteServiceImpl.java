package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.entities.Domicilio;
import com.powerRanger.ElBuenSabor.entities.Usuario;
// Importa Imagen y su repositorio si vas a manejar la imagen aquí
// import com.powerRanger.ElBuenSabor.entities.Imagen;
// import com.powerRanger.ElBuenSabor.repository.ImagenRepository;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.repository.DomicilioRepository;
import com.powerRanger.ElBuenSabor.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    // @Autowired private ImagenRepository imagenRepository; // Si manejas Imagen

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente getClienteById(Integer id) throws Exception {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + id));
    }

    // Podríamos implementar:
    // @Override
    // @Transactional(readOnly = true)
    // public Cliente getClienteByUsuarioAuth0Id(String auth0Id) throws Exception {
    //     Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
    //             .orElseThrow(() -> new Exception("Usuario no encontrado para Auth0 ID: " + auth0Id));
    //     return clienteRepository.findByUsuarioId(usuario.getId()) // Necesitarías este método en ClienteRepository
    //             .orElseThrow(() -> new Exception("Cliente no encontrado para Usuario ID: " + usuario.getId()));
    // }


    private void mapDtoToEntity(ClienteRequestDTO dto, Cliente cliente) throws Exception {
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        // Asociar Usuario
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElseThrow(() -> new Exception("Usuario no encontrado con ID: " + dto.getUsuarioId()));
            // Verificar si este usuario ya está asociado a otro cliente (si es una relación 1a1 estricta)
            // Cliente clienteExistenteConUsuario = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
            // if(clienteExistenteConUsuario != null && !clienteExistenteConUsuario.getId().equals(cliente.getId())) {
            //    throw new Exception("El Usuario ID " + dto.getUsuarioId() + " ya está asociado a otro cliente.");
            // }
            cliente.setUsuario(usuario);
        } else {
            throw new Exception("El ID de Usuario es obligatorio para crear/actualizar un cliente.");
        }

        // Asociar Domicilios
        if (cliente.getDomicilios() == null) { // Asegurar que la lista exista
            cliente.setDomicilios(new ArrayList<>());
        }
        cliente.getDomicilios().clear(); // Para la actualización, borramos los anteriores
        if (dto.getDomicilioIds() != null && !dto.getDomicilioIds().isEmpty()) {
            for (Integer domicilioId : dto.getDomicilioIds()) {
                Domicilio domicilio = domicilioRepository.findById(domicilioId)
                        .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + domicilioId));
                cliente.addDomicilio(domicilio); // Usar el helper addDomicilio
            }
        }

        // Manejo de Imagen (simplificado, usualmente la subida de imagen es un endpoint separado)
        // Si el DTO tuviera imagenId o imagenUrl:
        // if (dto.getImagenId() != null) {
        //     Imagen imagen = imagenRepository.findById(dto.getImagenId()).orElseThrow(...);
        //     cliente.setImagen(imagen);
        // } else {
        //     cliente.setImagen(null); // O manejar la lógica si no se envía imagen
        // }
    }

    @Override
    @Transactional
    public Cliente createCliente(@Valid ClienteRequestDTO dto) throws Exception {
        // Validar si el email ya existe
        // if (clienteRepository.findByEmail(dto.getEmail()).isPresent()) {
        //    throw new Exception("El email '" + dto.getEmail() + "' ya está registrado.");
        // }
        // Validar si el usuarioId ya está asociado a otro cliente
        // if (clienteRepository.findByUsuarioId(dto.getUsuarioId()).isPresent()) {
        //    throw new Exception("El Usuario ID " + dto.getUsuarioId() + " ya está asociado a otro cliente.");
        // }

        Cliente cliente = new Cliente();
        mapDtoToEntity(dto, cliente);
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente updateCliente(Integer id, @Valid ClienteRequestDTO dto) throws Exception {
        Cliente clienteExistente = getClienteById(id); // Verifica si el cliente existe

        // Validar si el nuevo email ya está en uso por OTRO cliente
        // clienteRepository.findByEmail(dto.getEmail()).ifPresent(c -> {
        //     if(!c.getId().equals(id)) throw new RuntimeException("El email ya está en uso por otro cliente.");
        // });
        // Validar si el nuevo usuarioId ya está en uso por OTRO cliente
        // clienteRepository.findByUsuarioId(dto.getUsuarioId()).ifPresent(c -> {
        //     if(!c.getId().equals(id)) throw new RuntimeException("El Usuario ID ya está en uso por otro cliente.");
        // });

        mapDtoToEntity(dto, clienteExistente);
        return clienteRepository.save(clienteExistente);
    }

    @Override
    @Transactional
    public void softDeleteCliente(Integer id) throws Exception {
        Cliente cliente = getClienteById(id);
        cliente.setEstadoActivo(false);
        cliente.setFechaBaja(LocalDate.now());
        // Considerar qué pasa con el Usuario asociado, ¿también se desactiva? Depende de tu lógica.
        // if (cliente.getUsuario() != null) {
        //     cliente.getUsuario().setEstadoActivo(false);
        //     cliente.getUsuario().setFechaBaja(LocalDate.now());
        //     usuarioRepository.save(cliente.getUsuario());
        // }
        clienteRepository.save(cliente);
    }
}