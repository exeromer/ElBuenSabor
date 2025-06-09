package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.ImagenRepository;
import com.powerRanger.ElBuenSabor.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class PromocionServiceImpl extends BaseServiceImpl<Promocion, PromocionRepository> implements PromocionService {

    @Autowired private ImagenRepository imagenRepository;
    @Autowired private ArticuloRepository articuloRepository;

    public PromocionServiceImpl(PromocionRepository promocionRepository) {
        super(promocionRepository);
    }

    // --- MÉTODOS ESPECÍFICOS IMPLEMENTADOS DESDE PromocionService ---

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> findAllPromociones() {
        try {
            // Llama al método genérico heredado
            return super.findAll().stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar las promociones: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResponseDTO findPromocionById(Integer id) throws Exception {
        // Llama al método genérico heredado
        Promocion promocion = super.findById(id);
        return convertToResponseDto(promocion);
    }

    @Override
    @Transactional
    public PromocionResponseDTO createPromocion(@Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocion = new Promocion();
        promocion.setImagenes(new ArrayList<>());
        promocion.setDetallesPromocion(new ArrayList<>());
        mapRequestDtoToEntity(dto, promocion);

        // Llama al método genérico heredado
        Promocion promocionGuardada = super.save(promocion);
        return convertToResponseDto(promocionGuardada);
    }

    @Override
    @Transactional
    public PromocionResponseDTO updatePromocion(Integer id, @Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocionExistente = super.findById(id);
        mapRequestDtoToEntity(dto, promocionExistente);

        // Llama al método genérico heredado
        Promocion promocionActualizada = super.update(id, promocionExistente);
        return convertToResponseDto(promocionActualizada);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Promocion promocion = super.findById(id);
        promocion.setEstadoActivo(false);
        // Llama al método genérico heredado para guardar el cambio
        super.save(promocion);
    }

    // --- MAPPERS Y LÓGICA PRIVADA (SIN CAMBIOS) ---

    private PromocionResponseDTO convertToResponseDto(Promocion promocion) {
        if (promocion == null) return null;
        PromocionResponseDTO dto = new PromocionResponseDTO();
        dto.setId(promocion.getId());
        dto.setDenominacion(promocion.getDenominacion());
        dto.setFechaDesde(promocion.getFechaDesde());
        dto.setFechaHasta(promocion.getFechaHasta());
        dto.setHoraDesde(promocion.getHoraDesde());
        dto.setHoraHasta(promocion.getHoraHasta());
        dto.setDescripcionDescuento(promocion.getDescripcionDescuento());
        dto.setPrecioPromocional(promocion.getPrecioPromocional());
        dto.setEstadoActivo(promocion.getEstadoActivo());

        if (promocion.getImagenes() != null) {
            dto.setImagenes(promocion.getImagenes().stream()
                    .map(this::convertImagenToDto)
                    .collect(Collectors.toList()));
        }
        if (promocion.getDetallesPromocion() != null) {
            dto.setDetallesPromocion(promocion.getDetallesPromocion().stream()
                    .map(this::convertPromocionDetalleToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private void mapRequestDtoToEntity(PromocionRequestDTO dto, Promocion promocion) throws Exception {
        promocion.setDenominacion(dto.getDenominacion());
        promocion.setFechaDesde(dto.getFechaDesde());
        promocion.setFechaHasta(dto.getFechaHasta());
        promocion.setHoraDesde(dto.getHoraDesde());
        promocion.setHoraHasta(dto.getHoraHasta());
        promocion.setDescripcionDescuento(dto.getDescripcionDescuento());
        promocion.setPrecioPromocional(dto.getPrecioPromocional());
        promocion.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        List<Imagen> imagenesActuales = new ArrayList<>(promocion.getImagenes());
        for(Imagen img : imagenesActuales) {
            promocion.removeImagen(img);
        }
        if (dto.getImagenIds() != null) {
            for (Integer imagenId : new HashSet<>(dto.getImagenIds())) {
                Imagen imagen = imagenRepository.findById(imagenId)
                        .orElseThrow(() -> new Exception("Imagen no encontrada con ID: " + imagenId));
                promocion.addImagen(imagen);
            }
        }

        List<PromocionDetalle> detallesActuales = new ArrayList<>(promocion.getDetallesPromocion());
        for(PromocionDetalle detalle : detallesActuales) {
            promocion.removeDetallePromocion(detalle);
        }
        if (dto.getDetallesPromocion() != null) {
            for (PromocionDetalleRequestDTO detalleDto : dto.getDetallesPromocion()) {
                Articulo articulo = articuloRepository.findById(detalleDto.getArticuloId())
                        .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + detalleDto.getArticuloId()));
                PromocionDetalle nuevoDetalle = new PromocionDetalle();
                nuevoDetalle.setArticulo(articulo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad());
                promocion.addDetallePromocion(nuevoDetalle);
            }
        }
    }

    private ArticuloSimpleResponseDTO convertArticuloToSimpleDto(Articulo articulo) {
        if (articulo == null) return null;
        ArticuloSimpleResponseDTO dto = new ArticuloSimpleResponseDTO();
        dto.setId(articulo.getId());
        dto.setDenominacion(articulo.getDenominacion());
        dto.setPrecioVenta(articulo.getPrecioVenta());
        return dto;
    }

    private PromocionDetalleResponseDTO convertPromocionDetalleToDto(PromocionDetalle detalle) {
        if (detalle == null) return null;
        PromocionDetalleResponseDTO dto = new PromocionDetalleResponseDTO();
        dto.setId(detalle.getId());
        dto.setCantidad(detalle.getCantidad());
        if (detalle.getArticulo() != null) {
            dto.setArticulo(convertArticuloToSimpleDto(detalle.getArticulo()));
        }
        return dto;
    }

    private ImagenResponseDTO convertImagenToDto(Imagen imagen) {
        if (imagen == null) return null;
        ImagenResponseDTO dto = new ImagenResponseDTO();
        dto.setId(imagen.getId());
        dto.setDenominacion(imagen.getDenominacion());
        dto.setEstadoActivo(imagen.getEstadoActivo());
        return dto;
    }
}