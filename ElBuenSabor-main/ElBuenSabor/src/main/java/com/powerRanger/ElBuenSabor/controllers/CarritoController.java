package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.UpdateCartItemQuantityRequestDTO; // Nuevo DTO importado
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.services.CarritoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes/{clienteId}/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping
    public ResponseEntity<?> getCarritoDelCliente(@PathVariable Integer clienteId) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));
            CarritoResponseDTO carrito = carritoService.getOrCreateCarrito(cliente);
            return ResponseEntity.ok(carrito);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    @PostMapping("/items")
    public ResponseEntity<?> agregarItemAlCarrito(
            @PathVariable Integer clienteId,
            @Valid @RequestBody AddItemToCartRequestDTO itemRequest) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));

            CarritoResponseDTO carritoActualizado = carritoService.addItemAlCarrito(cliente, itemRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(carritoActualizado);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("no encontrado")) {
                status = HttpStatus.NOT_FOUND;
            }
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    /**
     * Actualiza la cantidad de un ítem específico en el carrito del cliente.
     * Si la nuevaCantidad es 0, el ítem será eliminado del carrito.
     */
    @PutMapping("/items/{carritoItemId}")
    public ResponseEntity<?> actualizarCantidadItemDelCarrito(
            @PathVariable Integer clienteId,
            @PathVariable Long carritoItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequestDTO cantidadRequest) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));

            CarritoResponseDTO carritoActualizado = carritoService.actualizarCantidadItem(cliente, carritoItemId, cantidadRequest.getNuevaCantidad());
            return ResponseEntity.ok(carritoActualizado);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("no pertenece")) {
                status = HttpStatus.NOT_FOUND;
            }
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    /**
     * Elimina un ítem específico del carrito del cliente.
     */
    @DeleteMapping("/items/{carritoItemId}")
    public ResponseEntity<?> eliminarItemDelCarrito(
            @PathVariable Integer clienteId,
            @PathVariable Long carritoItemId) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));

            CarritoResponseDTO carritoActualizado = carritoService.eliminarItemDelCarrito(cliente, carritoItemId);
            return ResponseEntity.ok(carritoActualizado);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("no pertenece")) {
                status = HttpStatus.NOT_FOUND;
            }
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(status).body(errorResponse);
        }
    }

    /**
     * Vacía todos los ítems del carrito del cliente.
     */
    @DeleteMapping("/items") // DELETE a la colección de items para vaciarla
    public ResponseEntity<?> vaciarCarritoDelCliente(@PathVariable Integer clienteId) {
        try {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));

            CarritoResponseDTO carritoVaciado = carritoService.vaciarCarrito(cliente);
            return ResponseEntity.ok(carritoVaciado); // Devuelve el carrito vacío
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            HttpStatus status = e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            errorResponse.put("status", status.value());
            errorResponse.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(status).body(errorResponse);
        }
    }
}