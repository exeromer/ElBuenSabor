package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.ClienteRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.services.ClienteService;
// import com.powerRanger.ElBuenSabor.services.UsuarioService; // Para obtener el usuario autenticado
// import org.springframework.security.core.Authentication; // Para obtener el usuario autenticado
// import org.springframework.security.oauth2.jwt.Jwt; // Para obtener el auth0Id
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/clientes")
@Validated
public class ClienteController {

    @Autowired
    private ClienteService clienteService;
    // @Autowired // Descomentar cuando se implemente la lógica de perfil
    // private UsuarioService usuarioService;
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> createCliente(@Valid @RequestBody ClienteRequestDTO dto) {
        try {
            Cliente nuevoCliente = clienteService.createCliente(dto);
            return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')") // Ejemplo de autorización
    public ResponseEntity<List<Cliente>> getAllClientes() {
        try {
            List<Cliente> clientes = clienteService.getAllClientes();
            if (clientes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO') or @clienteSecurityService.isOwner(authentication, #id)") // Ejemplo autorización avanzada
    public ResponseEntity<?> getClienteById(@PathVariable Integer id) {
        try {
            Cliente cliente = clienteService.getClienteById(id);
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/usuario/{usuarioId}") // <--- Nuevo endpoint para obtener cliente por Usuario ID
    public ResponseEntity<?> getClienteByUsuarioId(@PathVariable Integer usuarioId) {
        try {
            Cliente cliente = clienteService.getClienteByUsuarioId(usuarioId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado para el Usuario ID: " + usuarioId));
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /*
    // Endpoint para que un cliente obtenga su propio perfil
    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            Usuario usuario = usuarioService.getByAuth0Id(auth0Id); // Asume que este método existe
            Cliente cliente = clienteService.getClienteByUsuarioId(usuario.getId()); // Necesitarías este método en ClienteService
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", "Perfil de cliente no encontrado o error al obtenerlo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // Endpoint para que un cliente actualice su propio perfil
    @PutMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<?> updateMyProfile(Authentication authentication, @Valid @RequestBody ClienteRequestDTO dto) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            Usuario usuario = usuarioService.getByAuth0Id(auth0Id);
            Cliente cliente = clienteService.getClienteByUsuarioId(usuario.getId()); // Lanza excepción si no existe

            // No permitir cambiar el usuarioId desde este endpoint
            if (dto.getUsuarioId() != null && !dto.getUsuarioId().equals(usuario.getId())) {
                 Map<String, Object> errorResponse = new HashMap<>();
                 errorResponse.put("error", "No se puede cambiar el usuario asociado al perfil.");
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            // Asegurarse que el DTO usa el usuarioId correcto para la validación en el servicio si fuera necesario
            dto.setUsuarioId(usuario.getId());


            Cliente clienteActualizado = clienteService.updateCliente(cliente.getId(), dto);
            return ResponseEntity.ok(clienteActualizado);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación al actualizar perfil");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                                          .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                                          .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
    */

    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Solo admin puede actualizar cualquier cliente por ID
    public ResponseEntity<?> updateCliente(@PathVariable Integer id, @Valid @RequestBody ClienteRequestDTO dto) {
        try {
            Cliente clienteActualizado = clienteService.updateCliente(id, dto);
            return ResponseEntity.ok(clienteActualizado);
        } catch (ConstraintViolationException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Error de validación al actualizar");
            errorResponse.put("mensajes", e.getConstraintViolations().stream()
                    .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                    .collect(Collectors.toList()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}") // Implementa borrado lógico
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> softDeleteCliente(@PathVariable Integer id) {
        try {
            clienteService.softDeleteCliente(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Cliente con ID " + id + " marcado como inactivo (borrado lógico).");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.NOT_FOUND.value());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}