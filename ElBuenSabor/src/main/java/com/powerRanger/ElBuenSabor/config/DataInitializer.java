package com.powerRanger.ElBuenSabor.config;

import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.entities.enums.*;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
// import java.util.List; // No es necesario aquí si las listas se inicializan en las entidades

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    @Autowired private LocalidadRepository localidadRepository;
    @Autowired private ProvinciaRepository provinciaRepository;
    @Autowired private PaisRepository paisRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private ArticuloManufacturadoRepository articuloManufacturadoRepository;
    @Autowired private ImagenRepository imagenRepository;
    @Autowired private PromocionRepository promocionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private FacturaRepository facturaRepository;

    private ArticuloManufacturado pizzaMargaritaInstance;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("====================================================================");
        System.out.println("INICIANDO: DataInitializer - Cargando datos de prueba...");
        System.out.println("====================================================================");

        if (empresaRepository.count() > 0) {
            System.out.println("DataInitializer: Los datos de prueba parecen ya existir. No se realiza la carga.");
            System.out.println("====================================================================");
            System.out.println("FINALIZADO: DataInitializer.");
            System.out.println("====================================================================");
            return;
        }

        // --- Datos Geográficos ---
        System.out.println("Cargando Datos Geográficos...");
        Pais argentina = new Pais(); argentina.setNombre("Argentina"); paisRepository.save(argentina);
        Pais chile = new Pais(); chile.setNombre("Chile"); paisRepository.save(chile);
        Provincia mendoza = new Provincia(); mendoza.setNombre("Mendoza"); mendoza.setPais(argentina); provinciaRepository.save(mendoza);
        Provincia cordoba = new Provincia(); cordoba.setNombre("Córdoba"); cordoba.setPais(argentina); provinciaRepository.save(cordoba);
        Localidad ciudadMendoza = new Localidad(); ciudadMendoza.setNombre("Ciudad de Mendoza"); ciudadMendoza.setProvincia(mendoza); localidadRepository.save(ciudadMendoza);
        Localidad guaymallen = new Localidad(); guaymallen.setNombre("Guaymallén"); guaymallen.setProvincia(mendoza); localidadRepository.save(guaymallen);

        // --- Unidades de Medida ---
        System.out.println("Cargando Unidades de Medida...");
        UnidadMedida umKg = new UnidadMedida(); umKg.setDenominacion("Kilogramo"); unidadMedidaRepository.save(umKg);
        UnidadMedida umUnidad = new UnidadMedida(); umUnidad.setDenominacion("Unidad"); unidadMedidaRepository.save(umUnidad);
        UnidadMedida umLitro = new UnidadMedida(); umLitro.setDenominacion("Litro"); unidadMedidaRepository.save(umLitro);
        UnidadMedida umGramos = new UnidadMedida(); umGramos.setDenominacion("Gramos"); unidadMedidaRepository.save(umGramos);

        // --- Categorías ---
        System.out.println("Cargando Categorías...");
        Categoria catPizzas = new Categoria(); catPizzas.setDenominacion("Pizzas"); catPizzas.setEstadoActivo(true); categoriaRepository.save(catPizzas);
        Categoria catHamburguesas = new Categoria(); catHamburguesas.setDenominacion("Hamburguesas"); catHamburguesas.setEstadoActivo(true); categoriaRepository.save(catHamburguesas);
        Categoria catBebidas = new Categoria(); catBebidas.setDenominacion("Bebidas"); catBebidas.setEstadoActivo(true); categoriaRepository.save(catBebidas);
        Categoria catInsumos = new Categoria(); catInsumos.setDenominacion("Insumos"); catInsumos.setEstadoActivo(true); categoriaRepository.save(catInsumos);

        // --- Artículos Insumo ---
        System.out.println("Cargando Artículos Insumo...");
        ArticuloInsumo harina = new ArticuloInsumo();
        harina.setDenominacion("Harina 000 (Gramos)"); harina.setPrecioVenta(0.05); // Precio por gramo
        harina.setUnidadMedida(umGramos); // Cambiado a Gramos
        harina.setCategoria(catInsumos); harina.setEstadoActivo(true); harina.setPrecioCompra(0.02); // Precio compra por gramo
        harina.setStockActual(10000.0); // 10kg en gramos
        harina.setstockMinimo(20000.0); // 20kg en gramos
        harina.setEsParaElaborar(true);
        articuloInsumoRepository.save(harina);

        ArticuloInsumo quesoMuzzaInsumo = new ArticuloInsumo();
        quesoMuzzaInsumo.setDenominacion("Queso Muzzarella (Gramos)"); quesoMuzzaInsumo.setPrecioVenta(0.20); // Precio por gramo
        quesoMuzzaInsumo.setUnidadMedida(umGramos); // Cambiado a Gramos
        quesoMuzzaInsumo.setCategoria(catInsumos); quesoMuzzaInsumo.setEstadoActivo(true); quesoMuzzaInsumo.setPrecioCompra(0.10);
        quesoMuzzaInsumo.setStockActual(5000.0); // 5kg en gramos
        quesoMuzzaInsumo.setstockMinimo(10000.0); // 10kg en gramos
        quesoMuzzaInsumo.setEsParaElaborar(true);
        articuloInsumoRepository.save(quesoMuzzaInsumo);

        ArticuloInsumo tomateTriturado = new ArticuloInsumo();
        tomateTriturado.setDenominacion("Tomate Triturado (ml)"); tomateTriturado.setPrecioVenta(0.08); // Precio por ml
        tomateTriturado.setUnidadMedida(umGramos); // Usaremos Gramos como proxy para ml si no tienes um "ml"
        tomateTriturado.setCategoria(catInsumos); tomateTriturado.setEstadoActivo(true); tomateTriturado.setPrecioCompra(0.04);
        tomateTriturado.setStockActual(3000.0); // 3L en ml
        tomateTriturado.setstockMinimo(6000.0); // 6L en ml
        tomateTriturado.setEsParaElaborar(true);
        articuloInsumoRepository.save(tomateTriturado);

        ArticuloInsumo panHamburguesa = new ArticuloInsumo();
        panHamburguesa.setDenominacion("Pan de Hamburguesa"); panHamburguesa.setPrecioVenta(1.0); panHamburguesa.setUnidadMedida(umUnidad);
        panHamburguesa.setCategoria(catInsumos); panHamburguesa.setEstadoActivo(true); panHamburguesa.setPrecioCompra(0.5);
        panHamburguesa.setStockActual(200.0); panHamburguesa.setstockMinimo(400.0); panHamburguesa.setEsParaElaborar(true);
        articuloInsumoRepository.save(panHamburguesa);

        ArticuloInsumo carneMolidaGramos = new ArticuloInsumo(); // Carne en gramos
        carneMolidaGramos.setDenominacion("Carne Molida Vacuna (Gramos)"); carneMolidaGramos.setPrecioVenta(0.15); // Precio por gramo
        carneMolidaGramos.setUnidadMedida(umGramos); // Cambiado a Gramos
        carneMolidaGramos.setCategoria(catInsumos); carneMolidaGramos.setEstadoActivo(true); carneMolidaGramos.setPrecioCompra(0.08);
        carneMolidaGramos.setStockActual(4000.0); // 4kg en gramos
        carneMolidaGramos.setstockMinimo(8000.0); // 8kg en gramos
        carneMolidaGramos.setEsParaElaborar(true);
        articuloInsumoRepository.save(carneMolidaGramos);

        // --- Artículos Manufacturados ---
        System.out.println("Cargando Artículos Manufacturados...");
        this.pizzaMargaritaInstance = new ArticuloManufacturado();
        pizzaMargaritaInstance.setDenominacion("Pizza Muzzarella"); pizzaMargaritaInstance.setPrecioVenta(100.0);
        pizzaMargaritaInstance.setUnidadMedida(umUnidad); pizzaMargaritaInstance.setCategoria(catPizzas);
        pizzaMargaritaInstance.setEstadoActivo(true); pizzaMargaritaInstance.setDescripcion("Clásica pizza de muzzarella");
        pizzaMargaritaInstance.setTiempoEstimadoMinutos(20); pizzaMargaritaInstance.setPreparacion("Preparación de muzzarella...");

        ArticuloManufacturadoDetalle amdHarinaMuzza = new ArticuloManufacturadoDetalle();
        amdHarinaMuzza.setArticuloInsumo(harina); // Harina (Gramos)
        amdHarinaMuzza.setCantidad(300.0); // 300 gramos (entero, pero como Double)
        amdHarinaMuzza.setEstadoActivo(true);
        pizzaMargaritaInstance.addManufacturadoDetalle(amdHarinaMuzza);

        ArticuloManufacturadoDetalle amdQuesoMuzza = new ArticuloManufacturadoDetalle();
        amdQuesoMuzza.setArticuloInsumo(quesoMuzzaInsumo); // Queso (Gramos)
        amdQuesoMuzza.setCantidad(250.0); // 250 gramos
        amdQuesoMuzza.setEstadoActivo(true);
        pizzaMargaritaInstance.addManufacturadoDetalle(amdQuesoMuzza);

        ArticuloManufacturadoDetalle amdSalsaMuzza = new ArticuloManufacturadoDetalle();
        amdSalsaMuzza.setArticuloInsumo(tomateTriturado); // Tomate (ml/Gramos)
        amdSalsaMuzza.setCantidad(100.0); // 100 ml/gramos
        amdSalsaMuzza.setEstadoActivo(true);
        pizzaMargaritaInstance.addManufacturadoDetalle(amdSalsaMuzza);
        articuloManufacturadoRepository.save(pizzaMargaritaInstance);

        ArticuloManufacturado hamburguesaClasica = new ArticuloManufacturado();
        hamburguesaClasica.setDenominacion("Hamburguesa Clásica"); hamburguesaClasica.setPrecioVenta(80.0);
        hamburguesaClasica.setUnidadMedida(umUnidad); hamburguesaClasica.setCategoria(catHamburguesas);
        hamburguesaClasica.setEstadoActivo(true); hamburguesaClasica.setDescripcion("Hamburguesa con carne, lechuga y tomate");
        hamburguesaClasica.setTiempoEstimadoMinutos(15); hamburguesaClasica.setPreparacion("Preparación de hamburguesa...");

        ArticuloManufacturadoDetalle amdPanHamb = new ArticuloManufacturadoDetalle();
        amdPanHamb.setArticuloInsumo(panHamburguesa);
        amdPanHamb.setCantidad(2.0); // 2 unidades de pan
        amdPanHamb.setEstadoActivo(true);
        hamburguesaClasica.addManufacturadoDetalle(amdPanHamb);

        ArticuloManufacturadoDetalle amdCarneHamb = new ArticuloManufacturadoDetalle();
        amdCarneHamb.setArticuloInsumo(carneMolidaGramos); // Carne (Gramos)
        amdCarneHamb.setCantidad(150.0); // 150 gramos
        amdCarneHamb.setEstadoActivo(true);
        hamburguesaClasica.addManufacturadoDetalle(amdCarneHamb);
        articuloManufacturadoRepository.save(hamburguesaClasica);

        // --- Empresas --- (Sin cambios)
        System.out.println("Cargando Empresas...");
        Empresa empresa1 = new Empresa();
        empresa1.setNombre("El Buen Sabor Central"); empresa1.setRazonSocial("El Buen Sabor S.A."); empresa1.setCuil("30-12345678-9");
        empresaRepository.save(empresa1);
        Empresa empresa2 = new Empresa();
        empresa2.setNombre("Sabores Regionales"); empresa2.setRazonSocial("Sabores Regionales S.R.L."); empresa2.setCuil("30-98765432-1");
        empresaRepository.save(empresa2);

        // --- Sucursales --- (Sin cambios en la lógica, solo asegurar datos consistentes)
        System.out.println("Cargando Sucursales...");
        Domicilio domSuc1E1 = new Domicilio(); domSuc1E1.setCalle("Av. Colón"); domSuc1E1.setNumero(100); domSuc1E1.setCp("C5000XAF"); domSuc1E1.setLocalidad(ciudadMendoza);
        Sucursal suc1E1 = new Sucursal();
        suc1E1.setNombre("Buen Sabor - Colón"); suc1E1.setHorarioApertura(LocalTime.of(10,0)); suc1E1.setHorarioCierre(LocalTime.of(23,0));
        suc1E1.setEmpresa(empresa1); suc1E1.setDomicilio(domSuc1E1); suc1E1.addCategoria(catPizzas); suc1E1.addCategoria(catBebidas); suc1E1.setEstadoActivo(true);
        sucursalRepository.save(suc1E1);

        Domicilio domSuc2E1 = new Domicilio(); domSuc2E1.setCalle("Belgrano"); domSuc2E1.setNumero(250); domSuc2E1.setCp("G5519ABC"); domSuc2E1.setLocalidad(guaymallen);
        Sucursal suc2E1 = new Sucursal();
        suc2E1.setNombre("Buen Sabor - Guaymallén"); suc2E1.setHorarioApertura(LocalTime.of(11,0)); suc2E1.setHorarioCierre(LocalTime.of(0,0));
        suc2E1.setEmpresa(empresa1); suc2E1.setDomicilio(domSuc2E1); suc2E1.addCategoria(catPizzas); suc2E1.setEstadoActivo(true);
        sucursalRepository.save(suc2E1);

        // --- Promociones --- (Asegurar que los artículos en promoción existan)
        System.out.println("Cargando Promociones...");
        ArticuloInsumo gaseosa = new ArticuloInsumo();
        gaseosa.setDenominacion("Gaseosa Cola 500ml"); gaseosa.setPrecioVenta(4.0); gaseosa.setUnidadMedida(umUnidad);
        gaseosa.setCategoria(catBebidas); gaseosa.setEstadoActivo(true); gaseosa.setPrecioCompra(1.5);
        gaseosa.setStockActual(100.0); gaseosa.setstockMinimo(300.0); gaseosa.setEsParaElaborar(false);
        articuloInsumoRepository.save(gaseosa);

        Promocion promo1Suc1 = new Promocion();
        promo1Suc1.setDenominacion("2x1 Muzzarellas"); promo1Suc1.setFechaDesde(LocalDate.now()); promo1Suc1.setFechaHasta(LocalDate.now().plusMonths(1));
        promo1Suc1.setHoraDesde(LocalTime.of(19,0)); promo1Suc1.setHoraHasta(LocalTime.of(23,0));
        promo1Suc1.setDescripcionDescuento("Llevate 2 pizzas muzzarella al precio de 1");
        promo1Suc1.setPrecioPromocional(pizzaMargaritaInstance.getPrecioVenta());
        promo1Suc1.setEstadoActivo(true);
        PromocionDetalle pd1Promo1 = new PromocionDetalle(); pd1Promo1.setArticulo(pizzaMargaritaInstance); pd1Promo1.setCantidad(2);
        promo1Suc1.addDetallePromocion(pd1Promo1);
        promocionRepository.save(promo1Suc1);
        suc1E1.addPromocion(promo1Suc1);
        sucursalRepository.save(suc1E1);


        // --- Usuarios y Clientes --- (Sin cambios)
        System.out.println("Cargando Usuarios y Clientes...");
        Usuario user1 = new Usuario("auth0|cliente1", "clienteAna", Rol.CLIENTE); usuarioRepository.save(user1);
        Cliente cliente1 = new Cliente(); cliente1.setNombre("Ana"); cliente1.setApellido("Garcia"); cliente1.setEmail("ana.g@example.com"); cliente1.setTelefono("2610001111"); cliente1.setEstadoActivo(true); cliente1.setUsuario(user1);
        Domicilio domCli1 = new Domicilio(); domCli1.setCalle("Calle Sol"); domCli1.setNumero(111); domCli1.setCp("M5500SOL"); domCli1.setLocalidad(ciudadMendoza); domicilioRepository.save(domCli1);
        cliente1.addDomicilio(domCli1); clienteRepository.save(cliente1);

        // --- Pedidos y Facturas --- (Sin cambios)
        System.out.println("Cargando Pedido y Factura de ejemplo...");
        if (suc1E1 != null && cliente1 != null && domCli1 != null && pizzaMargaritaInstance != null && gaseosa != null) {
            Pedido pedido1 = new Pedido();
            pedido1.setFechaPedido(LocalDate.now().minusDays(1));
            pedido1.setHoraEstimadaFinalizacion(LocalTime.of(21,0));
            pedido1.setTipoEnvio(TipoEnvio.DELIVERY);
            pedido1.setFormaPago(FormaPago.EFECTIVO);
            pedido1.setEstado(Estado.ENTREGADO);
            pedido1.setSucursal(suc1E1);
            pedido1.setCliente(cliente1);
            pedido1.setDomicilio(domCli1);

            DetallePedido dp1Ped1 = new DetallePedido();
            dp1Ped1.setArticulo(pizzaMargaritaInstance); dp1Ped1.setCantidad(1); dp1Ped1.setSubTotal(pizzaMargaritaInstance.getPrecioVenta() * 1);
            pedido1.addDetalle(dp1Ped1);

            DetallePedido dp2Ped1 = new DetallePedido();
            dp2Ped1.setArticulo(gaseosa); dp2Ped1.setCantidad(2); dp2Ped1.setSubTotal(gaseosa.getPrecioVenta() * 2);
            pedido1.addDetalle(dp2Ped1);

            pedido1.setTotal(dp1Ped1.getSubTotal() + dp2Ped1.getSubTotal());
            pedidoRepository.save(pedido1);

            Factura factura1 = new Factura();
            factura1.setPedido(pedido1);
            factura1.setFechaFacturacion(pedido1.getFechaPedido());
            factura1.setFormaPago(pedido1.getFormaPago());
            factura1.setTotalVenta(pedido1.getTotal());
            for (DetallePedido dp : pedido1.getDetalles()) {
                FacturaDetalle fd = new FacturaDetalle();
                fd.setArticulo(dp.getArticulo()); fd.setCantidad(dp.getCantidad());
                fd.setDenominacionArticulo(dp.getArticulo().getDenominacion());
                fd.setPrecioUnitarioArticulo(dp.getArticulo().getPrecioVenta());
                fd.setSubTotal(dp.getSubTotal());
                factura1.addDetalleFactura(fd);
            }
            facturaRepository.save(factura1);
            pedido1.setFactura(factura1);
            pedidoRepository.save(pedido1);
        }

        System.out.println("DataInitializer: Datos de prueba insertados correctamente!");
        System.out.println("====================================================================");
        System.out.println("FINALIZADO: DataInitializer.");
        System.out.println("====================================================================");
    }
}