package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoItemResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        if (articulo.getEstadoActivo() == null || !articulo.getEstadoActivo()) {
            throw new Exception("El artículo '" + articulo.getDenominacion() + "' no está disponible (inactivo).");
        }

        Carrito carrito = getOrCreateCarritoEntity(cliente);
        Optional<CarritoItem> carritoItemOpt = carritoItemRepository.findByCarritoAndArticulo(carrito, articulo);
        int cantidadSolicitada = itemRequest.getCantidad() + (carritoItemOpt.isPresent() ? carritoItemOpt.get().getCantidad() : 0);

        if (articulo instanceof ArticuloInsumo) {
            ArticuloInsumo insumo = (ArticuloInsumo) articulo;
            if (insumo.getStockActual() == null || insumo.getStockActual() < cantidadSolicitada) {
                throw new Exception("Stock insuficiente para el insumo: " + insumo.getDenominacion() +
                        ". Solicitado total: " + cantidadSolicitada +
                        ", Disponible: " + (insumo.getStockActual() != null ? insumo.getStockActual() : 0));
            }
        }
        
        if (carritoItemOpt.isPresent()) {
            CarritoItem item = carritoItemOpt.get();
            item.setCantidad(cantidadSolicitada); 
            carritoItemRepository.save(item);
        } else {
            CarritoItem item = new CarritoItem();
            item.setArticulo(articulo);
            item.setCantidad(itemRequest.getCantidad());
            item.setPrecioUnitarioAlAgregar(articulo.getPrecioVenta());
            carrito.addItem(item);
        }

        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        Carrito carritoGuardado = carritoRepository.save(carrito);
        return mapCarritoToDto(carritoGuardado);
    }


    @Override
    @Transactional
    public CarritoResponseDTO actualizarCantidadItem(Cliente cliente, Integer carritoItemId, int nuevaCantidad) throws Exception {
        validarPropietarioCliente(cliente);
        if (nuevaCantidad <= 0) {
            return eliminarItemDelCarrito(cliente, carritoItemId);
        }
        CarritoItem carritoItem = findAndValidateCarritoItem(cliente, carritoItemId);
        carritoItem.setCantidad(nuevaCantidad);
        carritoItemRepository.save(carritoItem);

        Carrito carrito = carritoItem.getCarrito();
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        carritoRepository.save(carrito);

        return mapCarritoToDto(carrito);
    }

    @Override
    @Transactional
    public CarritoResponseDTO eliminarItemDelCarrito(Cliente cliente, Integer carritoItemId) throws Exception {
        validarPropietarioCliente(cliente);
        CarritoItem carritoItem = findAndValidateCarritoItem(cliente, carritoItemId);
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
        Carrito carrito = carritoRepository.findByCliente(cliente)
                .orElseThrow(() -> new Exception("No se encontró un carrito para el cliente " + cliente.getEmail()));
        carrito.getItems().clear();
        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        carritoRepository.save(carrito);
        return mapCarritoToDto(carrito);
    }

    private Carrito getOrCreateCarritoEntity(Cliente cliente) {
        return carritoRepository.findByCliente(cliente).orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setCliente(cliente);
            return carritoRepository.save(nuevoCarrito);
        });
    }

    private CarritoItem findAndValidateCarritoItem(Cliente cliente, Integer carritoItemId) throws Exception {
        if (carritoItemId == null) {
            throw new Exception("ID del ítem del carrito no puede ser nulo.");
        }
        CarritoItem carritoItem = carritoItemRepository.findById(carritoItemId)
                .orElseThrow(() -> new Exception("Ítem de carrito no encontrado con ID: " + carritoItemId));
        if (carritoItem.getCarrito() == null || carritoItem.getCarrito().getCliente() == null ||
                !carritoItem.getCarrito().getCliente().getId().equals(cliente.getId())) {
            throw new Exception("El ítem no pertenece al carrito del cliente especificado.");
        }
        return carritoItem;
    }

    private CarritoResponseDTO mapCarritoToDto(Carrito carrito) {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        if (carrito.getCliente() != null) {
            dto.setClienteId(carrito.getCliente().getId());
        }
        dto.setFechaCreacion(carrito.getFechaCreacion());
        dto.setFechaUltimaModificacion(carrito.getFechaUltimaModificacion());
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
                itemDto.setSubtotalItem(itemEntity.getSubtotalItem());
                return itemDto;
            }).collect(Collectors.toList()));
        } else {
             dto.setItems(new ArrayList<>());
        }
        double totalGeneralCarrito = dto.getItems().stream()
                .mapToDouble(CarritoItemResponseDTO::getSubtotalItem)
                .sum();
        dto.setTotalCarrito(totalGeneralCarrito);
        return dto;
    }
}