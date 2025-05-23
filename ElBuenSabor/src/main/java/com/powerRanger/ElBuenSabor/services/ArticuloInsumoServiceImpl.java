package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.mappers.Mappers; // Asumiendo que Mappers está en este paquete
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ArticuloInsumoServiceImpl implements ArticuloInsumoService {

    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private Mappers mappers; // Usar la clase Mappers

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> getAllArticuloInsumo() {
        return articuloInsumoRepository.findAll().stream()
                .map(insumo -> (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloInsumoResponseDTO getArticuloInsumoById(Integer id) throws Exception {
        ArticuloInsumo insumo = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumo articuloInsumo) throws Exception {
        // Validar y asignar Categoria
        if (articuloInsumo.getCategoria() == null || articuloInsumo.getCategoria().getId() == null) {
            throw new Exception("La categoría es obligatoria para el artículo insumo.");
        }
        Categoria categoria = categoriaRepository.findById(articuloInsumo.getCategoria().getId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + articuloInsumo.getCategoria().getId()));
        articuloInsumo.setCategoria(categoria);

        // Validar y asignar UnidadMedida
        if (articuloInsumo.getUnidadMedida() == null || articuloInsumo.getUnidadMedida().getId() == null) {
            throw new Exception("La unidad de medida es obligatoria para el artículo insumo.");
        }
        UnidadMedida unidadMedida = unidadMedidaRepository.findById(articuloInsumo.getUnidadMedida().getId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + articuloInsumo.getUnidadMedida().getId()));
        articuloInsumo.setUnidadMedida(unidadMedida);

        ArticuloInsumo guardado = articuloInsumoRepository.save(articuloInsumo);
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(guardado);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumo articuloInsumoDetails) throws Exception {
        ArticuloInsumo insumoExistente = articuloInsumoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Insumo no encontrado con ID: " + id));

        // Actualizar campos de Articulo (superclase)
        insumoExistente.setDenominacion(articuloInsumoDetails.getDenominacion());
        insumoExistente.setPrecioVenta(articuloInsumoDetails.getPrecioVenta());
        insumoExistente.setEstadoActivo(articuloInsumoDetails.getEstadoActivo());

        if (articuloInsumoDetails.getCategoria() != null && articuloInsumoDetails.getCategoria().getId() != null) {
            Categoria cat = categoriaRepository.findById(articuloInsumoDetails.getCategoria().getId())
                    .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + articuloInsumoDetails.getCategoria().getId()));
            insumoExistente.setCategoria(cat);
        } else if (articuloInsumoDetails.getCategoria() == null) {
            throw new Exception("La categoría es obligatoria para el artículo insumo.");
        }

        if (articuloInsumoDetails.getUnidadMedida() != null && articuloInsumoDetails.getUnidadMedida().getId() != null) {
            UnidadMedida um = unidadMedidaRepository.findById(articuloInsumoDetails.getUnidadMedida().getId())
                    .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + articuloInsumoDetails.getUnidadMedida().getId()));
            insumoExistente.setUnidadMedida(um);
        } else if (articuloInsumoDetails.getUnidadMedida() == null) {
            throw new Exception("La unidad de medida es obligatoria para el artículo insumo.");
        }

        // Actualizar campos específicos de ArticuloInsumo
        insumoExistente.setPrecioCompra(articuloInsumoDetails.getPrecioCompra());
        insumoExistente.setStockActual(articuloInsumoDetails.getStockActual());
        insumoExistente.setStockMaximo(articuloInsumoDetails.getStockMaximo());
        insumoExistente.setEsParaElaborar(articuloInsumoDetails.getEsParaElaborar());

        ArticuloInsumo actualizado = articuloInsumoRepository.save(insumoExistente);
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(actualizado);
    }

    @Override
    @Transactional
    public void deleteArticuloInsumo(Integer id) throws Exception {
        if (!articuloInsumoRepository.existsById(id)) {
            throw new Exception("Artículo Insumo no encontrado con ID: " + id + " para eliminar.");
        }
        // Aquí podrías añadir lógica para verificar si el insumo está en uso en ArticuloManufacturadoDetalle
        articuloInsumoRepository.deleteById(id);
    }
}