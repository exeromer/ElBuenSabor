package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.PromocionRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.PromocionDetalleRequestDTO;
import com.powerRanger.ElBuenSabor.entities.*; // Todas las entidades
import com.powerRanger.ElBuenSabor.repository.*; // Todos los repositorios
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Validated
public class PromocionServiceImpl implements PromocionService {

    @Autowired private PromocionRepository promocionRepository;
    @Autowired private ImagenRepository imagenRepository;
    @Autowired private ArticuloRepository articuloRepository; // Para buscar Articulo (base)
    // PromocionDetalleRepository no se usa directamente para el CRUD principal de Promocion si los detalles se manejan en cascada

    private void mapDtoToEntity(PromocionRequestDTO dto, Promocion promocion) throws Exception {
        promocion.setDenominacion(dto.getDenominacion());
        promocion.setFechaDesde(dto.getFechaDesde());
        promocion.setFechaHasta(dto.getFechaHasta());
        promocion.setHoraDesde(dto.getHoraDesde());
        promocion.setHoraHasta(dto.getHoraHasta());
        promocion.setDescripcionDescuento(dto.getDescripcionDescuento());
        promocion.setPrecioPromocional(dto.getPrecioPromocional());
        promocion.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        // Manejo de Imágenes (asociar existentes)
        if (promocion.getImagenes() == null) promocion.setImagenes(new ArrayList<>());
        promocion.getImagenes().clear(); // Limpiar para actualización
        if (dto.getImagenIds() != null && !dto.getImagenIds().isEmpty()) {
            for (Integer imagenId : new HashSet<>(dto.getImagenIds())) { // HashSet para evitar duplicados
                Imagen imagen = imagenRepository.findById(imagenId)
                        .orElseThrow(() -> new Exception("Imagen no encontrada con ID: " + imagenId));
                // Aquí la entidad Imagen es la dueña de la relación con Promocion (Imagen tiene promocion_id)
                // Si queremos que Promocion tenga una lista de Imagenes y sea la dueña (con tabla de unión o mappedBy en Imagen)
                // el manejo sería diferente.
                // Asumiendo el modelo actual de Imagen (Imagen tiene promocion_id):
                // Si se crea una nueva imagen, se le setea la promoción.
                // Si se asocian existentes, la Imagen debe tener un setPromocion() y guardarse.
                // O más simple: Promocion.addImagen(imagen) que setea imagen.setPromocion(this).
                promocion.addImagen(imagen); // Asume que addImagen maneja la bidireccionalidad
            }
        }

        // Manejo de Detalles de Promoción
        if (promocion.getDetallesPromocion() == null) promocion.setDetallesPromocion(new ArrayList<>());
        promocion.getDetallesPromocion().clear(); // Estrategia: borrar y recrear
        if (dto.getDetallesPromocion() != null && !dto.getDetallesPromocion().isEmpty()) {
            for (PromocionDetalleRequestDTO detalleDto : dto.getDetallesPromocion()) {
                Articulo articulo = articuloRepository.findById(detalleDto.getArticuloId())
                        .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + detalleDto.getArticuloId()));

                PromocionDetalle nuevoDetalle = new PromocionDetalle();
                nuevoDetalle.setArticulo(articulo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad());
                promocion.addDetallePromocion(nuevoDetalle); // El helper establece la relación bidireccional
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Promocion> getAll() {
        return promocionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Promocion getById(Integer id) throws Exception {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Promocion create(@Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocion = new Promocion();
        mapDtoToEntity(dto, promocion);
        return promocionRepository.save(promocion); // CascadeType.ALL guardará los detalles e imágenes nuevas/asociadas
    }

    @Override
    @Transactional
    public Promocion update(Integer id, @Valid PromocionRequestDTO dto) throws Exception {
        Promocion promocionExistente = getById(id);
        mapDtoToEntity(dto, promocionExistente);
        return promocionRepository.save(promocionExistente);
    }

    @Override
    @Transactional
    public void softDelete(Integer id) throws Exception {
        Promocion promocion = getById(id);
        promocion.setEstadoActivo(false);
        // fechaBaja no está en Promocion actualmente, si la añades, actívala aquí
        // promocion.setFechaBaja(LocalDate.now());
        promocionRepository.save(promocion);
    }
}