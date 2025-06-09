package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*;
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
import java.util.stream.Collectors;

@Service
@Validated
public class SucursalServiceImpl extends BaseServiceImpl<Sucursal, SucursalRepository> implements SucursalService {

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    @Autowired private LocalidadRepository localidadRepository;
    @Autowired private PromocionRepository promocionRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    public SucursalServiceImpl(SucursalRepository sucursalRepository) {
        super(sucursalRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SucursalResponseDTO> findAllSucursales() {
        try {
            return super.findAll().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar sucursales: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SucursalResponseDTO findSucursalById(Integer id) throws Exception {
        return convertToDto(super.findById(id));
    }

    @Override
    @Transactional
    public SucursalResponseDTO createSucursal(@Valid SucursalRequestDTO dto) throws Exception {
        Sucursal sucursal = new Sucursal();
        mapDtoToEntity(dto, sucursal, true);
        return convertToDto(super.save(sucursal));
    }

    @Override
    @Transactional
    public SucursalResponseDTO updateSucursal(Integer id, @Valid SucursalRequestDTO dto) throws Exception {
        Sucursal entidadSucursal = super.findById(id);
        mapDtoToEntity(dto, entidadSucursal, false);
        return convertToDto(super.update(id, entidadSucursal));
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Sucursal sucursal = super.findById(id);
        sucursal.setEstadoActivo(false);
        sucursal.setFechaBaja(LocalDate.now());
        super.save(sucursal);
    }

    // MAPPERS Y LÓGICA PRIVADA (COMPLETA)
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

        List<Categoria> nuevasCategorias = new ArrayList<>();
        if (dto.getCategoriaIds() != null) {
            for (Integer categoriaId : new HashSet<>(dto.getCategoriaIds())) {
                Categoria categoria = categoriaRepository.findById(categoriaId)
                        .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + categoriaId));
                nuevasCategorias.add(categoria);
            }
        }
        sucursal.setCategorias(nuevasCategorias);

        List<Promocion> nuevasPromociones = new ArrayList<>();
        if (dto.getPromocionIds() != null) {
            for (Integer promocionId : new HashSet<>(dto.getPromocionIds())) {
                Promocion promocion = promocionRepository.findById(promocionId)
                        .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + promocionId));
                nuevasPromociones.add(promocion);
            }
        }
        sucursal.setPromociones(nuevasPromociones);
    }

    private SucursalResponseDTO convertToDto(Sucursal sucursal) {
        SucursalResponseDTO dto = new SucursalResponseDTO();
        dto.setId(sucursal.getId());
        dto.setNombre(sucursal.getNombre());
        dto.setHorarioApertura(sucursal.getHorarioApertura());
        dto.setHorarioCierre(sucursal.getHorarioCierre());
        dto.setEstadoActivo(sucursal.getEstadoActivo());
        dto.setFechaBaja(sucursal.getFechaBaja());

        if (sucursal.getEmpresa() != null) dto.setEmpresa(convertEmpresaToDto(sucursal.getEmpresa()));
        if (sucursal.getDomicilio() != null) dto.setDomicilio(convertDomicilioToDto(sucursal.getDomicilio()));
        if (sucursal.getCategorias() != null) dto.setCategorias(sucursal.getCategorias().stream().map(this::convertCategoriaToDto).collect(Collectors.toList()));
        if (sucursal.getPromociones() != null) dto.setPromociones(sucursal.getPromociones().stream().map(this::convertPromocionToSimpleDto).collect(Collectors.toList()));

        return dto;
    }

    private LocalTime parseTime(String timeString, String fieldName) throws Exception {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new Exception("El " + fieldName + " no puede estar vacío.");
        }
        try {
            return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e1) {
            try {
                return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
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

    // Otros mappers
    private PaisResponseDTO convertPaisToDto(Pais pais) { if (pais == null) return null; PaisResponseDTO dto = new PaisResponseDTO(); dto.setId(pais.getId()); dto.setNombre(pais.getNombre()); return dto; }
    private ProvinciaResponseDTO convertProvinciaToDto(Provincia provincia) { if (provincia == null) return null; ProvinciaResponseDTO dto = new ProvinciaResponseDTO(); dto.setId(provincia.getId()); dto.setNombre(provincia.getNombre()); dto.setPais(convertPaisToDto(provincia.getPais())); return dto; }
    private LocalidadResponseDTO convertLocalidadToDto(Localidad localidad) { if (localidad == null) return null; LocalidadResponseDTO dto = new LocalidadResponseDTO(); dto.setId(localidad.getId()); dto.setNombre(localidad.getNombre()); dto.setProvincia(convertProvinciaToDto(localidad.getProvincia())); return dto; }
    private DomicilioResponseDTO convertDomicilioToDto(Domicilio domicilio) { if (domicilio == null) return null; DomicilioResponseDTO dto = new DomicilioResponseDTO(); dto.setId(domicilio.getId()); dto.setCalle(domicilio.getCalle()); dto.setNumero(domicilio.getNumero()); dto.setCp(domicilio.getCp()); dto.setLocalidad(convertLocalidadToDto(domicilio.getLocalidad())); return dto; }
    private EmpresaResponseDTO convertEmpresaToDto(Empresa empresa) { if (empresa == null) return null; EmpresaResponseDTO dto = new EmpresaResponseDTO(); dto.setId(empresa.getId()); dto.setNombre(empresa.getNombre()); dto.setRazonSocial(empresa.getRazonSocial()); dto.setCuil(empresa.getCuil()); return dto; }
    private CategoriaResponseDTO convertCategoriaToDto(Categoria categoria) { if (categoria == null) return null; CategoriaResponseDTO dto = new CategoriaResponseDTO(); dto.setId(categoria.getId()); dto.setDenominacion(categoria.getDenominacion()); dto.setEstadoActivo(categoria.getEstadoActivo()); return dto; }
    private PromocionSimpleResponseDTO convertPromocionToSimpleDto(Promocion promocion) { if (promocion == null) return null; PromocionSimpleResponseDTO dto = new PromocionSimpleResponseDTO(); dto.setId(promocion.getId()); dto.setDenominacion(promocion.getDenominacion()); return dto; }
}