package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.PedidoEstadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PedidoResponseDTO; // Importar DTO de respuesta
// import com.powerRanger.ElBuenSabor.entities.Pedido; // Ya no se devuelve entidad
import com.powerRanger.ElBuenSabor.services.PedidoService;
import com.powerRanger.ElBuenSabor.services.UsuarioService; // Sigue siendo necesario si usas Authentication
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
    private UsuarioService usuarioService; // Mantener para el endpoint /mis-pedidos si se usa

    @PostMapping
    public ResponseEntity<?> createPedidoForAuthenticatedClient(@Valid @RequestBody PedidoRequestDTO dto, Authentication authentication) {
        try {
            if (authentication == null && dto.getClienteId() == null) { // Modo prueba sin token y sin clienteId
                throw new Exception("Para crear un pedido sin autenticación (modo prueba), se requiere clienteId en el DTO.");
            }

            PedidoResponseDTO nuevoPedidoDto;
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                String auth0Id = jwt.getSubject();
                nuevoPedidoDto = pedidoService.createForAuthenticatedClient(auth0Id, dto);
            } else { // Sin autenticación (permitAll) o clienteId provisto para admin
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