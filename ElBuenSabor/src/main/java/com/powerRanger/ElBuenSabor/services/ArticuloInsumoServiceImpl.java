package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import com.powerRanger.ElBuenSabor.repository.ArticuloInsumoRepository;
import com.powerRanger.ElBuenSabor.repository.CategoriaRepository;
import com.powerRanger.ElBuenSabor.repository.UnidadMedidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ArticuloInsumoServiceImpl extends BaseServiceImpl<ArticuloInsumo, ArticuloInsumoRepository> implements ArticuloInsumoService {

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private Mappers mappers;

    public ArticuloInsumoServiceImpl(ArticuloInsumoRepository articuloInsumoRepository) {
        super(articuloInsumoRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoResponseDTO> findAllInsumos(String searchTerm, Boolean estadoActivo) {
        List<ArticuloInsumo> insumos;
        String trimmedSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        if (trimmedSearchTerm != null) {
            insumos = baseRepository.searchByDenominacionWithOptionalStatus(trimmedSearchTerm, estadoActivo);
        } else {
            insumos = baseRepository.findAllWithOptionalStatus(estadoActivo);
        }
        return insumos.stream()
                .map(insumo -> (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloInsumoResponseDTO findInsumoById(Integer id) throws Exception {
        ArticuloInsumo insumo = super.findById(id);
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(insumo);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO createArticuloInsumo(@Valid ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumo insumo = new ArticuloInsumo();
        insumo.setImagenes(new ArrayList<>());
        mapDtoToEntity(dto, insumo);
        ArticuloInsumo guardado = super.save(insumo);
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(guardado);
    }

    @Override
    @Transactional
    public ArticuloInsumoResponseDTO updateArticuloInsumo(Integer id, @Valid ArticuloInsumoRequestDTO dto) throws Exception {
        ArticuloInsumo insumoExistente = super.findById(id);
        mapDtoToEntity(dto, insumoExistente);
        ArticuloInsumo actualizado = super.update(id, insumoExistente);
        return (ArticuloInsumoResponseDTO) mappers.convertArticuloToResponseDto(actualizado);
    }

    private void mapDtoToEntity(ArticuloInsumoRequestDTO dto, ArticuloInsumo insumo) throws Exception {
        insumo.setDenominacion(dto.getDenominacion());
        insumo.setPrecioVenta(dto.getPrecioVenta());
        insumo.setEstadoActivo(dto.getEstadoActivo());

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + dto.getCategoriaId()));
        insumo.setCategoria(categoria);

        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + dto.getUnidadMedidaId()));
        insumo.setUnidadMedida(unidadMedida);

        insumo.setPrecioCompra(dto.getPrecioCompra());
        insumo.setStockActual(dto.getStockActual());
        insumo.setstockMinimo(dto.getstockMinimo());
        insumo.setEsParaElaborar(dto.getEsParaElaborar());
    }

}