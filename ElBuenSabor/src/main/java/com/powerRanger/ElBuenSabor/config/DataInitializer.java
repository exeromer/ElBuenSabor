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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    // Repositorios necesarios para la carga de datos
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
    @Autowired private PromocionRepository promocionRepository;
    @Autowired private StockInsumoSucursalRepository stockInsumoSucursalRepository;


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
        Pais argentina = new Pais();
        argentina.setNombre("Argentina");
        paisRepository.save(argentina);

        Provincia mendoza = new Provincia();
        mendoza.setNombre("Mendoza");
        mendoza.setPais(argentina);
        provinciaRepository.save(mendoza);

        Provincia cordoba = new Provincia();
        cordoba.setNombre("Córdoba");
        cordoba.setPais(argentina);
        provinciaRepository.save(cordoba);

        Localidad ciudadMendoza = new Localidad();
        ciudadMendoza.setNombre("Ciudad de Mendoza");
        ciudadMendoza.setProvincia(mendoza);
        localidadRepository.save(ciudadMendoza);

        Localidad guaymallen = new Localidad();
        guaymallen.setNombre("Guaymallén");
        guaymallen.setProvincia(mendoza);
        localidadRepository.save(guaymallen);

        // --- Unidades de Medida ---
        System.out.println("Cargando Unidades de Medida...");
        UnidadMedida umGramos = new UnidadMedida();
        umGramos.setDenominacion("Gramos");
        unidadMedidaRepository.save(umGramos);

        UnidadMedida umUnidad = new UnidadMedida();
        umUnidad.setDenominacion("Unidad");
        unidadMedidaRepository.save(umUnidad);

        UnidadMedida umLitro = new UnidadMedida();
        umLitro.setDenominacion("Litro");
        unidadMedidaRepository.save(umLitro);

        UnidadMedida umCm3 = new UnidadMedida();
        umCm3.setDenominacion("cm3");
        unidadMedidaRepository.save(umCm3);

        // --- Categorías ---
        System.out.println("Cargando Categorías...");
        Categoria catPizzas = new Categoria();
        catPizzas.setDenominacion("Pizzas");
        catPizzas.setEstadoActivo(true);
        categoriaRepository.save(catPizzas);

        Categoria catHamburguesas = new Categoria();
        catHamburguesas.setDenominacion("Hamburguesas");
        catHamburguesas.setEstadoActivo(true);
        categoriaRepository.save(catHamburguesas);

        Categoria catBebidas = new Categoria();
        catBebidas.setDenominacion("Bebidas");
        catBebidas.setEstadoActivo(true);
        categoriaRepository.save(catBebidas);

        Categoria catInsumos = new Categoria();
        catInsumos.setDenominacion("Insumos");
        catInsumos.setEstadoActivo(true);
        categoriaRepository.save(catInsumos);

        Categoria catLomos = new Categoria();
        catLomos.setDenominacion("Lomos");
        catLomos.setEstadoActivo(true);
        categoriaRepository.save(catLomos);

        Categoria catTragos = new Categoria();
        catTragos.setDenominacion("Tragos");
        catTragos.setEstadoActivo(true);
        categoriaRepository.save(catTragos);

        // --- Empresa ---
        System.out.println("Cargando Empresa...");
        Empresa empresa1 = new Empresa();
        empresa1.setNombre("El Buen Sabor Central");
        empresa1.setRazonSocial("El Buen Sabor S.A.");
        empresa1.setCuil("30-12345678-9");
        empresaRepository.save(empresa1);

        // --- Sucursales ---
        System.out.println("Cargando Sucursales...");
        Domicilio domSuc1 = new Domicilio();
        domSuc1.setCalle("Av. San Martín");
        domSuc1.setNumero(1234);
        domSuc1.setCp("5500");
        domSuc1.setLocalidad(ciudadMendoza);

        Sucursal suc1 = new Sucursal();
        suc1.setNombre("Buen Sabor - Centro");
        suc1.setHorarioApertura(LocalTime.of(11, 0));
        suc1.setHorarioCierre(LocalTime.of(23, 59));
        suc1.setEmpresa(empresa1);
        suc1.setDomicilio(domSuc1);
        suc1.addCategoria(catPizzas);
        suc1.addCategoria(catHamburguesas);
        suc1.addCategoria(catLomos);
        suc1.addCategoria(catBebidas);
        suc1.addCategoria(catTragos);
        suc1.setEstadoActivo(true);
        sucursalRepository.save(suc1);

        Domicilio domSuc2 = new Domicilio();
        domSuc2.setCalle("Av. Las Heras");
        domSuc2.setNumero(567);
        domSuc2.setCp("5519");
        domSuc2.setLocalidad(guaymallen);

        Sucursal suc2 = new Sucursal();
        suc2.setNombre("Buen Sabor - Shopping");
        suc2.setHorarioApertura(LocalTime.of(10, 0));
        suc2.setHorarioCierre(LocalTime.of(22, 0));
        suc2.setEmpresa(empresa1);
        suc2.setDomicilio(domSuc2);
        suc2.addCategoria(catPizzas);
        suc2.addCategoria(catHamburguesas);
        suc2.addCategoria(catBebidas);
        suc2.setEstadoActivo(true);
        sucursalRepository.save(suc2);

        List<Sucursal> todasLasSucursales = sucursalRepository.findAll();

        // --- Artículos Insumo para Elaborar (40) ---
        System.out.println("Cargando Artículos Insumo para Elaborar...");
        // (Tu lógica para crear insumos aquí...)


        // --- Artículos Insumo para Venta (15) ---
        System.out.println("Cargando Artículos Insumo para Venta...");
        // (Tu lógica para crear insumos aquí...)


        // --- Artículos Manufacturados (15) ---
        System.out.println("Cargando Artículos Manufacturados...");
        // (Aquí puedes agregar tus 15 productos manufacturados manualmente desde el frontend)


        // --- Promociones (5) ---
        System.out.println("Cargando Promociones...");
        // (Aquí puedes agregar tus 5 promociones manualmente desde el frontend)


        System.out.println("DataInitializer: Carga de datos base completada. Los datos de clientes, empleados y pedidos deben ser creados desde el frontend.");
        System.out.println("====================================================================");
        System.out.println("FINALIZADO: DataInitializer.");
        System.out.println("====================================================================");
    }
}