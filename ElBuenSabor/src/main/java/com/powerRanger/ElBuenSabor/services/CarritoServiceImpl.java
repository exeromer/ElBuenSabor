package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoItemResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.entities.Carrito;
import com.powerRanger.ElBuenSabor.entities.CarritoItem;
import com.powerRanger.ElBuenSabor.entities.Cliente;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.CarritoItemRepository;
import com.powerRanger.ElBuenSabor.repository.CarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    @Transactional
    public CarritoResponseDTO getOrCreateCarrito(Cliente cliente) throws Exception {
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido para obtener o crear carrito.");
        }
        Carrito carrito = carritoRepository.findByCliente(cliente).orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setCliente(cliente);
            return carritoRepository.save(nuevoCarrito);
        });
        return mapCarritoToDto(carrito);
    }

    @Override
    @Transactional
    public CarritoResponseDTO addItemAlCarrito(Cliente cliente, AddItemToCartRequestDTO itemRequest) throws Exception {
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido para agregar items al carrito.");
        }
        if (itemRequest == null || itemRequest.getArticuloId() == null || itemRequest.getCantidad() == null || itemRequest.getCantidad() <= 0) {
            throw new Exception("Datos del ítem inválidos.");
        }
        Carrito carrito = getOrCreateCarritoEntity(cliente);

        Articulo articulo = articuloRepository.findById(itemRequest.getArticuloId())
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + itemRequest.getArticuloId()));

        if (articulo.getEstadoActivo() == null || !articulo.getEstadoActivo()) {
            throw new Exception("El artículo '" + articulo.getDenominacion() + "' no está disponible.");
        }

        Optional<CarritoItem> carritoItemOpt = carritoItemRepository.findByCarritoAndArticulo(carrito, articulo);

        if (carritoItemOpt.isPresent()) {
            CarritoItem item = carritoItemOpt.get();
            item.setCantidad(item.getCantidad() + itemRequest.getCantidad());
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
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido.");
        }
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