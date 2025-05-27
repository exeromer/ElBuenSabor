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

        Optional<Carrito> carritoOpt = carritoRepository.findByCliente(cliente);
        Carrito carrito;

        if (carritoOpt.isPresent()) {
            carrito = carritoOpt.get();
            // No actualizamos fechaUltimaModificacion solo por obtenerlo,
            // se actualizará cuando haya cambios reales.
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
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido para agregar items al carrito.");
        }
        if (itemRequest == null || itemRequest.getArticuloId() == null || itemRequest.getCantidad() == null || itemRequest.getCantidad() <= 0) {
            throw new Exception("Datos del ítem inválidos.");
        }

        Optional<Carrito> carritoOpt = carritoRepository.findByCliente(cliente);
        Carrito carrito;
        if (carritoOpt.isPresent()) {
            carrito = carritoOpt.get();
        } else {
            carrito = new Carrito();
            carrito.setCliente(cliente);
            // fechaCreacion y fechaUltimaModificacion se establecen en el constructor/save de Carrito
            carrito = carritoRepository.save(carrito);
        }

        Articulo articulo = articuloRepository.findById(itemRequest.getArticuloId())
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + itemRequest.getArticuloId()));

        if (articulo.getEstadoActivo() == null || !articulo.getEstadoActivo()) {
            throw new Exception("El artículo '" + articulo.getDenominacion() + "' no está disponible.");
        }

        Optional<CarritoItem> carritoItemOpt = carritoItemRepository.findByCarritoAndArticulo(carrito, articulo);

        CarritoItem item;
        if (carritoItemOpt.isPresent()) {
            item = carritoItemOpt.get();
            item.setCantidad(item.getCantidad() + itemRequest.getCantidad());
            carritoItemRepository.save(item);
        } else {
            item = new CarritoItem();
            item.setArticulo(articulo);
            item.setCantidad(itemRequest.getCantidad());
            item.setPrecioUnitarioAlAgregar(articulo.getPrecioVenta());
            carrito.addItem(item); // El helper se encarga de la relación bidireccional
        }

        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        Carrito carritoGuardado = carritoRepository.save(carrito);

        return mapCarritoToDto(carritoGuardado);
    }

    @Override
    @Transactional
    public CarritoResponseDTO actualizarCantidadItem(Cliente cliente, Long carritoItemId, int nuevaCantidad) throws Exception {
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido.");
        }
        if (carritoItemId == null) {
            throw new Exception("ID del ítem del carrito no puede ser nulo.");
        }
        if (nuevaCantidad <= 0) {
            // Si la nueva cantidad es 0 o menos, se considera una eliminación.
            // Podríamos llamar a eliminarItemDelCarrito o lanzar error si se prefiere una API más estricta.
            // Por ahora, para seguir la lógica de +/- que puede llevar a 0, lo eliminaremos.
            // Si se quiere que 0 no sea válido aquí, lanzar: throw new Exception("La nueva cantidad debe ser al menos 1.");
            return eliminarItemDelCarrito(cliente, carritoItemId);
        }

        CarritoItem carritoItem = carritoItemRepository.findById(carritoItemId)
                .orElseThrow(() -> new Exception("Ítem de carrito no encontrado con ID: " + carritoItemId));

        // Verificar que el ítem pertenezca al carrito del cliente
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
        if (cliente == null || cliente.getId() == null) {
            throw new Exception("Cliente no válido.");
        }
        if (carritoItemId == null) {
            throw new Exception("ID del ítem del carrito no puede ser nulo.");
        }

        CarritoItem carritoItem = carritoItemRepository.findById(carritoItemId)
                .orElseThrow(() -> new Exception("Ítem de carrito no encontrado con ID: " + carritoItemId));

        // Verificar que el ítem pertenezca al carrito del cliente
        if (carritoItem.getCarrito() == null || carritoItem.getCarrito().getCliente() == null ||
                !carritoItem.getCarrito().getCliente().getId().equals(cliente.getId())) {
            throw new Exception("El ítem no pertenece al carrito del cliente especificado.");
        }

        Carrito carrito = carritoItem.getCarrito();
        // La entidad Carrito tiene orphanRemoval=true y CascadeType.ALL en su lista de items.
        // El método helper removeItem en Carrito se encarga de la bidireccionalidad.
        carrito.removeItem(carritoItem); // Esto quitará el item de la colección
        // y orphanRemoval se encargará de borrarlo de la BD al guardar el carrito.
        // Alternativamente, podríamos hacer carritoItemRepository.delete(carritoItem);
        // pero usar el método de la entidad que maneja la colección es más OO.

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

        Optional<Carrito> carritoOpt = carritoRepository.findByCliente(cliente);
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            // Debido a orphanRemoval=true en Carrito.items, al limpiar la colección
            // y guardar el carrito, los items huérfanos serán eliminados.
            carrito.getItems().clear();
            carrito.setFechaUltimaModificacion(LocalDateTime.now());
            carritoRepository.save(carrito);
            return mapCarritoToDto(carrito);
        } else {
            // Si no hay carrito, no hay nada que vaciar. Devolver un DTO de carrito vacío.
            // O podríamos crear uno nuevo vacío si esa es la lógica deseada.
            // Reutilizar getOrCreateCarrito para consistencia.
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