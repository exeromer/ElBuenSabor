package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ImagenRequestDTO;
import com.powerRanger.ElBuenSabor.entities.Articulo; // Necesario si vas a asociar desde aquí
import com.powerRanger.ElBuenSabor.entities.Imagen;
import com.powerRanger.ElBuenSabor.entities.Promocion; // Necesario si vas a asociar desde aquí
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.ImagenRepository;
import com.powerRanger.ElBuenSabor.repository.PromocionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class ImagenServiceImpl implements ImagenService {

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private ArticuloRepository articuloRepository; // Para buscar Articulo al crear/actualizar Imagen

    @Autowired
    private PromocionRepository promocionRepository; // Para buscar Promocion al crear/actualizar Imagen

    @Autowired
    private FileStorageService fileStorageService; // Inyectar el servicio de archivos

    // Método helper para mapear DTO a Entidad y manejar asociaciones
    private void mapDtoToEntity(ImagenRequestDTO dto, Imagen imagen) throws Exception {
        imagen.setDenominacion(dto.getDenominacion());
        imagen.setEstadoActivo(dto.getEstadoActivo() != null ? dto.getEstadoActivo() : true);

        // Desasociar de Articulo si no se provee articuloId o es null
        imagen.setArticulo(null);
        if (dto.getArticuloId() != null) {
            Articulo articulo = articuloRepository.findById(dto.getArticuloId())
                    .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + dto.getArticuloId() + " para asociar a la imagen."));
            imagen.setArticulo(articulo);
            // Nota: Si Articulo tiene una List<Imagen> y es el dueño de la relación (con CascadeType.ALL),
            // también deberías añadir la imagen a la lista del artículo y guardar el artículo.
            // Ejemplo: articulo.addImagen(imagen); // asumiendo que addImagen maneja la bidireccionalidad
            // En este modelo, Imagen tiene la FK, así que setear imagen.setArticulo() es lo principal.
        }

        // Desasociar de Promocion si no se provee promocionId o es null
        imagen.setPromocion(null);
        if (dto.getPromocionId() != null) {
            // Asumimos que PromocionRepository y la entidad Promocion existen.
            // Si no, esta parte daría error o debería comentarse.
            Promocion promocion = promocionRepository.findById(dto.getPromocionId())
                    .orElseThrow(() -> new Exception("Promoción no encontrada con ID: " + dto.getPromocionId() + " para asociar a la imagen."));
            imagen.setPromocion(promocion);
            // Similar a Articulo, si Promocion tiene una List<Imagen> y es dueña.
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> getAllImagenes() {
        return imagenRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Imagen getImagenById(Integer id) throws Exception {
        return imagenRepository.findById(id)
                .orElseThrow(() -> new Exception("Imagen no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Imagen createImagen(@Valid ImagenRequestDTO dto) throws Exception {
        Imagen imagen = new Imagen();
        mapDtoToEntity(dto, imagen);
        return imagenRepository.save(imagen);
    }

    @Override
    @Transactional
    public Imagen updateImagen(Integer id, @Valid ImagenRequestDTO dto) throws Exception {
        Imagen imagenExistente = getImagenById(id); // Verifica si existe
        mapDtoToEntity(dto, imagenExistente);
        return imagenRepository.save(imagenExistente);
    }

    @Override
    @Transactional
    public void deleteImagen(Integer id) throws Exception {
        Imagen imagen = getImagenById(id); // Verifica si existe y obtiene la entidad

        String denominacion = imagen.getDenominacion();
        String filename = null;

        // Extraer el nombre del archivo de la denominacion (URL o path)
        // Esto asume que la denominacion es la URL completa generada por FileUploadController
        // o solo el nombre de archivo si así lo decidiste.
        if (denominacion != null && denominacion.contains("/api/files/view/")) {
            filename = denominacion.substring(denominacion.lastIndexOf("/") + 1);
        } else if (denominacion != null && !denominacion.contains("/")) {
            // Si la denominacion es solo el nombre del archivo (ej. UUID.jpg)
            filename = denominacion;
        }
        // Si la denominacion es una URL externa completa (ej. https://...), no intentamos borrar.

        // Primero borrar el registro de la base de datos
        imagenRepository.delete(imagen);

        // Si se pudo extraer un nombre de archivo local, intentar borrarlo del disco
        if (filename != null && !filename.isEmpty()) {
            try {
                fileStorageService.delete(filename);
                System.out.println("Archivo físico '" + filename + "' eliminado del disco.");
            } catch (Exception e) {
                // Es importante loguear este error, pero usualmente no queremos que la transacción
                // falle solo porque no se pudo borrar el archivo físico (la entidad DB ya se borró).
                System.err.println("Error al intentar borrar el archivo físico '" + filename + "' del disco: " + e.getMessage());
                // Podrías considerar un mecanismo de reintento o marcarlo para limpieza manual.
            }
        }
    }
}