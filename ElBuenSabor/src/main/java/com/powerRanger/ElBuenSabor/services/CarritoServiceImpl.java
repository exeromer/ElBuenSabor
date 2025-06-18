package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoItemResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.UpdateCartItemQuantityRequestDTO; // Nuevo DTO importado
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private ArticuloRepository articuloRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private void validarPropietarioCliente(Cliente clienteDelPath) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String auth0Id = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            auth0Id = jwt.getSubject();
        }

        if (auth0Id == null) {
            throw new AccessDeniedException("No se pudo determinar el usuario autenticado.");
        }

        Usuario usuarioAutenticado = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado en la BD."));

        Cliente clienteDelToken = clienteRepository.findByUsuarioId(usuarioAutenticado.getId())
                .orElseThrow(() -> new AccessDeniedException("Perfil de cliente no encontrado para el usuario autenticado."));

        if (!clienteDelToken.getId().equals(clienteDelPath.getId())) {
            throw new AccessDeniedException("Acceso denegado: No puedes realizar esta acción en recursos de otro cliente.");
        }
    }

    @Override
    @Transactional
    public CarritoResponseDTO getOrCreateCarrito(Cliente cliente) throws Exception {
        validarPropietarioCliente(cliente);

        Optional<Carrito> carritoOpt = carritoRepository.findByCliente(cliente);
        Carrito carrito;
        if (carritoOpt.isPresent()) {
            carrito = carritoOpt.get();
        } else {
            carrito = new Carrito();
            carrito.setCliente(cliente);
            carrito = carritoRepository.save(carrito);
        }
        return mapCarritoToDto(carrito);
    }

    @Override
    @Transactional
    public CarritoResponseDTO addItemAlCarrito(Cliente cliente, AddItemToCartRequestDTO itemRequest) throws Exception {
        validarPropietarioCliente(cliente);

        if (itemRequest == null || itemRequest.getArticuloId() == null || itemRequest.getCantidad() == null || itemRequest.getCantidad() <= 0) {
            throw new Exception("Datos del ítem inválidos.");
        }

        Articulo articulo = articuloRepository.findById(itemRequest.getArticuloId())
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + itemRequest.getArticuloId()));

        // La única validación de stock aquí es que el artículo esté activo.
        // La validación detallada de stock por sucursal se realiza al crear el pedido.
        if (articulo.getEstadoActivo() == null || !articulo.getEstadoActivo()) {
            throw new Exception("El artículo '" + articulo.getDenominacion() + "' no está disponible (inactivo).");
        }

        int cantidadSolicitada = itemRequest.getCantidad();
        CarritoItem itemExistente = null;
        Optional<Carrito> carritoOpt = carritoRepository.findByCliente(cliente);
        Carrito carrito;

        if (carritoOpt.isPresent()) {
            carrito = carritoOpt.get();
            Optional<CarritoItem> carritoItemOpt = carritoItemRepository.findByCarritoAndArticulo(carrito, articulo);
            if (carritoItemOpt.isPresent()) {
                itemExistente = carritoItemOpt.get();
                cantidadSolicitada = itemExistente.getCantidad() + itemRequest.getCantidad();
            }
        } else {
            carrito = new Carrito();
            carrito.setCliente(cliente);
        }

        if (itemExistente != null) {
            itemExistente.setCantidad(cantidadSolicitada);
            carritoItemRepository.save(itemExistente);
        } else {
            itemExistente = new CarritoItem();
            itemExistente.setArticulo(articulo);
            itemExistente.setCantidad(itemRequest.getCantidad());
            itemExistente.setPrecioUnitarioAlAgregar(articulo.getPrecioVenta());
            if (carrito.getId() == null) {
                carrito = carritoRepository.save(carrito);
            }
            itemExistente.setCarrito(carrito);
            carrito.addItem(itemExistente);
        }

        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        Carrito carritoGuardado = carritoRepository.save(carrito);

        return mapCarritoToDto(carritoGuardado);
    }

    @Override
    @Transactional
    public CarritoResponseDTO actualizarCantidadItem(Cliente cliente, Long carritoItemId, int nuevaCantidad) throws Exception {
        validarPropietarioCliente(cliente);
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido.");
        }
        if (carritoItemId == null) {
            throw new Exception("ID del ítem del carrito no puede ser nulo.");
        }
        if (nuevaCantidad <= 0) {
            // Si la nueva cantidad es 0 o menos, se considera una eliminación.
            return eliminarItemDelCarrito(cliente, carritoItemId);
        }

        CarritoItem carritoItem = carritoItemRepository.findById(carritoItemId)
                .orElseThrow(() -> new Exception("Ítem de carrito no encontrado con ID: " + carritoItemId));

        if (carritoItem.getCarrito() == null || carritoItem.getCarrito().getCliente() == null ||
                !carritoItem.getCarrito().getCliente().getId().equals(cliente.getId())) {
            throw new Exception("El ítem no pertenece al carrito del cliente especificado.");
        }

        carritoItem.setCantidad(nuevaCantidad);
        carritoItemRepository.save(carritoItem);

        Carrito carrito = carritoItem.getCarrito();
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        carritoRepository.save(carrito);

        return mapCarritoToDto(carrito);
    }

    @Override
    @Transactional
    public CarritoResponseDTO eliminarItemDelCarrito(Cliente cliente, Long carritoItemId) throws Exception {
        validarPropietarioCliente(cliente);

        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido.");
        }
        if (carritoItemId == null) {
            throw new Exception("ID del ítem del carrito no puede ser nulo.");
        }

        CarritoItem carritoItem = carritoItemRepository.findById(carritoItemId)
                .orElseThrow(() -> new Exception("Ítem de carrito no encontrado con ID: " + carritoItemId));

        if (carritoItem.getCarrito() == null || carritoItem.getCarrito().getCliente() == null ||
                !carritoItem.getCarrito().getCliente().getId().equals(cliente.getId())) {
            throw new Exception("El ítem no pertenece al carrito del cliente especificado.");
        }

        Carrito carrito = carritoItem.getCarrito();
        carrito.removeItem(carritoItem);

        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        carritoRepository.save(carrito);

        return mapCarritoToDto(carrito);
    }

    @Override
    @Transactional
    public CarritoResponseDTO vaciarCarrito(Cliente cliente) throws Exception {
        validarPropietarioCliente(cliente);
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido.");
        }

        Optional<Carrito> carritoOpt = carritoRepository.findByCliente(cliente);
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            carrito.getItems().clear();
            carrito.setFechaUltimaModificacion(LocalDateTime.now());
            carritoRepository.save(carrito);
            return mapCarritoToDto(carrito);
        } else {
            return getOrCreateCarrito(cliente);
        }
    }

    private CarritoResponseDTO mapCarritoToDto(Carrito carrito) {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        if (carrito.getCliente() != null) {
            dto.setClienteId(carrito.getCliente().getId());
        }
        dto.setFechaCreacion(carrito.getFechaCreacion());
        dto.setFechaUltimaModificacion(carrito.getFechaUltimaModificacion());

        double totalGeneralCarrito = 0.0;

        if (carrito.getItems() != null) {
            dto.setItems(carrito.getItems().stream().map(itemEntity -> {
                CarritoItemResponseDTO itemDto = new CarritoItemResponseDTO();
                itemDto.setId(itemEntity.getId());
                if (itemEntity.getArticulo() != null) {
                    itemDto.setArticuloId(itemEntity.getArticulo().getId());
                    itemDto.setArticuloDenominacion(itemEntity.getArticulo().getDenominacion());
                }
                itemDto.setCantidad(itemEntity.getCantidad());
                itemDto.setPrecioUnitarioAlAgregar(itemEntity.getPrecioUnitarioAlAgregar());

                double subtotalItem = 0.0;
                if (itemEntity.getCantidad() != null && itemEntity.getPrecioUnitarioAlAgregar() != null) {
                    subtotalItem = itemEntity.getCantidad() * itemEntity.getPrecioUnitarioAlAgregar();
                }
                itemDto.setSubtotalItem(subtotalItem);
                return itemDto;
            }).collect(Collectors.toList()));

            totalGeneralCarrito = dto.getItems().stream()
                    .mapToDouble(CarritoItemResponseDTO::getSubtotalItem)
                    .sum();
        }
        dto.setTotalCarrito(totalGeneralCarrito);
        return dto;
    }
}