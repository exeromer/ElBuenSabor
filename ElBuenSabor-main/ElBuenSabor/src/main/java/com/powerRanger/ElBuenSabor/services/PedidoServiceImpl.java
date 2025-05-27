package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.entities.enums.Rol;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired private LocalidadRepository localidadRepository; // Añadido para la nueva lógica de domicilio

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
        System.out.println("DEBUG: Iniciando crearPedidoDesdeCarrito para cliente ID: " + cliente.getId());
        Carrito carrito = carritoRepository.findByCliente(cliente)
                .orElseThrow(() -> new Exception("No se encontró un carrito para el cliente " + cliente.getEmail()));
        if (carrito.getItems() == null || carrito.getItems().isEmpty()) {
            throw new Exception("El carrito está vacío. No se puede generar el pedido.");
        }
        System.out.println("DEBUG: Carrito ID: " + carrito.getId() + " con " + carrito.getItems().size() + " items.");

        // --- Lógica de Domicilio: Buscar o Crear ---
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
            System.out.println("DEBUG: Usando domicilio existente encontrado ID: " + domicilioParaElPedido.getId());
        } else {
            Domicilio nuevoDomicilio = new Domicilio();
            nuevoDomicilio.setCalle(pedidoRequest.getCalleDomicilio());
            nuevoDomicilio.setNumero(pedidoRequest.getNumeroDomicilio());
            nuevoDomicilio.setCp(pedidoRequest.getCpDomicilio());
            nuevoDomicilio.setLocalidad(localidadDomicilio);
            domicilioParaElPedido = domicilioRepository.save(nuevoDomicilio);
            System.out.println("DEBUG: Creado nuevo domicilio ID: " + domicilioParaElPedido.getId());
        }

        // Opcional: Asociar el domicilio al cliente si así se indica y no está ya asociado
        if (pedidoRequest.getGuardarDireccionEnPerfil() != null && pedidoRequest.getGuardarDireccionEnPerfil()) {
            // Verificar si el cliente ya tiene este domicilio
            boolean yaTieneDomicilio = cliente.getDomicilios().stream()
                    .anyMatch(d -> d.getId().equals(domicilioParaElPedido.getId()));
            if (!yaTieneDomicilio) {
                cliente.addDomicilio(domicilioParaElPedido); // Asumiendo que Cliente tiene un método addDomicilio
                clienteRepository.save(cliente); // Guardar el cliente con el nuevo domicilio asociado
                System.out.println("DEBUG: Domicilio ID " + domicilioParaElPedido.getId() + " asociado al perfil del cliente ID " + cliente.getId());
            }
        }
        // --- Fin Lógica de Domicilio ---

        Sucursal sucursalPedido = sucursalRepository.findById(pedidoRequest.getSucursalId())
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + pedidoRequest.getSucursalId()));
        if (sucursalPedido.getEstadoActivo() == null || !sucursalPedido.getEstadoActivo()) {
            throw new Exception("La sucursal seleccionada no está activa.");
        }

        Map<Integer, Double> insumosAReducirMap = new HashMap<>();
        System.out.println("DEBUG: Iniciando Pre-Verificación de Stock...");

        for (CarritoItem item : carrito.getItems()) {
            Articulo articuloBaseDelCarrito = item.getArticulo();
            int cantidadPedida = item.getCantidad();
            System.out.println("DEBUG: Verificando stock para CarritoItem ID: " + item.getId() +
                    ", Articulo ID: " + articuloBaseDelCarrito.getId() +
                    " (" + articuloBaseDelCarrito.getDenominacion() + ")" +
                    ", Clase Proxy Inicial: " + articuloBaseDelCarrito.getClass().getName() +
                    ", Cantidad: " + cantidadPedida);

            Articulo articuloVerificado;
            Optional<ArticuloInsumo> optInsumoStock = articuloInsumoRepository.findById(articuloBaseDelCarrito.getId());
            if (optInsumoStock.isPresent()) {
                articuloVerificado = optInsumoStock.get();
                ArticuloInsumo insumo = (ArticuloInsumo) articuloVerificado;
                System.out.println("DEBUG: Es ArticuloInsumo (obtenido de repo para stock): " + insumo.getDenominacion() + ", Stock Actual: " + insumo.getStockActual());
                if (insumo.getEstadoActivo() == null || !insumo.getEstadoActivo()){
                    throw new Exception("El insumo '" + insumo.getDenominacion() + "' ya no está disponible.");
                }
                if (insumo.getStockActual() == null || insumo.getStockActual() < cantidadPedida) {
                    throw new Exception("Stock insuficiente para el insumo: " + insumo.getDenominacion() + ". Solicitado: " + cantidadPedida + ", Disponible: " + (insumo.getStockActual() !=null ? insumo.getStockActual():0) );
                }
                insumosAReducirMap.merge(insumo.getId(), (double) cantidadPedida, Double::sum);
            } else {
                Optional<ArticuloManufacturado> optManufStock = articuloManufacturadoRepository.findById(articuloBaseDelCarrito.getId());
                if (optManufStock.isPresent()) {
                    articuloVerificado = optManufStock.get();
                    ArticuloManufacturado manufacturado = (ArticuloManufacturado) articuloVerificado;
                    System.out.println("DEBUG: Es ArticuloManufacturado (obtenido de repo para stock): " + manufacturado.getDenominacion());
                    if (manufacturado.getEstadoActivo() == null || !manufacturado.getEstadoActivo()){
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

                    System.out.println("DEBUG: Receta para " + manufacturado.getDenominacion() + " tiene " + detallesReceta.size() + " insumos.");
                    for (ArticuloManufacturadoDetalle detalleRecetaItem : detallesReceta) {
                        ArticuloInsumo insumoComponenteOriginal = detalleRecetaItem.getArticuloInsumo();
                        if (insumoComponenteOriginal == null) throw new Exception ("Error en la receta de '"+manufacturado.getDenominacion()+"'.");

                        ArticuloInsumo insumoCompFromDb = articuloInsumoRepository.findById(insumoComponenteOriginal.getId())
                                .orElseThrow(() -> new Exception("Insumo " + insumoComponenteOriginal.getDenominacion() + " de receta no encontrado en DB."));

                        System.out.println("DEBUG:   Insumo de receta: " + insumoCompFromDb.getDenominacion() + ", Stock Actual: " + insumoCompFromDb.getStockActual() + ", Cantidad Receta: " + detalleRecetaItem.getCantidad());
                        if (insumoCompFromDb.getEstadoActivo() == null || !insumoCompFromDb.getEstadoActivo()){
                            throw new Exception("El insumo componente '" + insumoCompFromDb.getDenominacion() + "' ya no está disponible.");
                        }
                        double cantidadNecesariaComponenteTotal = detalleRecetaItem.getCantidad() * cantidadPedida;
                        if (insumoCompFromDb.getStockActual() == null || insumoCompFromDb.getStockActual() < cantidadNecesariaComponenteTotal) {
                            throw new Exception("Stock insuficiente del insumo '" + insumoCompFromDb.getDenominacion() + "'. Solicitado: " + cantidadNecesariaComponenteTotal + ", Disponible: " + (insumoCompFromDb.getStockActual() != null ? insumoCompFromDb.getStockActual() : 0) );
                        }
                        insumosAReducirMap.merge(insumoCompFromDb.getId(), cantidadNecesariaComponenteTotal, Double::sum);
                    }
                } else {
                    throw new Exception("Artículo con ID " + articuloBaseDelCarrito.getId() + " ("+articuloBaseDelCarrito.getDenominacion()+") no es ni Insumo ni Manufacturado, o no se encontró en repositorios específicos durante la verificación de stock.");
                }
            }
        }
        System.out.println("DEBUG: Pre-Verificación de Stock completada. Insumos a reducir: " + insumosAReducirMap);

        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setFechaPedido(LocalDate.now());
        nuevoPedido.setHoraEstimadaFinalizacion(parseTime(pedidoRequest.getHoraEstimadaFinalizacion(), "hora estimada de finalización"));
        nuevoPedido.setDomicilio(domicilioParaElPedido); // Usar el domicilio encontrado o creado
        nuevoPedido.setSucursal(sucursalPedido);
        nuevoPedido.setTipoEnvio(pedidoRequest.getTipoEnvio());
        nuevoPedido.setFormaPago(pedidoRequest.getFormaPago());
        nuevoPedido.setEstado(Estado.PENDIENTE);
        nuevoPedido.setEstadoActivo(true);
        // nuevoPedido.setNotas(pedidoRequest.getNotasAdicionales()); // Si tienes campo notas en Pedido

        double totalGeneralPedido = 0.0;
        double costoTotalPedido = 0.0;
        System.out.println("DEBUG: Iniciando cálculo de Total y TotalCosto. costoTotalPedido inicial: " + costoTotalPedido);

        for (CarritoItem item : carrito.getItems()) {
            DetallePedido detallePedido = new DetallePedido();
            Articulo articuloDelItem;
            Optional<ArticuloInsumo> optInsumo = articuloInsumoRepository.findById(item.getArticulo().getId());
            if (optInsumo.isPresent()) {
                articuloDelItem = optInsumo.get();
            } else {
                Optional<ArticuloManufacturado> optManuf = articuloManufacturadoRepository.findById(item.getArticulo().getId());
                if (optManuf.isPresent()) {
                    articuloDelItem = optManuf.get();
                } else {
                    throw new Exception("Artículo con ID " + item.getArticulo().getId() + " no encontrado en repositorios específicos al crear detalles de pedido.");
                }
            }

            System.out.println("DEBUG: Procesando para DetallePedido: Articulo '" + articuloDelItem.getDenominacion() +
                    "' (ID: " + articuloDelItem.getId() +
                    "), Clase Real Obtenida: " + articuloDelItem.getClass().getName() +
                    ", Cantidad: " + item.getCantidad());

            detallePedido.setArticulo(articuloDelItem);
            detallePedido.setCantidad(item.getCantidad());
            double subTotalItem = item.getCantidad() * item.getPrecioUnitarioAlAgregar();
            detallePedido.setSubTotal(subTotalItem);
            totalGeneralPedido += subTotalItem;

            if (articuloDelItem instanceof ArticuloInsumo) {
                ArticuloInsumo insumo = (ArticuloInsumo) articuloDelItem;
                System.out.println("DEBUG:   Costo Insumo: " + insumo.getDenominacion() + ", PrecioCompra: " + insumo.getPrecioCompra() + ", Cantidad: " + item.getCantidad());
                if (insumo.getPrecioCompra() == null) {
                    System.err.println("ERROR CRITICO: Insumo '" + insumo.getDenominacion() + "' (ID: " + insumo.getId() + ") tiene precioCompra NULO.");
                    throw new Exception("El insumo '"+insumo.getDenominacion()+"' no tiene precio de compra.");
                }
                costoTotalPedido += item.getCantidad() * insumo.getPrecioCompra();
            } else if (articuloDelItem instanceof ArticuloManufacturado) {
                ArticuloManufacturado manufacturado = (ArticuloManufacturado) articuloDelItem;
                System.out.println("DEBUG:   Costo Manufacturado: " + manufacturado.getDenominacion());
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

                    System.out.println("DEBUG:     Receta Insumo: " + insumoCompConPrecio.getDenominacion() + ", PrecioCompra: " + insumoCompConPrecio.getPrecioCompra() + ", Cantidad Receta: " + detalleRecetaItem.getCantidad());
                    if (insumoCompConPrecio.getPrecioCompra() == null) {
                        System.err.println("ERROR CRITICO: Insumo de receta '" + insumoCompConPrecio.getDenominacion() + "' (ID: " + insumoCompConPrecio.getId() + ") tiene precioCompra NULO.");
                        throw new Exception("El insumo componente '"+insumoCompConPrecio.getDenominacion()+"' no tiene precio de compra.");
                    }
                    costoManufacturadoUnitario += detalleRecetaItem.getCantidad() * insumoCompConPrecio.getPrecioCompra();
                }
                System.out.println("DEBUG:   Costo Manufacturado Unitario Calculado: " + costoManufacturadoUnitario);
                costoTotalPedido += item.getCantidad() * costoManufacturadoUnitario;
            } else {
                System.err.println("WARN (Cálculo Costo): Articulo ID " + articuloDelItem.getId() + " (" + articuloDelItem.getDenominacion() + ") no es ni ArticuloInsumo ni ArticuloManufacturado. Clase Real Obtenida: " + articuloDelItem.getClass().getName());
            }
            System.out.println("DEBUG:   costoTotalPedido acumulado: " + costoTotalPedido);
            nuevoPedido.addDetalle(detallePedido);
        }

        nuevoPedido.setTotal(totalGeneralPedido);
        nuevoPedido.setTotalCosto(costoTotalPedido);
        System.out.println("DEBUG: Pedido Final - Total: " + nuevoPedido.getTotal() + ", TotalCosto: " + nuevoPedido.getTotalCosto());

        System.out.println("DEBUG: Iniciando Actualización de Stock...");
        for (Map.Entry<Integer, Double> entry : insumosAReducirMap.entrySet()) {
            Integer insumoId = entry.getKey();
            Double cantidadADescontar = entry.getValue();
            ArticuloInsumo insumoAActualizar = articuloInsumoRepository.findById(insumoId)
                    .orElseThrow(() -> new Exception("Insumo con ID " + insumoId + " no encontrado para actualizar stock."));

            System.out.println("DEBUG:   Descontando Stock para Insumo ID: " + insumoId + " (" + insumoAActualizar.getDenominacion() + "), Cantidad a descontar: " + cantidadADescontar + ", Stock actual: " + insumoAActualizar.getStockActual());
            if(insumoAActualizar.getStockActual() == null) insumoAActualizar.setStockActual(0.0);
            insumoAActualizar.setStockActual(insumoAActualizar.getStockActual() - cantidadADescontar);
            articuloInsumoRepository.save(insumoAActualizar);
            System.out.println("DEBUG:   Nuevo Stock para Insumo ID: " + insumoId + ": " + insumoAActualizar.getStockActual());
        }
        System.out.println("DEBUG: Actualización de Stock completada.");

        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);
        System.out.println("DEBUG: Pedido Guardado con ID: " + pedidoGuardado.getId());

        carritoService.vaciarCarrito(cliente);
        System.out.println("DEBUG: Carrito vaciado para cliente ID: " + cliente.getId());

        return convertToResponseDto(pedidoGuardado);
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
        if (pedidoExistente.getEstado() == Estado.PENDIENTE && (nuevoEstado == Estado.ENTREGADO )) { //  Quite EN_CAMINO para simplificar
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
        }
        pedidoRepository.save(pedido);
    }
}