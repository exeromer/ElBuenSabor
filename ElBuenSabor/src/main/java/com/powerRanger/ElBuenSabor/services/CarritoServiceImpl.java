package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.AddItemToCartRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoItemResponseDTO;
import com.powerRanger.ElBuenSabor.dtos.CarritoResponseDTO;
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
    public CarritoResponseDTO getOrCreateCarrito(Cliente cliente) throws Exception { // Firma coincide con la interfaz
        validarPropietarioCliente(cliente); // Validación de propiedad

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
        return mapCarritoToDto(carrito); // Asegúrate que mapCarritoToDto esté definido
    }

    @Override
    @Transactional
    public CarritoResponseDTO addItemAlCarrito(Cliente cliente, AddItemToCartRequestDTO itemRequest) throws Exception {
        validarPropietarioCliente(cliente); // Asumiendo que tienes este método

        if (itemRequest == null || itemRequest.getArticuloId() == null || itemRequest.getCantidad() == null || itemRequest.getCantidad() <= 0) {
            throw new Exception("Datos del ítem inválidos.");
        }

        Articulo articulo = articuloRepository.findById(itemRequest.getArticuloId())
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + itemRequest.getArticuloId()));

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
                cantidadSolicitada = itemExistente.getCantidad() + itemRequest.getCantidad(); // Cantidad total si ya existe
            }
        } else {
            carrito = new Carrito();
            carrito.setCliente(cliente);
            // No es necesario guardar el carrito aquí todavía, se guardará con el item o al final.
        }

        // --- Validación de Stock en Backend ---
        if (articulo instanceof ArticuloInsumo) {
            ArticuloInsumo insumo = (ArticuloInsumo) articulo;
            if (insumo.getStockActual() == null || insumo.getStockActual() < cantidadSolicitada) {
                throw new Exception("Stock insuficiente para el insumo: " + insumo.getDenominacion() +
                        ". Solicitado total: " + cantidadSolicitada +
                        ", Disponible: " + (insumo.getStockActual() != null ? insumo.getStockActual() : 0));
            }
        } else if (articulo instanceof ArticuloManufacturado) {
            ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;
            // La validación de estadoActivo ya está hecha arriba para todos los artículos.
            // Aquí, podrías decidir NO hacer una verificación profunda de stock de componentes,
            // ya que el frontend ya tuvo la información de unidadesDisponiblesCalculadas
            // y la validación final y atómica ocurrirá en PedidoServiceImpl.crearPedidoDesdeCarrito.
            // Esto evita duplicar la lógica compleja o añadir dependencias de servicio innecesarias aquí.
            System.out.println("INFO: Añadiendo ArticuloManufacturado '" + manufacturado.getDenominacion() +
                    "' al carrito. El stock de componentes se validará exhaustivamente al confirmar el pedido.");

            // Si quisieras una verificación LIGERA aquí, podrías cargar el manufacturado con sus detalles
            // y usar tu método `calcularUnidadesDisponibles` (si lo hicieras accesible o lo replicaras de forma simplificada).
            // Pero por ahora, para no complicar `CarritoService` con la lógica de cálculo de manufacturados,
            // podemos confiar en la validación de `PedidoService`.
        }
        // --- Fin Validación de Stock ---


        if (itemExistente != null) {
            itemExistente.setCantidad(cantidadSolicitada);
            carritoItemRepository.save(itemExistente);
        } else {
            itemExistente = new CarritoItem();
            itemExistente.setArticulo(articulo);
            itemExistente.setCantidad(itemRequest.getCantidad()); // Cantidad inicial a agregar
            itemExistente.setPrecioUnitarioAlAgregar(articulo.getPrecioVenta());
            // Es importante asegurar que el carrito se guarde si es nuevo antes de añadir items, para manejar la persistencia en cascada.
            if (carrito.getId() == null) { // Si el carrito era nuevo y no se guardó
                carrito = carritoRepository.save(carrito);
            }
            itemExistente.setCarrito(carrito); // Establecer la relación
            carrito.addItem(itemExistente); // El método addItem puede ya hacer item.setCarrito(this)
        }

        carrito.setFechaUltimaModificacion(LocalDateTime.now());
        Carrito carritoGuardado = carritoRepository.save(carrito); // Guarda el carrito y por cascada los items nuevos/modificados

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
        validarPropietarioCliente(cliente);

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
        validarPropietarioCliente(cliente);
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