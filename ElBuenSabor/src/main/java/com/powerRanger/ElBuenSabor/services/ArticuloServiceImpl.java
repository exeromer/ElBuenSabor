package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.*;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import com.powerRanger.ElBuenSabor.repository.ArticuloRepository;
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
public class ArticuloServiceImpl extends BaseServiceImpl<Articulo, ArticuloRepository> implements ArticuloService {

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private Mappers mappers;

    public ArticuloServiceImpl(ArticuloRepository articuloRepository) {
        super(articuloRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloBaseResponseDTO> findAllArticulos() {
        try {
            return super.findAll().stream()
                    .map(mappers::convertArticuloToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar articulos: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloBaseResponseDTO findArticuloById(Integer id) throws Exception {
        return mappers.convertArticuloToResponseDto(super.findById(id));
    }

    @Override
    @Transactional
    public Articulo createArticulo(@Valid Articulo articulo) throws Exception {
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

        return super.save(articulo);
    }

    @Override
    @Transactional
    public Articulo updateArticulo(Integer id, @Valid Articulo articuloDetalles) throws Exception {
        Articulo articuloExistente = super.findById(id);

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

        return super.update(id, articuloExistente);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloBaseResponseDTO findByDenominacion(String denominacion) throws Exception {
        Articulo articulo = baseRepository.findByDenominacion(denominacion)
                .orElseThrow(() -> new Exception("Artículo no encontrado con denominación: " + denominacion));
        return mappers.convertArticuloToResponseDto(articulo);
    }
}