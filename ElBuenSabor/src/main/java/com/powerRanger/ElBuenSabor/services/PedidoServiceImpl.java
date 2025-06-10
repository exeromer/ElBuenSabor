package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- WEBSOCKETS: Import necesario
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class PedidoServiceImpl implements PedidoService {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CarritoRepository carritoRepository;
    @Autowired private CarritoService carritoService;
    @Autowired private ArticuloManufacturadoRepository articuloManufacturadoRepository;
    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private LocalidadRepository localidadRepository;

    // <-- WEBSOCKETS: Inyección del template para enviar mensajes
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // --- MAPPERS (Sin cambios en los mappers) ---
    private ArticuloSimpleResponseDTO convertArticuloToSimpleDto(Articulo articulo) {
        if (articulo == null) return null;
        ArticuloSimpleResponseDTO dto = new ArticuloSimpleResponseDTO();
        dto.setId(articulo.getId());
        dto.setDenominacion(articulo.getDenominacion());
        dto.setPrecioVenta(articulo.getPrecioVenta());
        return dto;
    }

    private DetallePedidoResponseDTO convertDetallePedidoToDto(DetallePedido detalle) {
        if (detalle == null) return null;
        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
        dto.setId(detalle.getId());
        dto.setCantidad(detalle.getCantidad());
        dto.setSubTotal(detalle.getSubTotal());
        dto.setArticulo(convertArticuloToSimpleDto(detalle.getArticulo()));
        return dto;
    }
    private PaisResponseDTO convertPaisToDto(Pais pais) {
        if (pais == null) return null;
        PaisResponseDTO dto = new PaisResponseDTO();
        dto.setId(pais.getId());
        dto.setNombre(pais.getNombre());
        return dto;
    }

    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) {
        if (provincia == null) return null;
        ProvinciaResponseDTO dto = new ProvinciaResponseDTO();
        dto.setId(provincia.getId());
        dto.setNombre(provincia.getNombre());
        if (provincia.getPais() != null) {
            dto.setPais(convertPaisToDto(provincia.getPais()));
        }
        return dto;
    }

    private LocalidadResponseDTO convertLocalidadToDto(Localidad localidad) {
        if (localidad == null) return null;
        LocalidadResponseDTO dto = new LocalidadResponseDTO();
        dto.setId(localidad.getId());
        dto.setNombre(localidad.getNombre());
        if (localidad.getProvincia() != null) {
            dto.setProvincia(convertProvinciaToDto(localidad.getProvincia()));
        }
        return dto;
    }
    private DomicilioResponseDTO convertDomicilioToDto(Domicilio domicilio) {
        if (domicilio == null) return null;
        DomicilioResponseDTO dto = new DomicilioResponseDTO();
        dto.setId(domicilio.getId());
        dto.setCalle(domicilio.getCalle());
        dto.setNumero(domicilio.getNumero());
        dto.setCp(domicilio.getCp());
        if (domicilio.getLocalidad() != null) {
            dto.setLocalidad(convertLocalidadToDto(domicilio.getLocalidad()));
        }
        return dto;
    }

    private SucursalResponseDTO convertSucursalToDto(Sucursal sucursal) {
        if (sucursal == null) return null;
        SucursalResponseDTO dto = new SucursalResponseDTO();
        dto.setId(sucursal.getId());
        dto.setNombre(sucursal.getNombre());
        return dto;
    }

    private ClienteResponseDTO convertClienteToDto(Cliente cliente) {
        if (cliente == null) return null;
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setEstadoActivo(cliente.getEstadoActivo());
        dto.setFechaBaja(cliente.getFechaBaja());
        if (cliente.getUsuario() != null) {
            dto.setUsuarioId(cliente.getUsuario().getId());
            dto.setUsername(cliente.getUsuario().getUsername());
            dto.setRolUsuario(cliente.getUsuario().getRol());
        }
        if (cliente.getDomicilios() != null && !cliente.getDomicilios().isEmpty()) {
            dto.setDomicilios(cliente.getDomicilios().stream().map(this::convertDomicilioToDto).collect(Collectors.toList()));
        } else {
            dto.setDomicilios(new ArrayList<>());
        }
        return dto;
    }

    private PedidoResponseDTO convertToResponseDto(Pedido pedido) {
        if (pedido == null) return null;
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setHoraEstimadaFinalizacion(pedido.getHoraEstimadaFinalizacion());

        Double subTotalPedido = 0.0;
        if (pedido.getDetalles() != null) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                subTotalPedido += detalle.getSubTotal();
            }
        }
        dto.setSubTotalPedido(subTotalPedido);
        dto.setDescuentoAplicado(pedido.getDescuentoAplicado() != null ? pedido.getDescuentoAplicado() : 0.0);
        dto.setTotal(pedido.getTotal());

        dto.setTotalCosto(pedido.getTotalCosto());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setEstado(pedido.getEstado());
        dto.setTipoEnvio(pedido.getTipoEnvio());
        dto.setFormaPago(pedido.getFormaPago());
        dto.setEstadoActivo(pedido.getEstadoActivo());
        dto.setFechaBaja(pedido.getFechaBaja());

        if (pedido.getSucursal() != null) {
            dto.setSucursal(convertSucursalToDto(pedido.getSucursal()));
        }
        if (pedido.getDomicilio() != null) {
            dto.setDomicilio(convertDomicilioToDto(pedido.getDomicilio()));
        }
        if (pedido.getCliente() != null) {
            dto.setCliente(convertClienteToDto(pedido.getCliente()));
        }
        if (pedido.getDetalles() != null) {
            dto.setDetalles(pedido.getDetalles().stream()
                    .map(this::convertDetallePedidoToDto)
                    .collect(Collectors.toList()));
        }

        dto.setMercadoPagoPaymentId(pedido.getMercadoPagoPaymentId());
        dto.setMercadoPagoPreferenceId(pedido.getMercadoPagoPreferenceId());
        dto.setMercadoPagoPaymentStatus(pedido.getMercadoPagoPaymentStatus());

        return dto;
    }

    // --- LÓGICA DE SERVICIO (Sin cambios en los métodos privados de ayuda) ---
    private LocalTime parseTime(String timeString, String fieldName) throws Exception {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new Exception("El " + fieldName + " no puede estar vacío.");
        }
        try {
            return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
            } catch (DateTimeParseException e2){
                throw new Exception("Formato de " + fieldName + " inválido. Use HH:mm:ss o HH:mm. Valor recibido: " + timeString);
            }
        }
    }

    private void validarFormaDePago(TipoEnvio tipoEnvio, FormaPago formaPago) throws IllegalArgumentException {
        if (tipoEnvio == TipoEnvio.DELIVERY && formaPago != FormaPago.MERCADO_PAGO) {
            throw new IllegalArgumentException("Para pedidos con envío a domicilio (DELIVERY), la única forma de pago aceptada es Mercado Pago.");
        }
        if (tipoEnvio == TipoEnvio.TAKEAWAY &&
                !(formaPago == FormaPago.EFECTIVO || formaPago == FormaPago.MERCADO_PAGO)) {
            throw new IllegalArgumentException("Forma de pago no válida para retiro en local (TAKEAWAY). Opciones: EFECTIVO o MERCADO_PAGO.");
        }
    }

    private Pedido mapAndPreparePedido(PedidoRequestDTO dto, Cliente cliente) throws Exception {
        validarFormaDePago(dto.getTipoEnvio(), dto.getFormaPago());

        Pedido pedido = new Pedido();
        pedido.setFechaPedido(LocalDate.now());
        pedido.setHoraEstimadaFinalizacion(parseTime(dto.getHoraEstimadaFinalizacion(), "hora estimada de finalización"));
        pedido.setTipoEnvio(dto.getTipoEnvio());
        pedido.setFormaPago(dto.getFormaPago());
        pedido.setEstado(Estado.PENDIENTE);
        pedido.setEstadoActivo(true);
        pedido.setCliente(cliente);
        pedido.setDescuentoAplicado(0.0);

        Domicilio domicilio = domicilioRepository.findById(dto.getDomicilioId())
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + dto.getDomicilioId()));
        pedido.setDomicilio(domicilio);

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + dto.getSucursalId()));
        pedido.setSucursal(sucursal);

        double subTotalPedido = 0.0;
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new Exception("El pedido debe contener al menos un detalle.");
        }

        for (DetallePedidoRequestDTO detalleDto : dto.getDetalles()) {
            Articulo articulo = articuloRepository.findById(detalleDto.getArticuloId())
                    .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + detalleDto.getArticuloId()));
            if (Boolean.FALSE.equals(articulo.getEstadoActivo())) {
                throw new Exception("El artículo '" + articulo.getDenominacion() + "' (ID: " + articulo.getId() + ") no está disponible.");
            }
            DetallePedido detalle = new DetallePedido();
            detalle.setArticulo(articulo);
            detalle.setCantidad(detalleDto.getCantidad());
            if (articulo.getPrecioVenta() == null) {
                throw new Exception("El artículo '" + articulo.getDenominacion() + "' (ID: " + articulo.getId() + ") no tiene un precio de venta asignado.");
            }
            double subTotalItem = articulo.getPrecioVenta() * detalleDto.getCantidad();
            detalle.setSubTotal(subTotalItem);
            subTotalPedido += subTotalItem;
            pedido.addDetalle(detalle);
        }

        if (pedido.getTipoEnvio() == TipoEnvio.TAKEAWAY) {
            double descuento = subTotalPedido * 0.10;
            pedido.setDescuentoAplicado(descuento);
            pedido.setTotal(subTotalPedido - descuento);
        } else {
            pedido.setTotal(subTotalPedido);
        }
        return pedido;
    }

    // --- MÉTODOS PÚBLICOS DEL SERVICIO ---

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> getAll() {
        return pedidoRepository.findAll().stream().map(this::convertToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO getById(Integer id) throws Exception {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + id));
        return convertToResponseDto(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> getPedidosByClienteId(Integer clienteId) throws Exception {
        if (!clienteRepository.existsById(clienteId)) {
            throw new Exception("Cliente no encontrado con ID: " + clienteId);
        }
        return pedidoRepository.findByClienteIdAndEstadoActivoTrueOrderByFechaPedidoDesc(clienteId).stream().map(this::convertToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> getPedidosByClienteAuth0Id(String auth0Id) throws Exception {
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id).orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id));
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElseThrow(() -> new Exception("Cliente no encontrado para el usuario " + usuario.getUsername()));
        return getPedidosByClienteId(cliente.getId());
    }

    @Override
    @Transactional
    public PedidoResponseDTO create(@Valid PedidoRequestDTO dto) throws Exception {
        Cliente cliente = clienteRepository.findById(dto.getClienteId()).orElseThrow(() -> new Exception("Cliente no encontrado con ID: " + dto.getClienteId()));
        Pedido pedido = mapAndPreparePedido(dto, cliente);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // <-- WEBSOCKETS: Notificar la creación del nuevo pedido.
        messagingTemplate.convertAndSend("/topic/pedidos-cocina", convertToResponseDto(pedidoGuardado));
        messagingTemplate.convertAndSend("/topic/pedidos-cajero", convertToResponseDto(pedidoGuardado));

        return convertToResponseDto(pedidoGuardado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO createForAuthenticatedClient(String auth0Id, @Valid PedidoRequestDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id).orElseThrow(() -> new Exception("Usuario autenticado (Auth0 ID: " + auth0Id + ") no encontrado en el sistema."));
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElseThrow(() -> new Exception("No se encontró un perfil de Cliente para el usuario: " + usuario.getUsername()));
        Pedido pedido = mapAndPreparePedido(dto, cliente);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // <-- WEBSOCKETS: Notificar la creación del nuevo pedido.
        messagingTemplate.convertAndSend("/topic/pedidos-cocina", convertToResponseDto(pedidoGuardado));
        messagingTemplate.convertAndSend("/topic/pedidos-cajero", convertToResponseDto(pedidoGuardado));

        return convertToResponseDto(pedidoGuardado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO crearPedidoDesdeCarrito(Cliente cliente, @Valid CrearPedidoRequestDTO pedidoRequest) throws Exception {
        // ... (lógica de validación, creación de domicilio, etc. sin cambios) ...
        System.out.println("DEBUG: Iniciando crearPedidoDesdeCarrito para cliente ID: " + cliente.getId());
        validarFormaDePago(pedidoRequest.getTipoEnvio(), pedidoRequest.getFormaPago());

        Carrito carrito = carritoRepository.findByCliente(cliente)
                .orElseThrow(() -> new Exception("No se encontró un carrito para el cliente " + cliente.getEmail()));
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new Exception("El carrito está vacío. No se puede generar el pedido.");
        }

        Domicilio domicilioParaElPedido;
        Localidad localidadDomicilio = localidadRepository.findById(pedidoRequest.getLocalidadIdDomicilio())
                .orElseThrow(() -> new Exception("Localidad no encontrada para el domicilio con ID: " + pedidoRequest.getLocalidadIdDomicilio()));

        Optional<Domicilio> optDomicilioExistente = domicilioRepository.findByCalleAndNumeroAndCpAndLocalidad(
                pedidoRequest.getCalleDomicilio(),
                pedidoRequest.getNumeroDomicilio(),
                pedidoRequest.getCpDomicilio(),
                localidadDomicilio
        );

        if (optDomicilioExistente.isPresent()) {
            domicilioParaElPedido = optDomicilioExistente.get();
        } else {
            Domicilio nuevoDomicilio = new Domicilio();
            nuevoDomicilio.setCalle(pedidoRequest.getCalleDomicilio());
            nuevoDomicilio.setNumero(pedidoRequest.getNumeroDomicilio());
            nuevoDomicilio.setCp(pedidoRequest.getCpDomicilio());
            nuevoDomicilio.setLocalidad(localidadDomicilio);
            domicilioParaElPedido = domicilioRepository.save(nuevoDomicilio);
        }

        if (pedidoRequest.getGuardarDireccionEnPerfil() != null && pedidoRequest.getGuardarDireccionEnPerfil()) {
            boolean yaTieneDomicilio = cliente.getDomicilios().stream()
                    .anyMatch(d -> d.getId().equals(domicilioParaElPedido.getId()));
            if (!yaTieneDomicilio) {
                cliente.addDomicilio(domicilioParaElPedido);
                clienteRepository.save(cliente);
            }
        }

        Sucursal sucursalPedido = sucursalRepository.findById(pedidoRequest.getSucursalId())
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + pedidoRequest.getSucursalId()));
        if (sucursalPedido.getEstadoActivo() == null || !sucursalPedido.getEstadoActivo()) {
            throw new Exception("La sucursal seleccionada no está activa.");
        }

        Map<Integer, Double> insumosAReducirMap = new HashMap<>();
        double subTotalGeneralPedido = 0.0;
        double costoTotalPedido = 0.0;

        for (CarritoItem item : carrito.getItems()) {
            Articulo articuloBaseDelCarrito = item.getArticulo();
            int cantidadPedida = item.getCantidad();
            Articulo articuloDelItem;

            Optional<ArticuloInsumo> optInsumo = articuloInsumoRepository.findById(articuloBaseDelCarrito.getId());
            if (optInsumo.isPresent()) {
                articuloDelItem = optInsumo.get();
                ArticuloInsumo insumo = (ArticuloInsumo) articuloDelItem;
                if (insumo.getEstadoActivo() == null || !insumo.getEstadoActivo()){
                    throw new Exception("El insumo '" + insumo.getDenominacion() + "' ya no está disponible.");
                }
                if (insumo.getStockActual() == null || insumo.getStockActual() < cantidadPedida) {
                    throw new Exception("Stock insuficiente para el insumo: " + insumo.getDenominacion());
                }
                insumosAReducirMap.merge(insumo.getId(), (double) cantidadPedida, Double::sum);
                if (insumo.getPrecioCompra() == null) throw new Exception("El insumo '"+insumo.getDenominacion()+"' no tiene precio de compra.");
                costoTotalPedido += cantidadPedida * insumo.getPrecioCompra();
            } else {
                Optional<ArticuloManufacturado> optManuf = articuloManufacturadoRepository.findById(articuloBaseDelCarrito.getId());
                if (optManuf.isPresent()) {
                    articuloDelItem = optManuf.get();
                    ArticuloManufacturado manufacturado = (ArticuloManufacturado) articuloDelItem;
                    if (manufacturado.getEstadoActivo() == null || !manufacturado.getEstadoActivo()){
                        throw new Exception("El artículo manufacturado '" + manufacturado.getDenominacion() + "' ya no está disponible.");
                    }
                    List<ArticuloManufacturadoDetalle> detallesReceta = manufacturado.getManufacturadoDetalles();
                    if (detallesReceta == null || detallesReceta.isEmpty()) {
                        detallesReceta = articuloManufacturadoRepository.findById(manufacturado.getId())
                                .map(ArticuloManufacturado::getManufacturadoDetalles)
                                .orElseThrow(() -> new Exception("No se pudo recargar el manufacturado " + manufacturado.getDenominacion()));
                    }
                    if (detallesReceta == null || detallesReceta.isEmpty()) {
                        throw new Exception("El artículo manufacturado '" + manufacturado.getDenominacion() + "' no tiene una receta definida.");
                    }
                    double costoManufacturadoUnitario = 0.0;
                    for (ArticuloManufacturadoDetalle detalleRecetaItem : detallesReceta) {
                        ArticuloInsumo insumoComponente = articuloInsumoRepository.findById(detalleRecetaItem.getArticuloInsumo().getId())
                                .orElseThrow(() -> new Exception("Insumo de receta no encontrado: " + detalleRecetaItem.getArticuloInsumo().getDenominacion()));
                        if (insumoComponente.getEstadoActivo() == null || !insumoComponente.getEstadoActivo()){
                            throw new Exception("El insumo componente '" + insumoComponente.getDenominacion() + "' ya no está disponible.");
                        }
                        double cantidadNecesariaTotal = detalleRecetaItem.getCantidad() * cantidadPedida;
                        if (insumoComponente.getStockActual() == null || insumoComponente.getStockActual() < cantidadNecesariaTotal) {
                            throw new Exception("Stock insuficiente del insumo '" + insumoComponente.getDenominacion() + "'.");
                        }
                        insumosAReducirMap.merge(insumoComponente.getId(), cantidadNecesariaTotal, Double::sum);
                        if (insumoComponente.getPrecioCompra() == null) throw new Exception("El insumo componente '"+insumoComponente.getDenominacion()+"' no tiene precio de compra.");
                        costoManufacturadoUnitario += detalleRecetaItem.getCantidad() * insumoComponente.getPrecioCompra();
                    }
                    costoTotalPedido += cantidadPedida * costoManufacturadoUnitario;
                } else {
                    throw new Exception("Artículo con ID " + articuloBaseDelCarrito.getId() + " no es ni Insumo ni Manufacturado.");
                }
            }
            if (articuloDelItem.getPrecioVenta() == null) {
                throw new Exception("El artículo '" + articuloDelItem.getDenominacion() + "' no tiene un precio de venta asignado.");
            }
            subTotalGeneralPedido += cantidadPedida * articuloDelItem.getPrecioVenta();
        }

        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setFechaPedido(LocalDate.now());
        nuevoPedido.setHoraEstimadaFinalizacion(parseTime(pedidoRequest.getHoraEstimadaFinalizacion(), "hora estimada de finalización"));
        nuevoPedido.setDomicilio(domicilioParaElPedido);
        nuevoPedido.setSucursal(sucursalPedido);
        nuevoPedido.setTipoEnvio(pedidoRequest.getTipoEnvio());
        nuevoPedido.setFormaPago(pedidoRequest.getFormaPago());
        nuevoPedido.setEstado(Estado.PENDIENTE);
        nuevoPedido.setEstadoActivo(true);
        nuevoPedido.setTotalCosto(costoTotalPedido);

        for (CarritoItem item : carrito.getItems()) {
            DetallePedido detallePedido = new DetallePedido();
            Articulo articuloDelItem = articuloRepository.findById(item.getArticulo().getId())
                    .orElseThrow(() -> new Exception("Artículo con ID " + item.getArticulo().getId() + " no encontrado al crear detalles."));
            detallePedido.setArticulo(articuloDelItem);
            detallePedido.setCantidad(item.getCantidad());
            if (articuloDelItem.getPrecioVenta() == null) {
                throw new Exception("El artículo '" + articuloDelItem.getDenominacion() + "' no tiene un precio de venta asignado.");
            }
            detallePedido.setSubTotal(item.getCantidad() * articuloDelItem.getPrecioVenta());
            nuevoPedido.addDetalle(detallePedido);
        }

        if (nuevoPedido.getTipoEnvio() == TipoEnvio.TAKEAWAY) {
            double descuento = subTotalGeneralPedido * 0.10;
            nuevoPedido.setDescuentoAplicado(descuento);
            nuevoPedido.setTotal(subTotalGeneralPedido - descuento);
        } else {
            nuevoPedido.setDescuentoAplicado(0.0);
            nuevoPedido.setTotal(subTotalGeneralPedido);
        }

        System.out.println("DEBUG: Iniciando Actualización de Stock...");
        for (Map.Entry<Integer, Double> entry : insumosAReducirMap.entrySet()) {
            ArticuloInsumo insumoAActualizar = articuloInsumoRepository.findById(entry.getKey())
                    .orElseThrow(() -> new Exception("Insumo con ID " + entry.getKey() + " no encontrado para actualizar stock."));
            if(insumoAActualizar.getStockActual() == null) insumoAActualizar.setStockActual(0.0);
            insumoAActualizar.setStockActual(insumoAActualizar.getStockActual() - entry.getValue());
            articuloInsumoRepository.save(insumoAActualizar);
        }
        System.out.println("DEBUG: Actualización de Stock completada.");

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);
        System.out.println("DEBUG: Pedido Guardado con ID: " + pedidoGuardado.getId());

        carritoService.vaciarCarrito(cliente);
        System.out.println("DEBUG: Carrito vaciado para cliente ID: " + cliente.getId());

        // <-- WEBSOCKETS: Notificar la creación del nuevo pedido.
        messagingTemplate.convertAndSend("/topic/pedidos-cocina", convertToResponseDto(pedidoGuardado));
        messagingTemplate.convertAndSend("/topic/pedidos-cajero", convertToResponseDto(pedidoGuardado));

        return convertToResponseDto(pedidoGuardado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO updateEstado(Integer id, Estado nuevoEstado) throws Exception {
        Pedido pedidoExistente = pedidoRepository.findById(id).orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + id));
        Estado estadoActual = pedidoExistente.getEstado();

        if (estadoActual == nuevoEstado) return convertToResponseDto(pedidoExistente);

        // ... (lógica de validación de estados sin cambios) ...
        switch (estadoActual) {
            case ENTREGADO:
                if (nuevoEstado == Estado.CANCELADO) throw new Exception("No se puede cancelar un pedido que ya fue entregado.");
                if (nuevoEstado != Estado.ENTREGADO) throw new Exception("Un pedido entregado no puede cambiar a otro estado.");
                break;
            case CANCELADO:
                if (nuevoEstado != Estado.CANCELADO) throw new Exception("No se puede cambiar el estado de un pedido ya cancelado.");
                break;
            case RECHAZADO:
                if (nuevoEstado != Estado.RECHAZADO) throw new Exception("No se puede cambiar el estado de un pedido ya rechazado.");
                break;
            case PENDIENTE:
                if (nuevoEstado == Estado.ENTREGADO) throw new Exception("Un pedido PENDIENTE debe pasar por PAGADO/PREPARACION/EN_CAMINO antes de ser ENTREGADO.");
                break;
            case PAGADO:
                if (nuevoEstado == Estado.PENDIENTE) throw new Exception("Un pedido PAGADO no puede volver a PENDIENTE.");
                break;
            default:
                break;
        }

        pedidoExistente.setEstado(nuevoEstado);
        Pedido pedidoActualizado = pedidoRepository.save(pedidoExistente);

        // <-- WEBSOCKETS: Notificar el cambio de estado del pedido.
        messagingTemplate.convertAndSend("/topic/pedidos-cocina", convertToResponseDto(pedidoActualizado));
        messagingTemplate.convertAndSend("/topic/pedidos-cajero", convertToResponseDto(pedidoActualizado));

        return convertToResponseDto(pedidoActualizado);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + id));
        if (pedido.getEstado() == Estado.ENTREGADO) {
            throw new Exception("No se puede eliminar (borrado lógico) un pedido que ya fue entregado.");
        }
        if (pedido.getEstado() == Estado.EN_CAMINO || pedido.getEstado() == Estado.PREPARACION) {
            throw new Exception("No se puede eliminar (borrado lógico) un pedido que está en " + pedido.getEstado() + ".");
        }

        pedido.setEstadoActivo(false);
        pedido.setFechaBaja(LocalDate.now());

        if (pedido.getEstado() != Estado.RECHAZADO && pedido.getEstado() != Estado.CANCELADO) {
            pedido.setEstado(Estado.CANCELADO);
        }
        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        // <-- WEBSOCKETS: Notificar la cancelación (borrado lógico) del pedido.
        messagingTemplate.convertAndSend("/topic/pedidos-cocina", convertToResponseDto(pedidoActualizado));
        messagingTemplate.convertAndSend("/topic/pedidos-cajero", convertToResponseDto(pedidoActualizado));
    }

    @Override
    @Transactional
    public PedidoResponseDTO procesarNotificacionMercadoPago(String paymentId, String status, String externalReference) throws Exception {
        Pedido pedido;
        try {
            Integer pedidoId = Integer.parseInt(externalReference);
            pedido = pedidoRepository.findById(pedidoId)
                    .orElseGet(() -> pedidoRepository.findByMercadoPagoPreferenceId(externalReference).orElse(null));
        } catch (NumberFormatException e) {
            pedido = pedidoRepository.findByMercadoPagoPreferenceId(externalReference).orElse(null);
        }

        if (pedido == null) {
            throw new Exception("Pedido no encontrado para la referencia externa de Mercado Pago: " + externalReference);
        }

        pedido.setMercadoPagoPaymentId(paymentId);
        pedido.setMercadoPagoPaymentStatus(status);

        if ("approved".equalsIgnoreCase(status)) {
            pedido.setEstado(Estado.PAGADO);
            System.out.println("INFO: Pedido ID " + pedido.getId() + " pagado exitosamente via Mercado Pago. Payment ID: " + paymentId);
        } else if ("rejected".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "in_mediation".equalsIgnoreCase(status) || "charged_back".equalsIgnoreCase(status) ) {
            pedido.setEstado(Estado.RECHAZADO);
            System.out.println("INFO: Pago para Pedido ID " + pedido.getId() + " fue " + status + " por Mercado Pago. Payment ID: " + paymentId);
        } else {
            System.out.println("INFO: Pedido ID " + pedido.getId() + " recibió notificación de Mercado Pago con estado: " + status + ". Payment ID: " + paymentId);
        }

        Pedido pedidoActualizado = pedidoRepository.save(pedido);

        // <-- WEBSOCKETS: Notificar el cambio de estado por el pago de Mercado Pago.
        messagingTemplate.convertAndSend("/topic/pedidos-cocina", convertToResponseDto(pedidoActualizado));
        messagingTemplate.convertAndSend("/topic/pedidos-cajero", convertToResponseDto(pedidoActualizado));

        return convertToResponseDto(pedidoActualizado);
    }

    @Override
    @Transactional
    public void actualizarPreferenciaMercadoPago(Integer pedidoId, String preferenceId) throws Exception {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + pedidoId + " para actualizar preferenceId de MP."));
        pedido.setMercadoPagoPreferenceId(preferenceId);
        pedidoRepository.save(pedido);
    }
}