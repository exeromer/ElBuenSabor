package com.powerRanger.ElBuenSabor;

import com.powerRanger.ElBuenSabor.entities.enums.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
public class ElBuenSaborApplication {

	// Repositorios Inyectados
	@Autowired private PaisRepository paisRepository;
	@Autowired private UnidadMedidaRepository unidadMedidaRepository;
	@Autowired private ArticuloRepository articuloRepository;
	@Autowired private CategoriaRepository categoriaRepository;
	@Autowired private ImagenRepository imagenRepository;
	@Autowired private ArticuloInsumoRepository articuloInsumoRepository;
	@Autowired private ArticuloManufacturadoRepository articuloManufacturadoRepository;
	@Autowired private PedidoRepository pedidoRepository;
	@Autowired private SucursalRepository sucursalRepository;
	@Autowired private DomicilioRepository domicilioRepository;
	@Autowired private PromocionRepository promocionRepository;
	@Autowired private UsuarioRepository usuarioRepository;
	@Autowired private ProvinciaRepository provinciaRepository;
	@Autowired private LocalidadRepository localidadRepository;
	@Autowired private FacturaRepository facturaRepository;
	@Autowired private ClienteRepository clienteRepository;
	@Autowired private PromocionDetalleRepository promocionDetalleRepository;

