package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO;
import com.powerRanger.ElBuenSabor.entities.Articulo;
import com.powerRanger.ElBuenSabor.entities.Imagen;
import com.powerRanger.ElBuenSabor.entities.Promocion;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.ImagenRepository;
import com.powerRanger.ElBuenSabor.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ImagenServiceImpl extends BaseServiceImpl<Imagen, ImagenRepository> implements ImagenService {

    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private PromocionRepository promocionRepository;
    @Autowired private FileStorageService fileStorageService;

    public ImagenServiceImpl(ImagenRepository imagenRepository) {
        super(imagenRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagenResponseDTO> findAllImagenes() {
        try {
            return super.findAll().stream()
                    .map(this::convertToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar imágenes: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenResponseDTO findImagenById(Integer id) throws Exception {
        return convertToResponseDto(super.findById(id));
    }

    @Override
    @Transactional
    public ImagenResponseDTO createImagen(@Valid ImagenRequestDTO dto) throws Exception {
        Imagen imagen = new Imagen();
        mapRequestDtoToEntity(dto, imagen);
        return convertToResponseDto(super.save(imagen));
    }

    @Override
    @Transactional
    public ImagenResponseDTO updateImagen(Integer id, @Valid ImagenRequestDTO dto) throws Exception {
        Imagen imagenExistente = super.findById(id);
        mapRequestDtoToEntity(dto, imagenExistente);
        return convertToResponseDto(super.update(id, imagenExistente));
    }

    @Override
    @Transactional
    public void deleteImagenCompleta(Integer id) throws Exception {
        Imagen imagen = super.findById(id);
        String denominacion = imagen.getDenominacion();
        String filename = null;

        if (denominacion != null && denominacion.contains("/api/files/view/")) {
            filename = denominacion.substring(denominacion.lastIndexOf("/") + 1);
        } else if (denominacion != null && !denominacion.contains("/")) {
            filename = denominacion;
        }

        super.delete(id); // Borra de la BD

        if (filename != null && !filename.isEmpty()) {
            try {
                fileStorageService.delete(filename); // Borra del disco
                System.out.println("Archivo físico '" + filename + "' eliminado del disco.");
            } catch (Exception e) {
                System.err.println("Error al intentar borrar el archivo físico '" + filename + "' del disco: " + e.getMessage());
                // No relanzamos la excepción para no revertir la transacción de borrado de BD si solo falla el borrado de archivo.
            }
        }
    }

    private ImagenResponseDTO convertToResponseDto(Imagen imagen) {
        if (imagen == null) return null;
        ImagenResponseDTO dto = new ImagenResponseDTO();
        dto.setId(imagen.getId());
        dto.setDenominacion(imagen.getDenominacion());
        dto.setEstadoActivo(imagen.getEstadoActivo());
        if (imagen.getArticulo() != null) {
            dto.setArticuloId(imagen.getArticulo().getId());
            dto.setArticuloDenominacion(imagen.getArticulo().getDenominacion());
        }
        if (imagen.getPromocion() != null) {
            dto.setPromocionId(imagen.getPromocion().getId());
            dto.setPromocionDenominacion(imagen.getPromocion().getDenominacion());
        }
        return dto;
    }

    private void mapRequestDtoToEntity(ImagenRequestDTO dto, Imagen imagen) throws Exception {
        imagen.setDenominacion(dto.getDenominacion());
        imagen.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        imagen.setArticulo(null); // Desasociar primero por si cambia
        if (dto.getArticuloId() != null) {
            Articulo articulo = articuloRepository.findById(dto.getArticuloId())
                    .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + dto.getArticuloId()));
            imagen.setArticulo(articulo);
        }

        imagen.setPromocion(null); // Desasociar primero por si cambia
        if (dto.getPromocionId() != null) {
            Promocion promocion = promocionRepository.findById(dto.getPromocionId())
                    .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + dto.getPromocionId()));
            imagen.setPromocion(promocion);
        }
    }
}