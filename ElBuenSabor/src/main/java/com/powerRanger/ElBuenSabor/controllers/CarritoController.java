package com.powerRanger.ElBuenSabor.controllers;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.UpdateCartItemQuantityRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.repository.ClienteRepository;
import com.powerRanger.ElBuenSabor.services.CarritoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/{clienteId}/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente findClienteById(Integer clienteId) throws Exception {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + clienteId));
    }

    @GetMapping
    public ResponseEntity<CarritoResponseDTO> getCarritoDelCliente(@PathVariable Integer clienteId) throws Exception {
        Cliente cliente = findClienteById(clienteId);
        CarritoResponseDTO carrito = carritoService.getOrCreateCarrito(cliente);
        return ResponseEntity.ok(carrito);
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponseDTO> agregarItemAlCarrito(
            @PathVariable Integer clienteId,
            @Valid @RequestBody AddItemToCartRequestDTO itemRequest) throws Exception {
        Cliente cliente = findClienteById(clienteId);
        CarritoResponseDTO carritoActualizado = carritoService.addItemAlCarrito(cliente, itemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoActualizado);
    }

    @PutMapping("/items/{carritoItemId}")
    public ResponseEntity<CarritoResponseDTO> actualizarCantidadItemDelCarrito(
            @PathVariable Integer clienteId,
            @PathVariable Integer carritoItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequestDTO cantidadRequest) throws Exception {
        Cliente cliente = findClienteById(clienteId);
        CarritoResponseDTO carritoActualizado = carritoService.actualizarCantidadItem(cliente, carritoItemId, cantidadRequest.getNuevaCantidad());
        return ResponseEntity.ok(carritoActualizado);
    }

    @DeleteMapping("/items/{carritoItemId}")
    public ResponseEntity<CarritoResponseDTO> eliminarItemDelCarrito(
            @PathVariable Integer clienteId,
            @PathVariable Integer carritoItemId) throws Exception {
        Cliente cliente = findClienteById(clienteId);
        CarritoResponseDTO carritoActualizado = carritoService.eliminarItemDelCarrito(cliente, carritoItemId);
        return ResponseEntity.ok(carritoActualizado);
    }

    @DeleteMapping("/items")
    public ResponseEntity<CarritoResponseDTO> vaciarCarritoDelCliente(@PathVariable Integer clienteId) throws Exception {
        Cliente cliente = findClienteById(clienteId);
        CarritoResponseDTO carritoVaciado = carritoService.vaciarCarrito(cliente);
        return ResponseEntity.ok(carritoVaciado);
    }
}