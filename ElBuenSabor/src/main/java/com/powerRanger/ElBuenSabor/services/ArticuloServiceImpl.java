package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*; // Importar todos los DTOs
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects; // Para Objects.nonNull en el mapper de imagen
import java.util.stream.Collectors;

@Service
@Validated
public class ArticuloServiceImpl implements ArticuloService {

    @Autowired private ArticuloRepository articuloRepository;
    @Autowired private CategoriaRepository categoriaRepository; // Necesario para create/update de Articulo base
    @Autowired private UnidadMedidaRepository unidadMedidaRepository; // Necesario para create/update de Articulo base
    // No inyectamos ImagenService aquí para evitar dependencia circular si ImagenService usa ArticuloService.
    // El mapeo de Imagen se hará de forma simple o se podría usar un ImagenMapper dedicado.

    // --- MAPPERS INTERNOS (o usar una clase Mappers externa) ---
    private UnidadMedidaResponseDTO convertUnidadMedidaToDto(UnidadMedida um) {
        if (um == null) return null;
        UnidadMedidaResponseDTO dto = new UnidadMedidaResponseDTO();
        dto.setId(um.getId());
        dto.setDenominacion(um.getDenominacion());
        return dto;
    }

    private CategoriaResponseDTO convertCategoriaToDto(Categoria cat) {
        if (cat == null) return null;
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(cat.getId());
        dto.setDenominacion(cat.getDenominacion());
        dto.setEstadoActivo(cat.getEstadoActivo());
        return dto;
    }

    private ImagenResponseDTO convertImagenToDto(Imagen img) {
        if (img == null) return null;
        ImagenResponseDTO dto = new ImagenResponseDTO();
        dto.setId(img.getId());
        dto.setDenominacion(img.getDenominacion());
        dto.setEstadoActivo(img.getEstadoActivo());
        if (img.getArticulo() != null) dto.setArticuloId(img.getArticulo().getId());
        if (img.getPromocion() != null) dto.setPromocionId(img.getPromocion().getId());
        return dto;
    }

    private ArticuloSimpleResponseDTO convertArticuloToSimpleDto(Articulo articulo) {
        if (articulo == null) return null;
        ArticuloSimpleResponseDTO dto = new ArticuloSimpleResponseDTO();
        dto.setId(articulo.getId());
        dto.setDenominacion(articulo.getDenominacion());
        dto.setPrecioVenta(articulo.getPrecioVenta());
        return dto;
    }

    private ArticuloManufacturadoDetalleResponseDTO convertAmdToDto(ArticuloManufacturadoDetalle amd) {
        if (amd == null) return null;
        ArticuloManufacturadoDetalleResponseDTO dto = new ArticuloManufacturadoDetalleResponseDTO();
        dto.setId(amd.getId());
        dto.setCantidad(amd.getCantidad());
        dto.setEstadoActivo(amd.getEstadoActivo());
        if (amd.getArticuloInsumo() != null) {
            dto.setArticuloInsumo(convertArticuloToSimpleDto(amd.getArticuloInsumo()));
        }
        return dto;
    }

