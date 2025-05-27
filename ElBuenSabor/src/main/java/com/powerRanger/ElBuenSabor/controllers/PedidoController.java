package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoEstadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.services.PedidoService;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping
    public ResponseEntity<?> createPedidoForAuthenticatedClient(@Valid @RequestBody PedidoRequestDTO dto, Authentication authentication) {
        try {
            if (authentication == null && dto.getClienteId() == null) {
                throw new Exception("Para crear un pedido sin autenticación (modo prueba), se requiere clienteId en el DTO.");
            }

            PedidoResponseDTO nuevoPedidoDto;
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String auth0Id = jwt.getSubject();
                nuevoPedidoDto = pedidoService.createForAuthenticatedClient(auth0Id, dto);
            } else {
                nuevoPedidoDto = pedidoService.create(dto);
            }
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);

        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin")
    public ResponseEntity<?> createPedidoByAdmin(@Valid @RequestBody PedidoRequestDTO dto) {
        try {
            PedidoResponseDTO nuevoPedidoDto = pedidoService.create(dto);
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos() {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getAll();
            if (pedidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> getMisPedidos(Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                throw new Exception("Se requiere autenticación para ver 'mis pedidos'.");
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();

            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosByClienteAuth0Id(auth0Id);
            if (pedidos.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> getPedidosByClienteId(@PathVariable Integer clienteId) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosByClienteId(clienteId);
            if (pedidos.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPedidoById(@PathVariable Integer id) {
        try {
            PedidoResponseDTO pedidoDto = pedidoService.getById(id);
            return ResponseEntity.ok(pedidoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Crea un Pedido a partir del carrito de un Cliente específico.
     * Versión actual para pruebas: El cliente se identifica por el clienteId en la URL.
     */
    @PostMapping("/cliente/{clienteId}/desde-carrito") // La URL incluye clienteId
    public ResponseEntity<?> crearPedidoDesdeCarrito(
            @PathVariable Integer clienteId, // Se toma el clienteId del path
            @Valid @RequestBody CrearPedidoRequestDTO pedidoRequest) {

        // ------------------------------------------------------------------------------------
        // NOTA PARA EL FUTURO (FLUJO CORRECTO CON AUTENTICACIÓN JWT/AUTH0 ACTIVA):
        // Cuando la seguridad con Auth0 esté completamente integrada y los clientes
        // envíen un token JWT válido, este endpoint debería idealmente obtener la identidad
        // del cliente a partir del token (Authentication Principal), en lugar de un ID en la URL,
        // para acciones que el cliente realiza sobre sus propios datos.
        //
        // La firma del método cambiaría a algo como:
        // @PostMapping("/desde-carrito") // URL podría ser más genérica para el cliente autenticado
        // public ResponseEntity<?> crearPedidoDesdeCarritoAutenticado(
        //         @Valid @RequestBody CrearPedidoRequestDTO pedidoRequest,
        //         Authentication authentication) { // Spring Security inyecta el objeto Authentication
        //
        // Y la lógica para obtener el cliente sería:
        // if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
        //     throw new Exception("Se requiere autenticación para confirmar el pedido desde el carrito.");
        // }
        // Jwt jwt = (Jwt) authentication.getPrincipal();
        // String auth0Id = jwt.getSubject();
        // Usuario usuarioAutenticado = usuarioService.findActualByAuth0Id(auth0Id) // Usando el método corregido
        //     .orElseThrow(() -> new Exception("Usuario autenticado (Auth0 ID: " + auth0Id + ") no encontrado."));
        // Cliente cliente = clienteRepository.findByUsuarioId(usuarioAutenticado.getId())
        //     .orElseThrow(() -> new Exception("Perfil de Cliente no encontrado para el usuario: " + usuarioAutenticado.getUsername()));
        // // ... luego se llama a pedidoService.crearPedidoDesdeCarrito(cliente, pedidoRequest);
        // ------------------------------------------------------------------------------------

        try {
            // Lógica actual (para probar sin token JWT o para uso por roles como admin):
            // Obtener el Cliente directamente usando el clienteId del path.
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId + ". No se puede crear pedido desde carrito."));

            // Llamar al servicio con la entidad Cliente obtenida
            PedidoResponseDTO nuevoPedidoDto = pedidoService.crearPedidoDesdeCarrito(cliente, pedidoRequest);
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);

        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("No se encontró un carrito") ||
                    e.getMessage().contains("El carrito está vacío") ||
                    e.getMessage().contains("no encontrado con ID:") ||
                    e.getMessage().contains("no pertenece al cliente")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("Stock insuficiente")) {
                status = HttpStatus.CONFLICT;
            }
            return handleGenericException(e, status);
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> updatePedidoEstado(@PathVariable Integer id, @Valid @RequestBody PedidoEstadoRequestDTO estadoDto) {
        try {
            PedidoResponseDTO pedidoActualizadoDto = pedidoService.updateEstado(id, estadoDto.getNuevoEstado());
            return ResponseEntity.ok(pedidoActualizadoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
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