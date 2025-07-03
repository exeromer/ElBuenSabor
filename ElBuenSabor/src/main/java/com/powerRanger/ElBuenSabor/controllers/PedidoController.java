package com.powerRanger.ElBuenSabor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.Estado; // Importar Estado
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.Rol; // Importar Rol
import com.powerRanger.ElBuenSabor.entities.enums.RolEmpleado; // Importar RolEmpleado
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.services.EmpleadoService; // Importar EmpleadoService
import com.powerRanger.ElBuenSabor.services.FacturaService;
import com.powerRanger.ElBuenSabor.services.PedidoService;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat; // Para LocalDate en RequestParam
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Para seguridad a nivel de método
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; // Importar SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpleadoService empleadoService; // Inyectar EmpleadoService

    @Autowired
    private FacturaService facturaService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<?> createPedidoForAuthenticatedClient(@Valid @RequestBody PedidoRequestDTO dto, Authentication authentication) {
        logger.info(">> POST /api/pedidos - Creando pedido para cliente.");
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                logger.warn("!! Intento de crear pedido sin autenticación válida.");
                throw new Exception("Se requiere autenticación para crear un pedido.");
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            logger.info(">> Autenticación por JWT encontrada. Auth0 ID: {}", auth0Id);
            PedidoResponseDTO nuevoPedidoDto = pedidoService.createForAuthenticatedClient(auth0Id, dto);

            logger.info("<< Pedido creado exitosamente con ID: {}", nuevoPedidoDto.getId());
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);

        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createPedidoByAdmin(@Valid @RequestBody PedidoRequestDTO dto) {
        logger.info(">> POST /api/pedidos/admin - Creando pedido como administrador.");
        try {
            PedidoResponseDTO nuevoPedidoDto = pedidoService.create(dto);
            logger.info("<< Pedido creado por admin exitosamente con ID: {}", nuevoPedidoDto.getId());
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);
        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<List<PedidoResponseDTO>> getAllPedidos() {
        logger.info(">> GET /api/pedidos - Obteniendo todos los pedidos.");
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getAll();
            if (pedidos.isEmpty()) {
                logger.info("<< No se encontraron pedidos.");
                return ResponseEntity.noContent().build();
            }
            logger.info("<< Se encontraron {} pedidos.", pedidos.size());
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            logger.error("!! Error al obtener todos los pedidos.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<?> getMisPedidos(Authentication authentication) {
        logger.info(">> GET /api/pedidos/mis-pedidos - Obteniendo pedidos del usuario autenticado.");
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
                logger.warn("!! Acceso a /mis-pedidos sin autenticación válida.");
                throw new Exception("Se requiere autenticación para ver 'mis pedidos'.");
            }
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            logger.info(">> Buscando pedidos para Auth0 ID: {}", auth0Id);

            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosByClienteAuth0Id(auth0Id);
            if (pedidos.isEmpty()) {
                logger.info("<< No se encontraron pedidos para el usuario con Auth0 ID: {}", auth0Id);
                return ResponseEntity.noContent().build();
            }
            logger.info("<< Se encontraron {} pedidos para el usuario con Auth0 ID: {}.", pedidos.size(), auth0Id);
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<?> getPedidosByClienteId(@PathVariable Integer clienteId) {
        logger.info(">> GET /api/pedidos/cliente/{} - Obteniendo pedidos por ID de cliente.", clienteId);
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosByClienteId(clienteId);
            if (pedidos.isEmpty()) {
                logger.info("<< No se encontraron pedidos para el cliente con ID: {}", clienteId);
                return ResponseEntity.noContent().build();
            }
            logger.info("<< Se encontraron {} pedidos para el cliente con ID: {}.", pedidos.size(), clienteId);
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO', 'ROLE_CLIENTE')")
    public ResponseEntity<?> getPedidoById(@PathVariable Integer id, Authentication authentication) {
        logger.info(">> GET /api/pedidos/{} - Obteniendo pedido por ID.", id);
        try {
            PedidoResponseDTO pedidoDto = pedidoService.getById(id);

            // Si es un cliente, validar que el pedido le pertenece
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String auth0Id = jwt.getSubject();
                UsuarioResponseDTO usuario = usuarioService.getByAuth0Id(auth0Id); // Asumiendo que este método existe y retorna Usuario
                if (usuario.getRol() == Rol.CLIENTE && !pedidoDto.getCliente().getUsuarioId().equals(usuario.getId())) {
                    throw new Exception("Acceso denegado: El pedido no pertenece a este cliente.");
                }
            }
            logger.info("<< Pedido con ID: {} encontrado.", id);
            return ResponseEntity.ok(pedidoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/cliente/{clienteId}/desde-carrito")
    @PreAuthorize("hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<?> crearPedidoDesdeCarrito(
            @PathVariable Integer clienteId,
            @Valid @RequestBody CrearPedidoRequestDTO pedidoRequest) {

        try {
            String requestBodyJson = objectMapper.writeValueAsString(pedidoRequest);
            logger.info(">>>>>>>>>> INICIO CREAR PEDIDO DESDE CARRITO - Cliente ID: {}", clienteId);
            logger.info(">>>>>>>>>> Request Body Recibido: {}", requestBodyJson);

            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId + ". No se puede crear pedido desde carrito."));

            PedidoResponseDTO nuevoPedidoDto = pedidoService.crearPedidoDesdeCarrito(cliente, pedidoRequest);

            logger.info("<<<<<<<<<< FIN CREAR PEDIDO - Pedido ID: {} - Preferencia MP Creada: {}", nuevoPedidoDto.getId(), nuevoPedidoDto.getMpPreferenceId());
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')") // Seguridad general para actualización de estado
    public ResponseEntity<?> updatePedidoEstado(@PathVariable Integer id, @Valid @RequestBody PedidoEstadoRequestDTO estadoDto) {
        logger.info(">> PUT /api/pedidos/{}/estado - Actualizando estado del pedido.", id);
        try {
            PedidoResponseDTO pedidoActualizadoDto = pedidoService.updateEstado(id, estadoDto.getNuevoEstado());
            logger.info("<< Estado del pedido con ID: {} actualizado a {}.", id, estadoDto.getNuevoEstado());
            return ResponseEntity.ok(pedidoActualizadoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{pedidoId}/estado-empleado/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<?> updatePedidoEstadoByEmpleado(@PathVariable Integer pedidoId,
                                                          @PathVariable Integer sucursalId,
                                                          @Valid @RequestBody PedidoEstadoRequestDTO estadoDto,
                                                          Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();
            PedidoResponseDTO pedidoActualizado = pedidoService.updateEstadoParaEmpleado(pedidoId, estadoDto.getNuevoEstado(), sucursalId, auth0Id);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND :
                    (e.getMessage().contains("Acceso denegado") || e.getMessage().contains("no puede")) ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }

    @PutMapping("/{pedidoId}/tiempo-cocina/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')") // Asegurar que solo empleados pueden acceder
    public ResponseEntity<?> addTiempoEstimadoCocina(@PathVariable Integer pedidoId,
                                                     @PathVariable Integer sucursalId,
                                                     @RequestParam Integer minutosToAdd,
                                                     Authentication authentication) {
        try {
            PedidoResponseDTO pedidoActualizado = pedidoService.addTiempoCocina(pedidoId, minutosToAdd, sucursalId);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (Exception e) {
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND :
                    (e.getMessage().contains("Acceso denegado") || e.getMessage().contains("no puede")) ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            return handleGenericException(e, status);
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<?> softDeletePedido(@PathVariable Integer id) {
        logger.info(">> DELETE /api/pedidos/{} - Iniciando borrado lógico de pedido.", id);
        try {
            pedidoService.softDelete(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Pedido con ID " + id + " procesado para borrado lógico/cancelación.");
            logger.info("<< Pedido con ID: {} procesado para borrado lógico.", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    // --- ENDPOINTS PARA ROLES ESPECÍFICOS ---

    // Cajero
    @GetMapping("/cajero/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')") // Empleados con rol Cajero deberían tener acceso
    public ResponseEntity<?> getPedidosForCajero(
            @PathVariable Integer sucursalId,
            @RequestParam(name = "estado", required = false) Estado estado,
            @RequestParam(name = "pedidoId", required = false) Integer pedidoId,
            @RequestParam(name = "fechaDesde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(name = "fechaHasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Authentication authentication) {
        try {
            // Se puede añadir una validación de rol más estricta aquí si el empleado
            // solo debe acceder si su rolEmpleado es CAJERO. Por ahora, se asume que
            // cualquier ROLE_EMPLEADO que acceda a esta URL es porque tiene permiso.
            // Para una validación estricta por RolEmpleado, se debería obtener el empleado
            // por el auth0Id y verificar su rolEmpleado.

            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosParaCajero(sucursalId, estado, pedidoId, fechaDesde, fechaHasta);
            if (pedidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/{pedidoId}/confirmar-pago-efectivo")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')")
    public ResponseEntity<?> confirmarPagoEfectivo(
            @PathVariable Integer pedidoId,
            Authentication authentication) {

        logger.info(">> POST /api/pedidos/{}/confirmar-pago-efectivo", pedidoId);

        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String auth0Id = jwt.getSubject();

            // 1. Obtener el pedido para validar
            PedidoResponseDTO pedido = pedidoService.getById(pedidoId);

            // 2. Validaciones
            if (pedido.getFormaPago() != FormaPago.EFECTIVO) {
                return handleGenericException(new Exception("El método de pago de este pedido no es EFECTIVO."), HttpStatus.BAD_REQUEST);
            }
            if (pedido.getEstado() != Estado.LISTO) {
                return handleGenericException(new Exception("El pedido debe estar en estado LISTO para ser pagado y entregado."), HttpStatus.BAD_REQUEST);
            }

            // 3. Cambiar el estado a ENTREGADO (esto disparará la facturación)
            PedidoResponseDTO pedidoEntregado = pedidoService.updateEstadoParaEmpleado(
                    pedidoId,
                    Estado.ENTREGADO,
                    pedido.getSucursal().getId(),
                    auth0Id
            );

            logger.info("<< Pedido ID: {} marcado como ENTREGADO y facturado (pago en efectivo).", pedidoId);
            return ResponseEntity.ok(pedidoEntregado);

        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

        // Cocina
    @GetMapping("/cocina/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')") // Empleados con rol Cocina
    public ResponseEntity<?> getPedidosForCocina(
            @PathVariable Integer sucursalId,
            Authentication authentication) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosParaCocina(sucursalId);
            if (pedidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delivery
    @GetMapping("/delivery/{sucursalId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLEADO')") // Empleados con rol Delivery
    public ResponseEntity<?> getPedidosForDelivery(
            @PathVariable Integer sucursalId,
            Authentication authentication) {
        try {
            List<PedidoResponseDTO> pedidos = pedidoService.getPedidosParaDelivery(sucursalId);
            if (pedidos.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pedidos);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Métodos helper para manejo de errores (ya los tenías)
    private ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        logger.error("!! Error de validación de constraint: {}", e.getMessage());
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Error de validación");
        errorResponse.put("mensajes", e.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    private ResponseEntity<Map<String, Object>> handleGenericException(Exception e, HttpStatus status) {
        logger.error("!! Ocurrió una excepción. Status: {}. Mensaje: {}", status, e.getMessage(), e);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", e.getMessage());
        return ResponseEntity.status(status).body(errorResponse);
    }
}