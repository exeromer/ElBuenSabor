package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ImagenResponseDTO; // Importar DTO de respuesta
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
public class ImagenServiceImpl implements ImagenService {

    @Autowired private ImagenRepository imagenRepository;
    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private PromocionRepository promocionRepository;
    @Autowired private FileStorageService fileStorageService;

    // Método de Mapeo de Entidad a DTO de Respuesta
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

    // Método helper para mapear DTO de request a Entidad y manejar asociaciones
    private void mapRequestDtoToEntity(ImagenRequestDTO dto, Imagen imagen) throws Exception {
        imagen.setDenominacion(dto.getDenominacion());
        imagen.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        imagen.setArticulo(null); // Desasociar primero por si cambia
        if (dto.getArticuloId() != null) {
            Articulo articulo = articuloRepository.findById(dto.getArticuloId())
                    .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + dto.getArticuloId()));
            imagen.setArticulo(articulo);
            // Si Articulo tiene una lista de Imagenes y es bidireccional, actualizar el otro lado
            // articulo.addImagen(imagen); // Asumiendo que Articulo tiene este helper
        }

        imagen.setPromocion(null); // Desasociar primero por si cambia
        if (dto.getPromocionId() != null) {
            Promocion promocion = promocionRepository.findById(dto.getPromocionId())
                    .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + dto.getPromocionId()));
            imagen.setPromocion(promocion);
            // Si Promocion tiene una lista de Imagenes y es bidireccional, actualizar el otro lado
            // promocion.addImagen(imagen); // Asumiendo que Promocion tiene este helper
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImagenResponseDTO> getAllImagenes() {
        return imagenRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ImagenResponseDTO getImagenById(Integer id) throws Exception {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new Exception("Imagen no encontrada con ID: " + id));
        return convertToResponseDto(imagen);
    }

    @Override
    @Transactional
    public ImagenResponseDTO createImagen(@Valid ImagenRequestDTO dto) throws Exception {
        Imagen imagen = new Imagen();
        mapRequestDtoToEntity(dto, imagen);
        Imagen imagenGuardada = imagenRepository.save(imagen);
        return convertToResponseDto(imagenGuardada);
    }

    @Override
    @Transactional
    public ImagenResponseDTO updateImagen(Integer id, @Valid ImagenRequestDTO dto) throws Exception {
        Imagen imagenExistente = imagenRepository.findById(id)
                .orElseThrow(() -> new Exception("Imagen no encontrada con ID: " + id));
        mapRequestDtoToEntity(dto, imagenExistente);
        Imagen imagenActualizada = imagenRepository.save(imagenExistente);
        return convertToResponseDto(imagenActualizada);
    }

    @Override
    @Transactional
    public void deleteImagen(Integer id) throws Exception {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new Exception("Imagen no encontrada con ID: " + id));

        String denominacion = imagen.getDenominacion();
        String filename = null;

        if (denominacion != null && denominacion.contains("/api/files/view/")) {
            filename = denominacion.substring(denominacion.lastIndexOf("/") + 1);
        } else if (denominacion != null && !denominacion.contains("/")) {
            filename = denominacion;
        }

        // Desasociar de Articulo si es el lado dueño o para mantener consistencia si Articulo tiene colección
        if(imagen.getArticulo() != null && imagen.getArticulo().getImagenes() != null) {
            // Esta lógica es más compleja si Articulo es el dueño de una colección de Imagenes.
            // Por ahora, la entidad Imagen solo tiene una referencia @ManyToOne.
            // Si Articulo tiene @OneToMany(mappedBy="articulo", cascade=ALL, orphanRemoval=true) List<Imagen> imagenes,
            // y usas articulo.removeImagen(imagen), entonces guardar el artículo se encargaría.
            // Como estamos borrando la Imagen directamente, y ella es la dueña de la FK a Articulo (si así se mapea),
            // no hay mucho que hacer aquí para la colección de Articulo, solo borrar la Imagen.
            // PERO, si Articulo tiene una List<Imagen> y es la dueña, se debe borrar la imagen de esa lista y guardar Articulo.
            // El modelo actual de Imagen tiene el ManyToOne, así que al borrar Imagen se rompe la FK.
        }
        // Similar para Promocion

        imagenRepository.delete(imagen); // Borra de la BD

        if (filename != null && !filename.isEmpty()) {
            try {
                fileStorageService.delete(filename); // Borra del disco
                System.out.println("Archivo físico '" + filename + "' eliminado del disco.");
            } catch (Exception e) {
                System.err.println("Error al intentar borrar el archivo físico '" + filename + "' del disco: " + e.getMessage());
            }
        }
    }
}