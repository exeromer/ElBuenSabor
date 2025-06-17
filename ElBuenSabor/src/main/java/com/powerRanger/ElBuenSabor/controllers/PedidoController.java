package com.powerRanger.ElBuenSabor.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powerRanger.ElBuenSabor.dtos.CrearPedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoEstadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.services.PedidoService;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    // Instancias para Logging y Serialización
    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping
    public ResponseEntity<?> createPedidoForAuthenticatedClient(@Valid @RequestBody PedidoRequestDTO dto, Authentication authentication) {
        logger.info(">> POST /api/pedidos - Creando pedido para cliente.");
        try {
            if (authentication == null && dto.getClienteId() == null) {
                logger.warn("!! Intento de crear pedido sin autenticación ni clienteId.");
                throw new Exception("Para crear un pedido sin autenticación (modo prueba), se requiere clienteId en el DTO.");
            }

            PedidoResponseDTO nuevoPedidoDto;
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String auth0Id = jwt.getSubject();
                logger.info(">> Autenticación por JWT encontrada. Auth0 ID: {}", auth0Id);
                nuevoPedidoDto = pedidoService.createForAuthenticatedClient(auth0Id, dto);
            } else {
                logger.info(">> Creando pedido para clienteId: {}", dto.getClienteId());
                nuevoPedidoDto = pedidoService.create(dto);
            }
            logger.info("<< Pedido creado exitosamente con ID: {}", nuevoPedidoDto.getId());
            return new ResponseEntity<>(nuevoPedidoDto, HttpStatus.CREATED);

        } catch (ConstraintViolationException e) {
            return handleConstraintViolation(e);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/admin")
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
    public ResponseEntity<?> getPedidoById(@PathVariable Integer id) {
        logger.info(">> GET /api/pedidos/{} - Obteniendo pedido por ID.", id);
        try {
            PedidoResponseDTO pedidoDto = pedidoService.getById(id);
            logger.info("<< Pedido con ID: {} encontrado.", id);
            return ResponseEntity.ok(pedidoDto);
        } catch (Exception e) {
            return handleGenericException(e, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/cliente/{clienteId}/desde-carrito")
    public ResponseEntity<?> crearPedidoDesdeCarrito(
            @PathVariable Integer clienteId,
            @Valid @RequestBody CrearPedidoRequestDTO pedidoRequest) {

        try {
            // Log para ver exactamente lo que llega desde el frontend
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

            // Usamos nuestro manejador genérico de excepciones que ya incluye logging del error
            return handleGenericException(e, status);
        }
    }


    @PutMapping("/{id}/estado")
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

    @DeleteMapping("/{id}")
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
        // Descomentar si se quiere ver la traza en la respuesta (NO RECOMENDADO EN PRODUCCIÓN)
        // e.printStackTrace();
        return ResponseEntity.status(status).body(errorResponse);
    }
}