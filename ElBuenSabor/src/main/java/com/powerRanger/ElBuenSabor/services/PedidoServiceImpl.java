package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.FormaPago;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import com.powerRanger.ElBuenSabor.entities.enums.TipoEnvio;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime; // Importar LocalDateTime
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Set;    // ¡Importar Set!
import java.util.HashSet; // ¡Importar HashSet!

import com.powerRanger.ElBuenSabor.dtos.MercadoPagoCreatePreferenceDTO;
import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.entities.enums.TipoPromocion; // Importar TipoPromocion desde entities.enums

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
    @Autowired private MercadoPagoService mercadoPagoService;
    @Autowired private StockInsumoSucursalService stockInsumoSucursalService;
    @Autowired private PromocionService promocionService; // ¡Inyectamos PromocionService!
    private static final Logger logger = LoggerFactory.getLogger(PedidoServiceImpl.class);

    // --- MAPPERS (Como los tenías) ---
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
        // Añadir campos de promoción al DTO
        if (detalle.getPromocionAplicada() != null) {
            dto.setPromocionAplicadaId(detalle.getPromocionAplicada().getId());
        }
        dto.setDescuentoAplicadoPorPromocion(detalle.getDescuentoAplicadoPorPromocion());
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
        dto.setTotal(pedido.getTotal());
        dto.setTotalCosto(pedido.getTotalCosto());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setEstado(pedido.getEstado());
        dto.setTipoEnvio(pedido.getTipoEnvio());
        dto.setFormaPago(pedido.getFormaPago());
        dto.setEstadoActivo(pedido.getEstadoActivo());
        dto.setFechaBaja(pedido.getFechaBaja());
        dto.setDescuentoAplicado(pedido.getDescuentoAplicado());
        dto.setMpPreferenceId(pedido.getMpPreferenceId());


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
        return dto;
    }

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

    private Pedido mapAndPreparePedido(PedidoRequestDTO dto, Cliente cliente) throws Exception {
        Pedido pedido = new Pedido();
        pedido.setFechaPedido(LocalDate.now());
        pedido.setHoraEstimadaFinalizacion(parseTime(dto.getHoraEstimadaFinalizacion(), "hora estimada de finalización"));
        pedido.setTipoEnvio(dto.getTipoEnvio());
        pedido.setFormaPago(dto.getFormaPago());
        pedido.setEstado(Estado.PENDIENTE);
        pedido.setEstadoActivo(true);
        pedido.setCliente(cliente);

        Domicilio domicilio = domicilioRepository.findById(dto.getDomicilioId())
                .orElseThrow(() -> new Exception("Domicilio no encontrado con ID: " + dto.getDomicilioId()));
        pedido.setDomicilio(domicilio);

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + dto.getSucursalId()));
        pedido.setSucursal(sucursal);

        double totalPedido = 0.0;
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
            double subTotal = articulo.getPrecioVenta() * detalleDto.getCantidad();
            detalle.setSubTotal(subTotal);
            totalPedido += subTotal;
            pedido.addDetalle(detalle);
        }
        pedido.setTotal(totalPedido);
        return pedido;
    }

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
        return convertToResponseDto(pedidoGuardado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO createForAuthenticatedClient(String auth0Id, @Valid PedidoRequestDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id).orElseThrow(() -> new Exception("Usuario autenticado (Auth0 ID: " + auth0Id + ") no encontrado en el sistema."));
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElseThrow(() -> new Exception("No se encontró un perfil de Cliente para el usuario: " + usuario.getUsername()));
        Pedido pedido = mapAndPreparePedido(dto, cliente);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        return convertToResponseDto(pedidoGuardado);
    }

    @Override
    @Transactional
    public PedidoResponseDTO crearPedidoDesdeCarrito(Cliente cliente, @Valid CrearPedidoRequestDTO pedidoRequest) throws Exception {
        logger.info("INICIO - crearPedidoDesdeCarrito para Cliente ID: {}", cliente.getId());
        Carrito carrito = carritoRepository.findByCliente(cliente)
                .orElseThrow(() -> new Exception("No se encontró un carrito para el cliente " + cliente.getEmail()));
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            logger.warn("El carrito ID: {} está vacío. Abortando creación de pedido.", carrito.getId());
            throw new Exception("El carrito está vacío. No se puede generar el pedido.");
        }
        logger.info("Carrito ID: {} encontrado con {} items.", carrito.getId(), carrito.getItems().size());

        // --- Lógica de Domicilio: Buscar o Crear ---
        logger.info("Procesando domicilio: Calle {}, N° {}, CP {}", pedidoRequest.getCalleDomicilio(), pedidoRequest.getNumeroDomicilio(), pedidoRequest.getCpDomicilio());
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
            logger.info("Se usará domicilio existente encontrado. ID: {}", domicilioParaElPedido.getId());
        } else {
            Domicilio nuevoDomicilio = new Domicilio();
            nuevoDomicilio.setCalle(pedidoRequest.getCalleDomicilio());
            nuevoDomicilio.setNumero(pedidoRequest.getNumeroDomicilio());
            nuevoDomicilio.setCp(pedidoRequest.getCpDomicilio());
            nuevoDomicilio.setLocalidad(localidadDomicilio);
            domicilioParaElPedido = domicilioRepository.save(nuevoDomicilio);
            logger.info("Se creó un nuevo domicilio. ID: {}", domicilioParaElPedido.getId());
        }

        if (pedidoRequest.getGuardarDireccionEnPerfil() != null && pedidoRequest.getGuardarDireccionEnPerfil()) {
            boolean yaTieneDomicilio = cliente.getDomicilios().stream()
                    .anyMatch(d -> d.getId().equals(domicilioParaElPedido.getId()));
            if (!yaTieneDomicilio) {
                cliente.addDomicilio(domicilioParaElPedido);
                clienteRepository.save(cliente);
                logger.info("Domicilio ID {} asociado al perfil del cliente ID {}", domicilioParaElPedido.getId(), cliente.getId());
            }
        }

        // --- Fin Lógica de Domicilio ---

        Sucursal sucursalPedido = sucursalRepository.findById(pedidoRequest.getSucursalId())
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + pedidoRequest.getSucursalId()));
        if (sucursalPedido.getEstadoActivo() == null || !sucursalPedido.getEstadoActivo()) {
            logger.warn("Intento de pedido a sucursal inactiva ID: {}", sucursalPedido.getId());
            throw new Exception("La sucursal seleccionada no está activa.");
        }
        logger.info("Sucursal validada. ID: {}", sucursalPedido.getId());

        // --- Obtener promociones activas para la sucursal ---
        List<PromocionResponseDTO> promocionesActivasDTO = promocionService.getPromocionesActivasPorSucursal(
                sucursalPedido.getId(),
                LocalDateTime.now().toLocalDate(),
                LocalDateTime.now().toLocalTime()
        );
        logger.info("Se encontraron {} promociones activas para la sucursal ID {}.", promocionesActivasDTO.size(), sucursalPedido.getId());

        // --- Lógica de Stock por Sucursal ---
        Map<Integer, Double> insumosAReducirMap = new HashMap<>();
        logger.info("Iniciando Pre-Verificación de Stock...");

        Set<Integer> articulosProcesadosPorCombo = new HashSet<>();

        for (CarritoItem item : carrito.getItems()) {
            Articulo articuloBaseDelCarrito = item.getArticulo();
            int cantidadPedida = item.getCantidad();
            logger.info("Verificando stock para CarritoItem ID: {}, Articulo ID: {} ('{}'), Clase Proxy Inicial: {}, Cantidad: {}", item.getId(), articuloBaseDelCarrito.getId(), articuloBaseDelCarrito.getDenominacion(), articuloBaseDelCarrito.getClass().getName(), cantidadPedida);

            // Determinar si es ArticuloInsumo o ArticuloManufacturado
            Optional<ArticuloInsumo> optInsumoStock = articuloInsumoRepository.findById(articuloBaseDelCarrito.getId());
            if (optInsumoStock.isPresent()) {
                ArticuloInsumo insumo = optInsumoStock.get();
                logger.info("Es ArticuloInsumo: {}, ID: {}", insumo.getDenominacion(), insumo.getId());

                if (Boolean.TRUE.equals(insumo.getEsParaElaborar())) {
                    logger.warn("El artículo insumo '{}' (ID: {}) es 'para elaborar' y se intentó vender directamente. Su stock NO se gestiona para venta directa en esta lógica.", insumo.getDenominacion(), insumo.getId());
                    throw new Exception("El insumo '" + insumo.getDenominacion() + "' (ID: " + insumo.getId() + ") no se puede vender directamente. Está marcado para elaboración.");
                }

                StockInsumoSucursalResponseDTO stockInsumoSucursal = stockInsumoSucursalService.getStockByInsumoAndSucursal(
                        insumo.getId(), sucursalPedido.getId()
                );
                Double stockActualInsumoSucursal = stockInsumoSucursal.getStockActual();

                if (stockActualInsumoSucursal == null || stockActualInsumoSucursal < cantidadPedida) {
                    throw new Exception("Stock insuficiente para el insumo: " + insumo.getDenominacion() +
                            " en la sucursal " + sucursalPedido.getNombre() +
                            ". Solicitado: " + cantidadPedida +
                            ", Disponible: " + (stockActualInsumoSucursal != null ? stockActualInsumoSucursal : 0.0));
                }
                insumosAReducirMap.merge(insumo.getId(), (double) cantidadPedida, Double::sum);

            } else { // Es ArticuloManufacturado
                Optional<ArticuloManufacturado> optManufStock = articuloManufacturadoRepository.findById(articuloBaseDelCarrito.getId());
                if (optManufStock.isPresent()) {
                    ArticuloManufacturado manufacturado = optManufStock.get();
                    logger.info("Es ArticuloManufacturado: {}, ID: {}", manufacturado.getDenominacion(), manufacturado.getId());

                    if (Boolean.FALSE.equals(manufacturado.getEstadoActivo())){
                        throw new Exception("El artículo manufacturado '" + manufacturado.getDenominacion() + "' ya no está disponible.");
                    }

                    List<ArticuloManufacturadoDetalle> detallesReceta = manufacturado.getManufacturadoDetalles();
                    if (detallesReceta == null || detallesReceta.isEmpty()) {
                        ArticuloManufacturado manufacturadoRecargado = articuloManufacturadoRepository.findById(manufacturado.getId())
                                .orElseThrow(() -> new Exception("No se pudo recargar el manufacturado " + manufacturado.getDenominacion()));
                        detallesReceta = manufacturadoRecargado.getManufacturadoDetalles();
                        if (detallesReceta == null || detallesReceta.isEmpty()) {
                            throw new Exception("El artículo manufacturado '" + manufacturado.getDenominacion() + "' no tiene una receta definida (detalles vacíos o nulos incluso después de recargar).");
                        }
                    }

                    logger.info("Receta para {} tiene {} insumos.", manufacturado.getDenominacion(), detallesReceta.size());
                    for (ArticuloManufacturadoDetalle detalleRecetaItem : detallesReceta) {
                        ArticuloInsumo insumoComponenteOriginal = detalleRecetaItem.getArticuloInsumo();
                        if (insumoComponenteOriginal == null) throw new Exception ("Error en la receta de '"+manufacturado.getDenominacion()+"'.");

                        StockInsumoSucursalResponseDTO stockInsumoCompSucursal = stockInsumoSucursalService.getStockByInsumoAndSucursal(
                                insumoComponenteOriginal.getId(), sucursalPedido.getId()
                        );
                        Double stockActualInsumoCompSucursal = stockInsumoCompSucursal.getStockActual();

                        logger.info("--> Insumo de receta: {}, Stock Actual: {}, Cantidad Receta: {}", insumoComponenteOriginal.getDenominacion(), stockActualInsumoCompSucursal, detalleRecetaItem.getCantidad());
                        if (Boolean.FALSE.equals(insumoComponenteOriginal.getEstadoActivo())){
                            throw new Exception("El insumo componente '" + insumoComponenteOriginal.getDenominacion() + "' ya no está disponible.");
                        }
                        double cantidadNecesariaComponenteTotal = detalleRecetaItem.getCantidad() * cantidadPedida;
                        if (stockActualInsumoCompSucursal == null || stockActualInsumoCompSucursal < cantidadNecesariaComponenteTotal) {
                            throw new Exception("Stock insuficiente del insumo '" + insumoComponenteOriginal.getDenominacion() +
                                    "' en la sucursal " + sucursalPedido.getNombre() +
                                    ". Solicitado: " + cantidadNecesariaComponenteTotal +
                                    ", Disponible: " + (stockActualInsumoCompSucursal != null ? stockActualInsumoCompSucursal : 0.0));
                        }
                        insumosAReducirMap.merge(insumoComponenteOriginal.getId(), cantidadNecesariaComponenteTotal, Double::sum);
                    }
                } else {
                    throw new Exception("Artículo con ID " + articuloBaseDelCarrito.getId() + " ("+articuloBaseDelCarrito.getDenominacion()+") no es ni ArticuloInsumo ni ArticuloManufacturado, o no se encontró en repositorios específicos durante la verificación de stock.");
                }
            }
        }
        logger.info("Pre-Verificación de Stock completada. Insumos a reducir: {}", insumosAReducirMap);

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

        double totalGeneralPedido = 0.0;
        double costoTotalPedido = 0.0;
        double descuentoTotalPromociones = 0.0;
        logger.info("Iniciando cálculo de Total y TotalCosto. costoTotalPedido inicial: {}", costoTotalPedido);

        // --- Aplicación de promociones y cálculo de totales ---
        for (CarritoItem item : carrito.getItems()) {
            DetallePedido detallePedido = new DetallePedido();
            Articulo articuloDelItem = item.getArticulo();

            logger.info("Procesando para DetallePedido: Articulo '{}' (ID: {}), Clase Real Obtenida: {}, Cantidad: {}", articuloDelItem.getDenominacion(), articuloDelItem.getId(), articuloDelItem.getClass().getName(), item.getCantidad());
            detallePedido.setArticulo(articuloDelItem);
            detallePedido.setCantidad(item.getCantidad());

            double subTotalSinDescuento = item.getCantidad() * item.getPrecioUnitarioAlAgregar();
            double subTotalItem = item.getCantidad() * item.getPrecioUnitarioAlAgregar();
            double descuentoAplicadoPorPromocion = 0.0;
            Promocion promocionAplicada = null; // Para almacenar la entidad Promocion si aplica

            // Si el item ya trae una promoción pre-aplicada desde el carrito, la usamos.
            // Esto es importante si el carrito ya tiene lógica para seleccionar la mejor promoción.
            if (!articulosProcesadosPorCombo.contains(articuloDelItem.getId())) {
                Optional<PromocionResponseDTO> optPromocion = promocionesActivasDTO.stream()
                        .filter(p -> p.getDetallesPromocion().stream()
                                .anyMatch(pd -> pd.getArticulo() != null && pd.getArticulo().getId().equals(articuloDelItem.getId())))
                        .findFirst();

                if (optPromocion.isPresent()) {
                    PromocionResponseDTO promoDto = optPromocion.get();
                    logger.info("--> Promoción encontrada para '{}': {}", articuloDelItem.getDenominacion(), promoDto.getDenominacion());

                    // Lógica para PORCENTAJE
                    if (promoDto.getTipoPromocion() == TipoPromocion.PORCENTAJE) {
                        descuentoAplicadoPorPromocion = subTotalSinDescuento * (promoDto.getPorcentajeDescuento() / 100.0);
                        logger.info("--> Calculando Descuento PORCENTAJE: -${}", descuentoAplicadoPorPromocion);

                        // Lógica para CANTIDAD (ej: 2x1)
                    } else if (promoDto.getTipoPromocion() == TipoPromocion.CANTIDAD) {
                        PromocionDetalleResponseDTO detallePromo = promoDto.getDetallesPromocion().stream().findFirst().orElse(null);
                        if (detallePromo != null && item.getCantidad() >= detallePromo.getCantidad()) {
                            int vecesAplicada = item.getCantidad() / detallePromo.getCantidad();
                            double precioOriginalLote = detallePromo.getCantidad() * item.getPrecioUnitarioAlAgregar();
                            double descuentoPorLote = precioOriginalLote - promoDto.getPrecioPromocional();
                            descuentoAplicadoPorPromocion = descuentoPorLote * vecesAplicada;
                            logger.info("--> Calculando Descuento CANTIDAD: -${}", descuentoAplicadoPorPromocion);
                        } else {
                            logger.warn("--> La cantidad pedida ({}) no cumple el requisito ({}) de la promoción '{}'", item.getCantidad(), detallePromo != null ? detallePromo.getCantidad() : "N/A", promoDto.getDenominacion());
                        }
                    }

                    if (descuentoAplicadoPorPromocion > 0) {
                        promocionAplicada = mapPromocionResponseToEntity(promoDto);
                    }
                } else {
                    logger.info("--> No se encontraron promociones activas para '{}'", articuloDelItem.getDenominacion());
                }
            }

            double subTotalFinal = subTotalSinDescuento - descuentoAplicadoPorPromocion;
            detallePedido.setSubTotal(subTotalFinal);
            detallePedido.setPromocionAplicada(promocionAplicada);
            detallePedido.setDescuentoAplicadoPorPromocion(descuentoAplicadoPorPromocion);

            totalGeneralPedido += subTotalFinal;

            if (articuloDelItem instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) articuloDelItem;
                logger.info("--> Costo Insumo: {}, PrecioCompra: {}, Cantidad: {}", insumo.getDenominacion(), insumo.getPrecioCompra(), item.getCantidad());
                if (insumo.getPrecioCompra() == null) {
                    logger.error("!! ERROR CRITICO: Insumo '{}' (ID: {}) tiene precioCompra NULO.", insumo.getDenominacion(), insumo.getId());
                    throw new Exception("El insumo '"+insumo.getDenominacion()+"' no tiene precio de compra.");
                }
                costoTotalPedido += item.getCantidad() * insumo.getPrecioCompra();
            } else if (articuloDelItem instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articuloDelItem;
                logger.info("--> Costo Manufacturado: {}", manufacturado.getDenominacion());
                double costoManufacturadoUnitario = 0.0;

                List<ArticuloManufacturadoDetalle> detallesReceta = manufacturado.getManufacturadoDetalles();
                if (detallesReceta == null || detallesReceta.isEmpty()) {
                    ArticuloManufacturado manufacturadoRecargado = articuloManufacturadoRepository.findById(manufacturado.getId())
                            .orElseThrow(() -> new Exception("No se pudo recargar el manufacturado " + manufacturado.getDenominacion() + " para obtener detalles de receta."));
                    detallesReceta = manufacturadoRecargado.getManufacturadoDetalles();
                    if (detallesReceta == null || detallesReceta.isEmpty()) {
                        throw new Exception("El artículo manufacturado '" + manufacturado.getDenominacion() + "' (ID: " + manufacturado.getId() + ") no tiene detalles de receta para el cálculo de costo (incluso después de recargar).");
                    }
                }
                for (ArticuloManufacturadoDetalle detalleRecetaItem : detallesReceta) {
                    ArticuloInsumo insumoComponenteOriginal = detalleRecetaItem.getArticuloInsumo();
                    if (insumoComponenteOriginal == null) {
                        throw new Exception("Error en receta de '" + manufacturado.getDenominacion() + "': insumo nulo.");
                    }
                    ArticuloInsumo insumoCompConPrecio = articuloInsumoRepository.findById(insumoComponenteOriginal.getId())
                            .orElseThrow(() -> new Exception("Insumo " + insumoComponenteOriginal.getDenominacion() + " de receta no encontrado en BD para costo."));

                    logger.info("----> Receta Insumo: {}, PrecioCompra: {}, Cantidad Receta: {}", insumoCompConPrecio.getDenominacion(), insumoCompConPrecio.getPrecioCompra(), detalleRecetaItem.getCantidad());
                    if (insumoCompConPrecio.getPrecioCompra() == null) {
                        logger.error("!! ERROR CRITICO: Insumo de receta '{}' (ID: {}) tiene precioCompra NULO.", insumoCompConPrecio.getDenominacion(), insumoCompConPrecio.getId());
                        throw new Exception("El insumo componente '"+insumoCompConPrecio.getDenominacion()+"' no tiene precio de compra.");
                    }
                    costoManufacturadoUnitario += detalleRecetaItem.getCantidad() * insumoCompConPrecio.getPrecioCompra();
                }
                logger.info("--> Costo Manufacturado Unitario Calculado: {}", costoManufacturadoUnitario);
                costoTotalPedido += item.getCantidad() * costoManufacturadoUnitario;
            } else {
                logger.warn("!! (Cálculo Costo): Articulo ID {} ({}) no es ni ArticuloInsumo ni ArticuloManufacturado. Clase Real Obtenida: {}", articuloDelItem.getId(), articuloDelItem.getDenominacion(), articuloDelItem.getClass().getName());
            }
            logger.info("--> costoTotalPedido acumulado: {}", costoTotalPedido);
            nuevoPedido.addDetalle(detallePedido);
        }

        // --- Aplicar descuento global (TAKEAWAY/EFECTIVO) después de las promociones individuales ---
        double descuentoGlobal = 0.0;
        if (pedidoRequest.getTipoEnvio() == TipoEnvio.TAKEAWAY && pedidoRequest.getFormaPago() == FormaPago.EFECTIVO) {
            descuentoGlobal = totalGeneralPedido * 0.10;
            logger.info("Descuento del 10% aplicado para TAKEAWAY/EFECTIVO: -${}", descuentoGlobal);
            totalGeneralPedido -= descuentoGlobal;
        }
        nuevoPedido.setDescuentoAplicado(descuentoGlobal); // Este campo ahora refleja solo el descuento global

        nuevoPedido.setTotal(totalGeneralPedido);
        nuevoPedido.setTotalCosto(costoTotalPedido);
        logger.info("Pedido Final - Total: {}, TotalCosto: {}", nuevoPedido.getTotal(), nuevoPedido.getTotalCosto());

        logger.info("Iniciando Actualización de Stock en la Base de Datos...");
        for (Map.Entry<Integer, Double> entry : insumosAReducirMap.entrySet()) {
            Integer insumoId = entry.getKey();
            Double cantidadADescontar = entry.getValue();
            stockInsumoSucursalService.reduceStock(insumoId, sucursalPedido.getId(), cantidadADescontar);
            logger.info("--> Stock para Insumo ID {} en Sucursal ID {} reducido en {}. (Lógica de StockInsumoSucursalService)", insumoId, sucursalPedido.getId(), cantidadADescontar);
        }
        logger.info("Actualización de Stock completada.");

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);
        logger.info("Pedido guardado en DB con ID: {}", pedidoGuardado.getId());

        if (pedidoGuardado.getFormaPago() == FormaPago.MERCADO_PAGO) {
            logger.info("Forma de pago es MERCADO_PAGO. Intentando crear preferencia...");
            try {
                String preferenceId = mercadoPagoService.crearPreferenciaPago(pedidoGuardado);
                pedidoGuardado.setMpPreferenceId(preferenceId);
                pedidoGuardado = pedidoRepository.save(pedidoGuardado);
                logger.info("Preferencia de Mercado Pago creada exitosamente. Pedido actualizado en DB con Preference ID: {}", preferenceId);
            } catch (Exception e) {
                logger.error("!! FALLÓ la creación de la preferencia de Mercado Pago para Pedido ID: {}. Causa: {}", pedidoGuardado.getId(), e.getMessage(), e);
                throw new Exception("No se pudo generar la preferencia de pago. Por favor, intente de nuevo.", e);
            }
        }

        carritoService.vaciarCarrito(cliente);
        logger.info("Carrito vaciado para cliente ID: {}", cliente.getId());
        logger.info("FIN - crearPedidoDesdeCarrito ejecutado exitosamente para Pedido ID: {}", pedidoGuardado.getId());

        return convertToResponseDto(pedidoGuardado);
    }

    // Helper para convertir PromocionResponseDTO a Promocion (Entidad)
    // Se ha ajustado para manejar los detalles de la promoción y el artículo asociado.
    private Promocion mapPromocionResponseToEntity(PromocionResponseDTO dto) {
        if (dto == null) return null;
        Promocion promocion = new Promocion();
        promocion.setId(dto.getId());
        promocion.setDenominacion(dto.getDenominacion());
        promocion.setFechaDesde(dto.getFechaDesde());
        promocion.setFechaHasta(dto.getFechaHasta());
        promocion.setHoraDesde(dto.getHoraDesde());
        promocion.setHoraHasta(dto.getHoraHasta());
        promocion.setDescripcionDescuento(dto.getDescripcionDescuento());
        promocion.setPrecioPromocional(dto.getPrecioPromocional());
        promocion.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        promocion.setTipoPromocion(dto.getTipoPromocion());

        // Mapear los detalles de la promoción si existen en el DTO
        if (dto.getDetallesPromocion() != null) {
            Set<PromocionDetalle> detalles = new HashSet<>();
            for (PromocionDetalleResponseDTO detalleDto : dto.getDetallesPromocion()) {
                PromocionDetalle detalle = new PromocionDetalle();
                detalle.setId(detalleDto.getId());
                detalle.setCantidad(detalleDto.getCantidad());
                // Mapear el Artículo si existe
                if (detalleDto.getArticulo() != null) {
                    Articulo articulo = new Articulo(); // Crear una instancia de Articulo
                    articulo.setId(detalleDto.getArticulo().getId());
                    articulo.setDenominacion(detalleDto.getArticulo().getDenominacion());
                    articulo.setPrecioVenta(detalleDto.getArticulo().getPrecioVenta());
                    // Asegúrate de que si Articulo es una clase abstracta, esto sea un ArticuloManufacturado o ArticuloInsumo
                    // Para este mapeo simple, si solo necesitas el ID, esto podría ser suficiente.
                    // Si necesitas el objeto completo con herencia, deberías buscarlo en el repositorio.
                    detalle.setArticulo(articulo);
                }
                detalles.add(detalle);
            }
            promocion.setDetallesPromocion(detalles);
        }

        // Mapear sucursales si existen en el DTO (aunque para DetallePedido no se usa directamente)
        if (dto.getSucursales() != null) {
            Set<Sucursal> sucursales = new HashSet<>();
            for (SucursalSimpleResponseDTO sucursalDto : dto.getSucursales()) {
                Sucursal sucursal = new Sucursal();
                sucursal.setId(sucursalDto.getId());
                sucursal.setNombre(sucursalDto.getNombre());
                sucursales.add(sucursal);
            }
            promocion.setSucursales(sucursales);
        }

        return promocion;
    }

    @Override
    @Transactional
    public String createPreferenceMp(String auth0Id, @Valid MercadoPagoCreatePreferenceDTO dto) throws Exception {
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseThrow(() -> new Exception("Usuario no encontrado con Auth0 ID: " + auth0Id));

        if (!(usuario.getRol() == Rol.CLIENTE || usuario.getRol() == Rol.ADMIN)) {
            throw new Exception("Acceso denegado: Solo clientes o administradores pueden crear preferencias de pago.");
        }

        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new Exception("Pedido con ID " + dto.getPedidoId() + " no encontrado."));

        if (usuario.getRol() == Rol.CLIENTE && !pedido.getCliente().getUsuario().getAuth0Id().equals(auth0Id)) {
            throw new Exception("Acceso denegado: El pedido solicitado no pertenece a este cliente.");
        }

        if (pedido.getMpPreferenceId() != null && !pedido.getMpPreferenceId().isEmpty()) {
            System.out.println("DEBUG MP: El pedido ID " + pedido.getId() + " ya tiene un Preference ID de MP: " + pedido.getMpPreferenceId());
            return pedido.getMpPreferenceId();
        }

        if (pedido.getFormaPago() != FormaPago.MERCADO_PAGO) {
            throw new Exception("El pedido con ID " + pedido.getId() + " no está configurado para pago con Mercado Pago.");
        }

        if (pedido.getEstado() != Estado.PENDIENTE) {
            throw new Exception("El pedido con ID " + dto.getPedidoId() + " no está en un estado válido para generar pago. Estado actual: " + pedido.getEstado());
        }

        try {
            String preferenceId = mercadoPagoService.crearPreferenciaPago(pedido);
            pedido.setMpPreferenceId(preferenceId);
            pedidoRepository.save(pedido);
            return preferenceId;
        } catch (Exception e) {
            System.err.println("ERROR: Falló la creación de la preferencia de Mercado Pago para pedido " + pedido.getId() + ": " + e.getMessage());
            throw new Exception("Error al generar preferencia de Mercado Pago: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PedidoResponseDTO updateEstado(Integer id, Estado nuevoEstado) throws Exception {
        Pedido pedidoExistente = pedidoRepository.findById(id).orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + id));
        if (pedidoExistente.getEstado() == Estado.ENTREGADO) {
            if (nuevoEstado == Estado.CANCELADO) throw new Exception("No se puede cancelar un pedido que ya fue entregado.");
            if (nuevoEstado != Estado.ENTREGADO) throw new Exception("Un pedido entregado no puede cambiar a estado: " + nuevoEstado);
        }
        if (pedidoExistente.getEstado() == Estado.CANCELADO && nuevoEstado != Estado.CANCELADO) {
            throw new Exception("No se puede cambiar el estado de un pedido cancelado.");
        }
        if (pedidoExistente.getEstado() == Estado.RECHAZADO && nuevoEstado != Estado.RECHAZADO) {
            throw new Exception("No se puede cambiar el estado de un pedido rechazado.");
        }
        if (pedidoExistente.getEstado() == Estado.PENDIENTE && (nuevoEstado == Estado.ENTREGADO )) {
            throw new Exception("Un pedido pendiente debe pasar por preparación/listo antes de ser entregado.");
        }
        pedidoExistente.setEstado(nuevoEstado);
        Pedido pedidoActualizado = pedidoRepository.save(pedidoExistente);
        return convertToResponseDto(pedidoActualizado);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Pedido pedido = pedidoRepository.findById(id).orElseThrow(() -> new Exception("Pedido no encontrado con ID: " + id));
        if (pedido.getEstado() == Estado.ENTREGADO) {
            throw new Exception("No se puede eliminar (borrado lógico) un pedido que ya fue entregado.");
        }
        pedido.setEstadoActivo(false);
        pedido.setFechaBaja(LocalDate.now());
        if (pedido.getEstado() != Estado.CANCELADO && pedido.getEstado() != Estado.RECHAZADO && pedido.getEstado() != Estado.ENTREGADO) {
            pedido.setEstado(Estado.CANCELADO);
            restituirStockDePedido(pedido);
        }
        pedidoRepository.save(pedido);
    }

    private void restituirStockDePedido(Pedido pedido) throws Exception {
        logger.info("Iniciando restitución de stock para Pedido ID: {}", pedido.getId());
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            logger.warn("El pedido ID {} no tiene detalles. No se restituirá stock.", pedido.getId());
            return;
        }

        Integer sucursalId = pedido.getSucursal() != null ? pedido.getSucursal().getId() : null;
        if (sucursalId == null) {
            logger.error("No se pudo determinar la sucursal del pedido ID {}. No se restituirá stock.", pedido.getId());
            throw new Exception("No se pudo determinar la sucursal del pedido para restituir stock.");
        }

        for (DetallePedido detallePedido : pedido.getDetalles()) {
            Articulo articuloDelDetalle = detallePedido.getArticulo();
            int cantidadPedida = detallePedido.getCantidad();

            Articulo refreshedArticulo = articuloRepository.findById(articuloDelDetalle.getId())
                    .orElseThrow(() -> new Exception("Artículo del detalle ID " + articuloDelDetalle.getId() + " no encontrado para restitución."));


            if (refreshedArticulo instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) refreshedArticulo;
                if (Boolean.TRUE.equals(insumo.getEsParaElaborar())) {
                    logger.info("Insumo '{}' (ID: {}) es 'para elaborar'. No se restituye directamente su stock para venta.", insumo.getDenominacion(), insumo.getId());
                } else {
                    stockInsumoSucursalService.addStock(insumo.getId(), sucursalId, (double) cantidadPedida);
                    logger.info("Stock restituido para insumo ID {} en sucursal ID {}. Cantidad: {}", insumo.getId(), sucursalId, cantidadPedida);
                }
            } else if (refreshedArticulo instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) refreshedArticulo;
                List<ArticuloManufacturadoDetalle> detallesReceta = manufacturado.getManufacturadoDetalles();

                if (detallesReceta == null || detallesReceta.isEmpty()) {
                    ArticuloManufacturado loadedManufacturado = articuloManufacturadoRepository.findById(manufacturado.getId())
                            .orElseThrow(() -> new Exception("Articulo Manufacturado " + manufacturado.getId() + " no encontrado para restitución."));
                    detallesReceta = loadedManufacturado.getManufacturadoDetalles();
                    if (detallesReceta == null || detallesReceta.isEmpty()) {
                        logger.warn("El ArticuloManufacturado '{}' (ID: {}) no tiene detalles de receta. No se restituirán sus insumos.", manufacturado.getDenominacion(), manufacturado.getId());
                        continue;
                    }
                }

                for (ArticuloManufacturadoDetalle recetaItem : detallesReceta) {
                    ArticuloInsumo insumoComponente = recetaItem.getArticuloInsumo();
                    Double cantidadNecesaria = recetaItem.getCantidad();
                    stockInsumoSucursalService.addStock(insumoComponente.getId(), sucursalId, cantidadNecesaria * cantidadPedida);
                    logger.info("Stock restituido para insumo componente ID {} en sucursal ID {}. Cantidad: {}", insumoComponente.getId(), sucursalId, cantidadNecesaria * cantidadPedida);
                }
            } else {
                logger.warn("Artículo '{}' (ID: {}) no es insumo ni manufacturado. No se procesa restitución de stock.", articuloDelDetalle.getDenominacion(), articuloDelDetalle.getId());
            }
        }
        logger.info("Restitución de stock completada para Pedido ID: {}", pedido.getId());
    }
}