    // Este es el mapper polimórfico principal
    public ArticuloBaseResponseDTO convertArticuloToResponseDto(Articulo articulo) {
        if (articulo == null) return null;
        ArticuloBaseResponseDTO baseDto;

        if (articulo instanceof ArticuloInsumo) {
            ArticuloInsumo insumo = (ArticuloInsumo) articulo;
            ArticuloInsumoResponseDTO dto = new ArticuloInsumoResponseDTO();
            dto.setPrecioCompra(insumo.getPrecioCompra());
            dto.setStockActual(insumo.getStockActual());
            dto.setstockMinimo(insumo.getstockMinimo());
            dto.setEsParaElaborar(insumo.getEsParaElaborar());
            baseDto = dto;
        } else if (articulo instanceof ArticuloManufacturado) {
            ArticuloManufacturado manufacturado = (ArticuloManufacturado) articulo;
            ArticuloManufacturadoResponseDTO dto = new ArticuloManufacturadoResponseDTO();
            dto.setDescripcion(manufacturado.getDescripcion());
            dto.setTiempoEstimadoMinutos(manufacturado.getTiempoEstimadoMinutos());
            dto.setPreparacion(manufacturado.getPreparacion());
            if (manufacturado.getManufacturadoDetalles() != null) {
                dto.setManufacturadoDetalles(
                        manufacturado.getManufacturadoDetalles().stream()
                                .map(this::convertAmdToDto)
                                .collect(Collectors.toList())
                );
            }
            baseDto = dto;
        } else {
            // Si tienes Articulos que no son Insumo ni Manufacturado
            // necesitarías un ArticuloResponseDTO concreto que no sea abstracto.
            // Por ahora, si esto ocurre, es un tipo no manejado por los DTOs específicos.
            // Para que ArticuloBaseResponseDTO no sea abstracto, esta rama debería instanciarlo.
            // Pero como lo hicimos abstracto, esta situación es un error de modelado de DTO.
            // Si Articulo SIEMPRE es Insumo o Manufacturado, esta rama no se alcanza.
            // Si puede ser un Articulo "base", ArticuloBaseResponseDTO no debería ser abstract.
            // Asumamos por ahora que ArticuloBaseResponseDTO no es abstract y este es el fallback.
            // Si es abstract, esta línea daría error:
            // baseDto = new ArticuloBaseResponseDTO();
            // Alternativa si ArticuloBaseResponseDTO es abstract:
            throw new IllegalStateException("Tipo de Articulo no mapeado a DTO específico: " + articulo.getClass().getName());
        }

        // Poblar campos comunes
        baseDto.setId(articulo.getId());
        baseDto.setDenominacion(articulo.getDenominacion());
        baseDto.setPrecioVenta(articulo.getPrecioVenta());
        baseDto.setEstadoActivo(articulo.getEstadoActivo());
        // baseDto.setFechaBaja(articulo.getFechaBaja()); // Si lo tienes

        if (articulo.getUnidadMedida() != null) {
            baseDto.setUnidadMedida(convertUnidadMedidaToDto(articulo.getUnidadMedida()));
        }
        if (articulo.getCategoria() != null) {
            baseDto.setCategoria(convertCategoriaToDto(articulo.getCategoria()));
        }
        if (articulo.getImagenes() != null) {
            baseDto.setImagenes(articulo.getImagenes().stream()
                    .map(this::convertImagenToDto)
                    .collect(Collectors.toList()));
        }
        return baseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloBaseResponseDTO> getAllArticulos() {
        return articuloRepository.findAll().stream()
                .map(this::convertArticuloToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloBaseResponseDTO getArticuloById(Integer id) throws Exception {
        Articulo articulo = articuloRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + id));
        return convertArticuloToResponseDto(articulo);
    }

    @Override
    @Transactional
    public Articulo createArticulo(@Valid Articulo articulo) throws Exception {
        // Esta implementación es para un Articulo "base" si existiera.
        // Normalmente, crearías instancias de ArticuloInsumo o ArticuloManufacturado
        // a través de sus propios servicios o DTOs que indiquen el tipo.
        if (articulo.getCategoria() == null || articulo.getCategoria().getId() == null) {
            throw new Exception("La categoría es obligatoria para el artículo.");
        }
        Categoria cat = categoriaRepository.findById(articulo.getCategoria().getId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + articulo.getCategoria().getId()));
        articulo.setCategoria(cat);

        if (articulo.getUnidadMedida() == null || articulo.getUnidadMedida().getId() == null) {
            throw new Exception("La unidad de medida es obligatoria para el artículo.");
        }
        UnidadMedida um = unidadMedidaRepository.findById(articulo.getUnidadMedida().getId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + articulo.getUnidadMedida().getId()));
        articulo.setUnidadMedida(um);

        return articuloRepository.save(articulo);
    }

    @Override
    @Transactional
    public Articulo updateArticulo(Integer id, @Valid Articulo articuloDetalles) throws Exception {
        Articulo articuloExistente = articuloRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo no encontrado con ID: " + id + " para actualizar."));

        // Solo actualiza campos comunes de Articulo.
        // Las subclases deben manejar sus propios campos en sus servicios.
        articuloExistente.setDenominacion(articuloDetalles.getDenominacion());
        articuloExistente.setPrecioVenta(articuloDetalles.getPrecioVenta());
        articuloExistente.setEstadoActivo(articuloDetalles.getEstadoActivo());

        if (articuloDetalles.getCategoria() != null && articuloDetalles.getCategoria().getId() != null) {
            Categoria cat = categoriaRepository.findById(articuloDetalles.getCategoria().getId()).orElseThrow(() -> new Exception("Categoría no encontrada"));
            articuloExistente.setCategoria(cat);
        } else if (articuloDetalles.getCategoria() == null && articuloExistente.getCategoria() == null ) {
            throw new Exception("La categoría es obligatoria para el artículo.");
        }


        if (articuloDetalles.getUnidadMedida() != null && articuloDetalles.getUnidadMedida().getId() != null) {
            UnidadMedida um = unidadMedidaRepository.findById(articuloDetalles.getUnidadMedida().getId()).orElseThrow(() -> new Exception("Unidad de Medida no encontrada"));
            articuloExistente.setUnidadMedida(um);
        } else if (articuloDetalles.getUnidadMedida() == null && articuloExistente.getUnidadMedida() == null) {
            throw new Exception("La unidad de medida es obligatoria para el artículo.");
        }
        // Las imágenes se manejan por separado o en servicios de subclase.
        // No se actualizan las colecciones aquí directamente para un Articulo base.

        return articuloRepository.save(articuloExistente);
    }

    @Override
    @Transactional
    public void deleteArticulo(Integer id) throws Exception {
        if (!articuloRepository.existsById(id)) {
            throw new Exception("Artículo no encontrado con ID: " + id + " para eliminar.");
        }
        articuloRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloBaseResponseDTO findByDenominacion(String denominacion) throws Exception {
        Articulo articulo = articuloRepository.findByDenominacion(denominacion)
                .orElseThrow(() -> new Exception("Artículo no encontrado con denominación: " + denominacion));
        return convertArticuloToResponseDto(articulo);
    }
}