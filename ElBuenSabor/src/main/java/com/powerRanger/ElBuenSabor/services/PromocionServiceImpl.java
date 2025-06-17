package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*; // Importar todos los DTOs
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.*;
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
public class PromocionServiceImpl implements PromocionService {

    @Autowired private PromocionRepository promocionRepository;
    @Autowired private ImagenRepository imagenRepository;
    @Autowired private ArticuloRepository articuloRepository;
    // Inyectar servicios de Imagen y Artículo si los mappers están allí, o tener mappers aquí.
    // Por simplicidad, haremos los mappers aquí.

    // --- MAPPERS ---
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
        // Si ImagenResponseDTO necesita IDs de artículo/promoción, y tu entidad Imagen los tiene
        // if (imagen.getArticulo() != null) dto.setArticuloId(imagen.getArticulo().getId());
        // if (imagen.getPromocion() != null) dto.setPromocionId(imagen.getPromocion().getId());
        return dto;
    }

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

        // Manejo de Imágenes
        List<Imagen> imagenesActuales = new ArrayList<>(promocion.getImagenes());
        for(Imagen img : imagenesActuales) {
            promocion.removeImagen(img); // Desasociar y marcar para orphanRemoval
        }
        if (dto.getImagenIds() != null) {
            for (Integer imagenId : new HashSet<>(dto.getImagenIds())) {
                Imagen imagen = imagenRepository.findById(imagenId)
                        .orElseThrow(() -> new Exception("Imagen no encontrada con ID: " + imagenId));
                promocion.addImagen(imagen); // Helper se encarga de la bidireccionalidad
            }
        }

        // Manejo de Detalles de Promoción
        List<PromocionDetalle> detallesActuales = new ArrayList<>(promocion.getDetallesPromocion());
        for(PromocionDetalle detalle : detallesActuales) {
            promocion.removeDetallePromocion(detalle); // Desasociar y marcar para orphanRemoval
        }
        if (dto.getDetallesPromocion() != null) {
            for (PromocionDetalleRequestDTO detalleDto : dto.getDetallesPromocion()) {
                Articulo articulo = articuloRepository.findById(detalleDto.getArticuloId())
                        .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + detalleDto.getArticuloId()));

                PromocionDetalle nuevoDetalle = new PromocionDetalle();
                nuevoDetalle.setArticulo(articulo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad());
                // Si PromocionDetalle tuviera estadoActivo, se setearía aquí
                promocion.addDetallePromocion(nuevoDetalle);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromocionResponseDTO> getAll() {
        return promocionRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PromocionResponseDTO getById(Integer id) throws Exception {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + id));
        return convertToResponseDto(promocion);
    }

    @Override
    @Transactional
    public PromocionResponseDTO create(@Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocion = new Promocion();
        // Inicializar listas en la nueva promoción
        promocion.setImagenes(new ArrayList<>());
        promocion.setDetallesPromocion(new ArrayList<>());
        mapRequestDtoToEntity(dto, promocion);
        Promocion promocionGuardada = promocionRepository.save(promocion);
        return convertToResponseDto(promocionGuardada);
    }

    @Override
    @Transactional
    public PromocionResponseDTO update(Integer id, @Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocionExistente = promocionRepository.findById(id)
                .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + id));
        mapRequestDtoToEntity(dto, promocionExistente);
        Promocion promocionActualizada = promocionRepository.save(promocionExistente);
        return convertToResponseDto(promocionActualizada);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + id));
        promocion.setEstadoActivo(false);
        // Si Promocion tuviera fechaBaja, se setearía aquí.
        promocionRepository.save(promocion);
    }
}