	// Variable de instancia para referenciar pizzaMargarita en otros métodos
	private ArticuloManufacturado pizzaMargaritaInstance;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ElBuenSaborApplication.class, args);
		ElBuenSaborApplication app = context.getBean(ElBuenSaborApplication.class);
		app.insertarDatos();
		app.actualizarArticulo();
	}

	@Transactional
	public void insertarDatos() {
		System.out.println("Iniciando inserción de datos...");

		// ---------- UNIDAD DE MEDIDA ----------
		UnidadMedida unidadMedidaLitros = new UnidadMedida();
		unidadMedidaLitros.setDenominacion("Litros");
		unidadMedidaRepository.save(unidadMedidaLitros);
		UnidadMedida unidadMedidaGramos = new UnidadMedida();
		unidadMedidaGramos.setDenominacion("Gramos");
		unidadMedidaRepository.save(unidadMedidaGramos);
		UnidadMedida unidadMedidaKg = new UnidadMedida();
		unidadMedidaKg.setDenominacion("Kg");
		unidadMedidaRepository.save(unidadMedidaKg);
		UnidadMedida unidadMedidaUnidad = new UnidadMedida();
		unidadMedidaUnidad.setDenominacion("Unidad");
		unidadMedidaRepository.save(unidadMedidaUnidad);

		// ---------- CATEGORÍA ----------
		Categoria categoriaBebidas = new Categoria();
		categoriaBebidas.setDenominacion("Bebidas");
		categoriaRepository.save(categoriaBebidas);
		Categoria categoriaPizzas = new Categoria();
		categoriaPizzas.setDenominacion("Pizzas");
		categoriaRepository.save(categoriaPizzas);
		Categoria categoriaInsumos = new Categoria();
		categoriaInsumos.setDenominacion("Insumos");
		categoriaRepository.save(categoriaInsumos);

		// ---------- ARTÍCULO (Ejemplo de Bebida) ----------
		Articulo articuloBebida = new Articulo();
		articuloBebida.setDenominacion("Coca Cola 2.5L");
		articuloBebida.setPrecioVenta(1200.0);
		articuloBebida.setUnidadMedida(unidadMedidaLitros);
		articuloBebida.setCategoria(categoriaBebidas);
		articuloBebida.setEstadoActivo(true);
		Imagen imagenBebida = new Imagen();
		imagenBebida.setDenominacion("Imagen Coca Cola");
		// Asumiendo que Articulo tiene un método addImagen(Imagen imagen) que hace:
		// this.imagenes.add(imagen); imagen.setArticulo(this);
		// Si no, la forma actual de setImagenes y luego guardar Articulo es correcta si hay cascada.
		// Para mantenerlo simple, si tienes CascadeType.ALL en Articulo.imagenes:
		articuloBebida.setImagenes(new ArrayList<>(List.of(imagenBebida)));
		articuloRepository.save(articuloBebida);


		// ---------- ARTÍCULO INSUMO ----------
		ArticuloInsumo harina = new ArticuloInsumo();
		harina.setDenominacion("Harina 000");
		harina.setPrecioVenta(300.0);
		harina.setUnidadMedida(unidadMedidaKg); // Harina en Kg
		harina.setCategoria(categoriaInsumos);
		harina.setEstadoActivo(true);
		harina.setPrecioCompra(150.0);
		harina.setStockActual(50);
		harina.setStockMaximo(200);
		harina.setEsParaElaborar(true);
		Imagen imagenHarina = new Imagen();
		imagenHarina.setDenominacion("Imagen Harina");
		harina.setImagenes(new ArrayList<>(List.of(imagenHarina)));
		articuloInsumoRepository.save(harina);

		ArticuloInsumo quesoMozzarella = new ArticuloInsumo();
		quesoMozzarella.setDenominacion("Queso Mozzarella");
		quesoMozzarella.setPrecioVenta(2000.0);
		quesoMozzarella.setUnidadMedida(unidadMedidaKg); // Queso en Kg
		quesoMozzarella.setCategoria(categoriaInsumos);
		quesoMozzarella.setEstadoActivo(true);
		quesoMozzarella.setPrecioCompra(1200.0);
		quesoMozzarella.setStockActual(20);
		quesoMozzarella.setStockMaximo(50);
		quesoMozzarella.setEsParaElaborar(true);
		articuloInsumoRepository.save(quesoMozzarella);

		// ---------- ARTÍCULO MANUFACTURADO (Pizza) ----------
		this.pizzaMargaritaInstance = new ArticuloManufacturado(); // Usar la variable de instancia
		this.pizzaMargaritaInstance.setDenominacion("Pizza Margarita");
		this.pizzaMargaritaInstance.setPrecioVenta(5000.0);
		this.pizzaMargaritaInstance.setUnidadMedida(unidadMedidaUnidad);
		this.pizzaMargaritaInstance.setCategoria(categoriaPizzas);
		this.pizzaMargaritaInstance.setDescripcion("Pizza clásica con mozzarella fresca, salsa de tomate y albahaca.");
		this.pizzaMargaritaInstance.setTiempoEstimadoMinutos(25);
		this.pizzaMargaritaInstance.setPreparacion("1. Estirar masa. 2. Salsa de tomate. 3. Mozzarella. 4. Hornear. 5. Albahaca fresca.");
		this.pizzaMargaritaInstance.setEstadoActivo(true);
		Imagen imagenPizza = new Imagen();
		imagenPizza.setDenominacion("Imagen Pizza Margarita");
		this.pizzaMargaritaInstance.setImagenes(new ArrayList<>(List.of(imagenPizza)));

		ArticuloManufacturadoDetalle detalleHarinaPizza = new ArticuloManufacturadoDetalle();
		// IMPORTANTE: Lógica de cantidad y unidad de medida.
		// Si ArticuloManufacturadoDetalle.cantidad es Integer, y la UM de harina es Kg,
		// necesitarás una UM "Gramos" para harina o que 'cantidad' sea Double.
		// Asumiendo que quieres usar 200 gramos de harina (0.2 Kg) y 'cantidad' es Integer:
		// Opción 1: Cambiar la UM de harina a gramos y setCantidad(200)
		// Opción 2: Mantener UM de harina en Kg, y que ArticuloManufacturadoDetalle.cantidad sea Double, setCantidad(0.2)
		// Opción 3 (menos ideal): Si cantidad es Integer, y quieres representar 0.2Kg, es un problema.
		// Voy a asumir que ajustarás esto. Para el ejemplo, si cantidad es Integer, pondré 1 y se asume que la UM del detalle se ajusta.
		// O si tu 'cantidad' en ArticuloManufacturadoDetalle es Double:
		// detalleHarinaPizza.setCantidad(0.2); // 0.2 Kg
		detalleHarinaPizza.setCantidad(1); // EJEMPLO: 1 unidad de lo que sea que represente la lógica del detalle para "Harina 000 (Kg)"
		// ESTO NECESITA REVISIÓN DE TU PARTE SEGÚN TU MODELO DE DATOS
		detalleHarinaPizza.setArticuloInsumo(harina);

		ArticuloManufacturadoDetalle detalleQuesoPizza = new ArticuloManufacturadoDetalle();
		// detalleQuesoPizza.setCantidad(0.15); // Si cantidad es Double (0.15 Kg)
		detalleQuesoPizza.setCantidad(1); // EJEMPLO: 1 unidad para "Queso Mozzarella (Kg)" - REVISAR
		detalleQuesoPizza.setArticuloInsumo(quesoMozzarella);

		this.pizzaMargaritaInstance.addManufacturadoDetalle(detalleHarinaPizza); // Usar el método helper
		this.pizzaMargaritaInstance.addManufacturadoDetalle(detalleQuesoPizza); // Usar el método helper

		articuloManufacturadoRepository.save(this.pizzaMargaritaInstance);


		// ---------- PAÍS, PROVINCIA, LOCALIDAD, DOMICILIO ----------
		Pais paisArg = new Pais(); paisArg.setNombre("Argentina"); paisRepository.save(paisArg);
		Provincia mendoza = new Provincia(); mendoza.setNombre("Mendoza"); mendoza.setPais(paisArg); provinciaRepository.save(mendoza);
		Localidad ciudadMendoza = new Localidad(); ciudadMendoza.setNombre("Ciudad de Mendoza"); ciudadMendoza.setProvincia(mendoza); localidadRepository.save(ciudadMendoza);
		Domicilio domicilioCliente = new Domicilio(); domicilioCliente.setCalle("San Martín"); domicilioCliente.setNumero(1025); domicilioCliente.setCp(5500); domicilioCliente.setLocalidad(ciudadMendoza); domicilioRepository.save(domicilioCliente);
		Domicilio domicilioSucursal = new Domicilio(); domicilioSucursal.setCalle("Las Heras"); domicilioSucursal.setNumero(450); domicilioSucursal.setCp(5500); domicilioSucursal.setLocalidad(ciudadMendoza); domicilioRepository.save(domicilioSucursal);

		// ---------- USUARIO para Cliente ----------
		Usuario usuarioCliente = new Usuario();
		usuarioCliente.setUsername("cliente_juan");
		usuarioCliente.setAuth0Id("auth0|juanperez123");
		usuarioCliente.setRol(Rol.CLIENTE); // Corregido
		usuarioCliente.setEstadoActivo(true);
		// No es necesario save(usuarioCliente) si Cliente tiene CascadeType.ALL en su relación con Usuario

		// ---------- CLIENTE ----------
		Cliente juanPerez = new Cliente();
		juanPerez.setNombre("Juan");
		juanPerez.setApellido("Pérez");
		juanPerez.setTelefono("2615550101");
		juanPerez.setEmail("juan.perez@example.com");
		juanPerez.setFechaNacimiento(LocalDate.of(1985, 10, 15));
		juanPerez.setUsuario(usuarioCliente);
		juanPerez.setEstadoActivo(true);
		juanPerez.addDomicilio(domicilioCliente); // Usar el método helper
		Imagen imagenCliente = new Imagen();
		imagenCliente.setDenominacion("Foto perfil Juan");
		juanPerez.setImagen(imagenCliente);
		clienteRepository.save(juanPerez);

		// ---------- SUCURSAL ----------
		Sucursal sucursalCentro = new Sucursal();
		sucursalCentro.setNombre("El Buen Sabor - Centro");
		sucursalCentro.setHorarioApertura(LocalTime.of(10, 0));
		sucursalCentro.setHorarioCierre(LocalTime.of(23, 30));
		sucursalCentro.setDomicilio(domicilioSucursal);
		sucursalCentro.setEstadoActivo(true);
		sucursalCentro.addCategoria(categoriaPizzas); // Usar el método helper
		sucursalCentro.addCategoria(categoriaBebidas); // Usar el método helper

		// ---------- PROMOCIÓN ----------
		Promocion promoDosPorUnoPizzas = new Promocion();
		promoDosPorUnoPizzas.setDenominacion("2x1 en Pizzas Clásicas");
		promoDosPorUnoPizzas.setFechaDesde(LocalDate.now().minusDays(2));
		promoDosPorUnoPizzas.setFechaHasta(LocalDate.now().plusWeeks(1));
		promoDosPorUnoPizzas.setHoraDesde(LocalTime.NOON);
		promoDosPorUnoPizzas.setHoraHasta(LocalTime.MIDNIGHT.minusSeconds(1));
		promoDosPorUnoPizzas.setDescripcionDescuento("Llevando 2 Pizzas Margaritas, pagas solo una.");
		promoDosPorUnoPizzas.setPrecioPromocional(this.pizzaMargaritaInstance.getPrecioVenta()); // Usar la instancia
		promoDosPorUnoPizzas.setEstadoActivo(true);
		Imagen imgPromoPizzas = new Imagen();
		imgPromoPizzas.setDenominacion("Flyer Promo Pizzas");
		promoDosPorUnoPizzas.setImagenes(new ArrayList<>(List.of(imgPromoPizzas)));

		PromocionDetalle detallePromoPizza = new PromocionDetalle();
		detallePromoPizza.setArticulo(this.pizzaMargaritaInstance); // Usar la instancia
		detallePromoPizza.setCantidad(2);
		detallePromoPizza.setPromocion(promoDosPorUnoPizzas);
		// El método addDetallePromocion en Promocion debería hacer:
		// this.detallesPromocion.add(detalle); detalle.setPromocion(this);
		// Si no lo tienes, la forma actual es:
		if (promoDosPorUnoPizzas.getDetallesPromocion() == null) {
			promoDosPorUnoPizzas.setDetallesPromocion(new ArrayList<>());
		}
		promoDosPorUnoPizzas.getDetallesPromocion().add(detallePromoPizza);
		promocionRepository.save(promoDosPorUnoPizzas);

		sucursalCentro.addPromocion(promoDosPorUnoPizzas); // Usar el método helper
		sucursalRepository.save(sucursalCentro);


		// ---------- PEDIDO ----------
		Pedido primerPedido = new Pedido();
		primerPedido.setFechaPedido(LocalDate.now());
		primerPedido.setHoraEstimadaFinalizacion(LocalTime.now().plusMinutes(45));
		primerPedido.setSucursal(sucursalCentro);
		primerPedido.setDomicilio(domicilioCliente);
		primerPedido.setEstado(Estado.PREPARACION);
		primerPedido.setTipoEnvio(TipoEnvio.DELIVERY);
		primerPedido.setFormaPago(FormaPago.MERCADO_PAGO);


		primerPedido.setEstadoActivo(true);
		primerPedido.setCliente(juanPerez);

		DetallePedido detallePedidoPizza = new DetallePedido();
		detallePedidoPizza.setArticulo(this.pizzaMargaritaInstance); // Usar la instancia
		detallePedidoPizza.setCantidad(1);
		detallePedidoPizza.setSubTotal(this.pizzaMargaritaInstance.getPrecioVenta());

		DetallePedido detallePedidoBebida = new DetallePedido();
		detallePedidoBebida.setArticulo(articuloBebida);
		detallePedidoBebida.setCantidad(2);
		detallePedidoBebida.setSubTotal(articuloBebida.getPrecioVenta() * 2);

		primerPedido.addDetalle(detallePedidoPizza); // Usar el método helper
		primerPedido.addDetalle(detallePedidoBebida); // Usar el método helper

		double totalPedido = 0;
		if (primerPedido.getDetalles() != null) {
			for(DetallePedido dp : primerPedido.getDetalles()){
				if(dp.getSubTotal() != null) totalPedido += dp.getSubTotal();
			}
		}
		primerPedido.setTotal(totalPedido);
		primerPedido.setTotalCosto(totalPedido * 0.4);
		pedidoRepository.save(primerPedido);

		// ---------- FACTURA ----------
		Factura facturaPedido = new Factura();
		facturaPedido.setPedido(primerPedido);
		facturaPedido.setFormaPago(primerPedido.getFormaPago());
		facturaPedido.setMpPreferenceId("pref-" + primerPedido.getId());

		double totalFactura = 0;
		for(DetallePedido dp : primerPedido.getDetalles()){
			FacturaDetalle fd = new FacturaDetalle();
			fd.setArticulo(dp.getArticulo());
			fd.setCantidad(dp.getCantidad());
			fd.setDenominacionArticulo(dp.getArticulo().getDenominacion());
			fd.setPrecioUnitarioArticulo(dp.getArticulo().getPrecioVenta());
			fd.setSubTotal(dp.getCantidad() * dp.getArticulo().getPrecioVenta());
			facturaPedido.addDetalleFactura(fd); // Usar el método helper de Factura
			totalFactura += fd.getSubTotal();
		}
		facturaPedido.setTotalVenta(totalFactura);
		// facturaPedido.setEstadoFactura(EstadoFactura.ACTIVA); // Ya se hace en el constructor de Factura
		facturaRepository.save(facturaPedido);

		primerPedido.setFactura(facturaPedido);
		pedidoRepository.save(primerPedido);

		System.out.println("Datos insertados correctamente!");
	}

	@Transactional
	public void actualizarArticulo() {
		if (this.pizzaMargaritaInstance == null || this.pizzaMargaritaInstance.getId() == null) {
			System.err.println("No se puede actualizar 'pizzaMargaritaInstance' porque no fue inicializada o guardada correctamente en insertarDatos().");
			Articulo articuloFallback = articuloRepository.findByDenominacion("Pizza Margarita"); // Usar el método del repo
			if (articuloFallback instanceof ArticuloManufacturado) {
				this.pizzaMargaritaInstance = (ArticuloManufacturado) articuloFallback;
			} else {
				System.err.println("No se encontró la Pizza Margarita para actualizar por denominación.");
				return;
			}
			// Doble chequeo por si findByDenominacion devuelve null y no se maneja con Optional
			if (this.pizzaMargaritaInstance == null || this.pizzaMargaritaInstance.getId() == null){
				System.err.println("No se pudo obtener la pizza para actualizar.");
				return;
			}
		}

		final Integer idPizzaParaActualizar = this.pizzaMargaritaInstance.getId(); // Efectivamente final

		Articulo articuloAActualizar = articuloRepository.findById(idPizzaParaActualizar)
				.orElseThrow(() -> new RuntimeException("No se encontró el artículo con ID: " + idPizzaParaActualizar + " para actualizar."));

		System.out.println("Actualizando: " + articuloAActualizar.getDenominacion());
		articuloAActualizar.setDenominacion("Pizza Margarita Clásica Recargada");
		articuloAActualizar.setPrecioVenta(5250.0);
		articuloAActualizar.setEstadoActivo(false);

		articuloRepository.save(articuloAActualizar);
		System.out.println("Artículo actualizado a: " + articuloAActualizar.getDenominacion() + " y estado: " + articuloAActualizar.getEstadoActivo());
	}
}