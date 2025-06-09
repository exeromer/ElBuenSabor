package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoDetalleDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO;
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import com.powerRanger.ElBuenSabor.repository.*;
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
public class ArticuloManufacturadoServiceImpl extends BaseServiceImpl<ArticuloManufacturado, ArticuloManufacturadoRepository> implements ArticuloManufacturadoService {

    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private Mappers mappers;

    public ArticuloManufacturadoServiceImpl(ArticuloManufacturadoRepository manufacturadoRepository) {
        super(manufacturadoRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> findAllManufacturados(String searchTerm, Boolean estadoActivo) {
        List<ArticuloManufacturado> manufacturados;
        String trimmedSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        if (trimmedSearchTerm != null) {
            manufacturados = baseRepository.searchByDenominacionWithOptionalStatus(trimmedSearchTerm, estadoActivo);
        } else {
            manufacturados = baseRepository.findAllWithOptionalStatus(estadoActivo);
        }

        return manufacturados.stream().map(am -> {
            ArticuloManufacturadoResponseDTO dto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(am);
            ArticuloManufacturado amConDetalles = baseRepository.findById(am.getId()).orElse(am);
            dto.setUnidadesDisponiblesCalculadas(calcularUnidadesDisponibles(amConDetalles));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloManufacturadoResponseDTO findManufacturadoById(Integer id) throws Exception {
        ArticuloManufacturado manufacturado = super.findById(id);
        ArticuloManufacturadoResponseDTO dto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(manufacturado);
        dto.setUnidadesDisponiblesCalculadas(calcularUnidadesDisponibles(manufacturado));
        return dto;
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado am = new ArticuloManufacturado();
        am.setImagenes(new ArrayList<>());
        am.setManufacturadoDetalles(new ArrayList<>());
        mapRequestDtoToEntity(dto, am);
        ArticuloManufacturado amGuardado = super.save(am);
        return (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amGuardado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado amExistente = super.findById(id);
        mapRequestDtoToEntity(dto, amExistente);
        ArticuloManufacturado amActualizado = super.update(id, amExistente);
        return (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amActualizado);
    }

    private void mapRequestDtoToEntity(ArticuloManufacturadoRequestDTO dto, ArticuloManufacturado am) throws Exception {
        am.setDenominacion(dto.getDenominacion());
        am.setPrecioVenta(dto.getPrecioVenta());
        am.setEstadoActivo(dto.getEstadoActivo());

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + dto.getCategoriaId()));
        am.setCategoria(categoria);

        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + dto.getUnidadMedidaId()));
        am.setUnidadMedida(unidadMedida);

        am.setDescripcion(dto.getDescripcion());
        am.setTiempoEstimadoMinutos(dto.getTiempoEstimadoMinutos());
        am.setPreparacion(dto.getPreparacion());

        if (am.getManufacturadoDetalles() == null) am.setManufacturadoDetalles(new ArrayList<>());
        am.getManufacturadoDetalles().clear();

        if (dto.getManufacturadoDetalles() != null && !dto.getManufacturadoDetalles().isEmpty()) {
            for (ArticuloManufacturadoDetalleDTO detalleDto : dto.getManufacturadoDetalles()) {
                ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDto.getArticuloInsumoId())
                        .orElseThrow(() -> new Exception("ArticuloInsumo no encontrado con ID: " + detalleDto.getArticuloInsumoId()));
                ArticuloManufacturadoDetalle nuevoDetalle = new ArticuloManufacturadoDetalle();
                nuevoDetalle.setArticuloInsumo(insumo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad());
                nuevoDetalle.setEstadoActivo(detalleDto.getEstadoActivo() != null ? detalleDto.getEstadoActivo() : true);
                am.addManufacturadoDetalle(nuevoDetalle);
            }
        }
    }

    private Integer calcularUnidadesDisponibles(ArticuloManufacturado manufacturado) {
        if (manufacturado.getManufacturadoDetalles() == null || manufacturado.getManufacturadoDetalles().isEmpty()) {
            return 0;
        }
        int unidadesDisponiblesMinimo = Integer.MAX_VALUE;
        for (ArticuloManufacturadoDetalle detalleReceta : manufacturado.getManufacturadoDetalles()) {
            ArticuloInsumo insumoComponente = detalleReceta.getArticuloInsumo();
            if (insumoComponente == null) return 0;

            ArticuloInsumo insumoConStockActual = articuloInsumoRepository.findById(insumoComponente.getId()).orElse(null);
            if (insumoConStockActual == null || insumoConStockActual.getStockActual() == null) return 0;

            double stockActual = insumoConStockActual.getStockActual();
            double cantidadNecesariaPorUnidad = detalleReceta.getCantidad();
            if (cantidadNecesariaPorUnidad <= 0) continue;
            if (stockActual < cantidadNecesariaPorUnidad) return 0;

            int unidadesConEsteInsumo = (int) Math.floor(stockActual / cantidadNecesariaPorUnidad);
            if (unidadesConEsteInsumo < unidadesDisponiblesMinimo) {
                unidadesDisponiblesMinimo = unidadesConEsteInsumo;
            }
        }
        return (unidadesDisponiblesMinimo == Integer.MAX_VALUE) ? 0 : unidadesDisponiblesMinimo;
    }
}