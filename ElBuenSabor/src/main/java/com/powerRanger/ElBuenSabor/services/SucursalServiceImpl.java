package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.DomicilioRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.SucursalRequestDTO;
import com.powerRanger.ElBuenSabor.entities.*;
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
import java.util.HashSet;
import java.util.List;

@Service
@Validated
public class SucursalServiceImpl implements SucursalService {

    @Autowired private SucursalRepository sucursalRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    @Autowired private LocalidadRepository localidadRepository;
    @Autowired private PromocionRepository promocionRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private LocalTime parseTime(String timeString, String fieldName) throws Exception {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new Exception("El " + fieldName + " no puede estar vacío.");
        }
        try {
            return LocalTime.parse(timeString, TIME_FORMATTER); // HH:mm
        } catch (DateTimeParseException e1) {
            try {
                return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss")); // HH:mm:ss
            } catch (DateTimeParseException e2) {
                throw new Exception("Formato de " + fieldName + " inválido. Use HH:mm o HH:mm:ss. Valor recibido: " + timeString);
            }
        }
    }

    private Domicilio createOrUpdateDomicilio(Domicilio existingDomicilio, DomicilioRequestDTO domicilioDto) throws Exception {
        Localidad localidad = localidadRepository.findById(domicilioDto.getLocalidadId())
                .orElseThrow(() -> new Exception("Localidad no encontrada con ID: " + domicilioDto.getLocalidadId()));

        Domicilio domicilioToSave = existingDomicilio != null ? existingDomicilio : new Domicilio();
        domicilioToSave.setCalle(domicilioDto.getCalle());
        domicilioToSave.setNumero(domicilioDto.getNumero());
        domicilioToSave.setCp(domicilioDto.getCp());
        domicilioToSave.setLocalidad(localidad);
        return domicilioRepository.save(domicilioToSave);
    }

    private void mapDtoToEntity(SucursalRequestDTO dto, Sucursal sucursal, boolean isCreate) throws Exception {
        sucursal.setNombre(dto.getNombre());
        sucursal.setHorarioApertura(parseTime(dto.getHorarioApertura(), "horario de apertura"));
        sucursal.setHorarioCierre(parseTime(dto.getHorarioCierre(), "horario de cierre"));
        sucursal.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new Exception("Empresa no encontrada con ID: " + dto.getEmpresaId()));
        sucursal.setEmpresa(empresa);

        if (dto.getDomicilio() == null) {
            throw new Exception("Los datos del domicilio son obligatorios.");
        }
        Domicilio domicilioManaged = createOrUpdateDomicilio(isCreate ? null : sucursal.getDomicilio(), dto.getDomicilio());
        sucursal.setDomicilio(domicilioManaged);

        // Manejo de Categorías
        List<Categoria> nuevasCategorias = new ArrayList<>();
        if (dto.getCategoriaIds() != null && !dto.getCategoriaIds().isEmpty()) {
            for (Integer categoriaId : new HashSet<>(dto.getCategoriaIds())) {
                Categoria categoria = categoriaRepository.findById(categoriaId)
                        .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + categoriaId));
                nuevasCategorias.add(categoria);
            }
        }
        sucursal.setCategorias(nuevasCategorias); // Asignar la nueva lista procesada

        // Manejo de Promociones
        List<Promocion> nuevasPromociones = new ArrayList<>();
        if (dto.getPromocionIds() != null && !dto.getPromocionIds().isEmpty()) {
            for (Integer promocionId : new HashSet<>(dto.getPromocionIds())) {
                Promocion promocion = promocionRepository.findById(promocionId)
                        .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + promocionId));
                nuevasPromociones.add(promocion);
            }
        }
        sucursal.setPromociones(nuevasPromociones); // Asignar la nueva lista procesada
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sucursal> getAll() {
        return sucursalRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Sucursal getById(Integer id) throws Exception {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new Exception("Sucursal no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Sucursal create(@Valid SucursalRequestDTO dto) throws Exception {
        Sucursal sucursal = new Sucursal();
        // Inicializar listas en la nueva sucursal para que no sean null antes de mapDtoToEntity
        sucursal.setCategorias(new ArrayList<>());
        sucursal.setPromociones(new ArrayList<>());
        mapDtoToEntity(dto, sucursal, true);
        return sucursalRepository.save(sucursal);
    }

    @Override
    @Transactional
    public Sucursal update(Integer id, @Valid SucursalRequestDTO dto) throws Exception {
        Sucursal sucursalExistente = getById(id);
        mapDtoToEntity(dto, sucursalExistente, false);
        return sucursalRepository.save(sucursalExistente);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Sucursal sucursal = getById(id);
        sucursal.setEstadoActivo(false);
        sucursal.setFechaBaja(LocalDate.now());
        sucursalRepository.save(sucursal);
    }
}