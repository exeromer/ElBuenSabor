package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.PedidoEstadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Pedido;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.services.PedidoService;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    // @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<?> createPedidoForAuthenticatedClient(@Valid @RequestBody PedidoRequestDTO dto, Authentication authentication) {
        try {
            // Cuando la seguridad esté activa, obtendrás el auth0Id del token
            // String auth0Id = "auth0|testcliente"; // Placeholder si pruebas sin token real
            // if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            //     Jwt jwt = (Jwt) authentication.getPrincipal();
            //     auth0Id = jwt.getSubject();
            // } else {
            //     // Manejar caso donde no hay token o el principal no es Jwt (importante si permites acceso sin token a este endpoint)
            //     // Por ahora, con SecurityConfig permitiendo todo, authentication será null.
            //     // Para que este endpoint funcione sin token, deberías usar el createPedidoByAdmin
            //     // o modificar la lógica para tomar clienteId del DTO si no hay auth.
            //     // Vamos a asumir que si no hay auth, es un error o usamos el clienteId del DTO
            //     if (dto.getClienteId() == null) {
            //         throw new Exception("Se requiere clienteId en el DTO o un usuario autenticado.");
            //     }
            //     // Para prueba SIN token, llamamos al create normal que usa clienteId del DTO
            //     Pedido nuevoPedido = pedidoService.create(dto);
            //     return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
            // }
            // Pedido nuevoPedido = pedidoService.createForAuthenticatedClient(auth0Id, dto);
            // return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);

            // Simplificación para pruebas con SecurityConfig permitiendo todo y sin enviar token:
            // Usaremos el endpoint /admin o asumiremos que el clienteId viene en el DTO
            if (dto.getClienteId() == null) {
                throw new Exception("Se requiere clienteId en el DTO para este endpoint de prueba.");
            }
            Pedido nuevoPedido = pedidoService.create(dto); // Usa el método que toma clienteId del DTO
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);

        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createPedidoByAdmin(@Valid @RequestBody PedidoRequestDTO dto) {
        try {
            Pedido nuevoPedido = pedidoService.create(dto);
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<List<Pedido>> getAllPedidos() {
        try {
            List<Pedido> pedidos = pedidoService.getAll();
            if (pedidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mis-pedidos")
    // @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<?> getMisPedidos(Authentication authentication) {
        try {
            // String auth0Id = "auth0|testcliente"; // Placeholder si pruebas sin token real
            // if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            //     throw new Exception("Se requiere autenticación para ver 'mis pedidos'.");
            // }
            // Jwt jwt = (Jwt) authentication.getPrincipal();
            // auth0Id = jwt.getSubject();

            // Para prueba sin token, necesitaríamos un clienteId o auth0Id de prueba
            // Esto fallará si no hay autenticación real.
            // List<Pedido> pedidos = pedidoService.getPedidosByClienteAuth0Id(auth0Id);

            // Alternativa para prueba sin token (requiere un clienteId como parámetro):
            // Este endpoint debería ser /cliente/{clienteId}/pedidos para ser más RESTful
            // Por ahora, no lo implementaremos así para no cambiar la firma del servicio.
            // Solo para demostración, no usar en producción sin seguridad real:
            if (authentication == null) { // Simulación para cuando no hay token
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Simulación: Se requiere autenticación para ver 'mis pedidos'. Pruebe GET /api/pedidos/cliente/{id}");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();

            List<Pedido> pedidos = pedidoService.getPedidosByClienteAuth0Id(auth0Id);
            if (pedidos.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cliente/{clienteId}") // Endpoint para que admin/empleado vea pedidos de un cliente
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<?> getPedidosByClienteId(@PathVariable Integer clienteId) {
        try {
            List<Pedido> pedidos = pedidoService.getPedidosByClienteId(clienteId);
            if (pedidos.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO') or @pedidoSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<?> getPedidoById(@PathVariable Integer id) {
        try {
            Pedido pedido = pedidoService.getById(id);
            return ResponseEntity.ok(pedido);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/estado")
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<?> updatePedidoEstado(@PathVariable Integer id, @Valid @RequestBody PedidoEstadoRequestDTO estadoDto) {
        try {
            Pedido pedidoActualizado = pedidoService.updateEstado(id, estadoDto.getNuevoEstado());
            return ResponseEntity.ok(pedidoActualizado);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<?> softDeletePedido(@PathVariable Integer id) {
        try {
            pedidoService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Pedido con ID " + id + " procesado para borrado lógico/cancelación.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    private ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de validación");
        errorResponse.put("mensajes", e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<Map<String, Object>> handleGenericException(Exception e, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(status).body(errorResponse);
    }
}