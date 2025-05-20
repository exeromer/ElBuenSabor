package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoDetalleDTO; // Importar DTO
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid; // Lo mantenemos si el controlador pasa el DTO con @Valid
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class ArticuloManufacturadoServiceImpl implements ArticuloManufacturadoService {

    @Autowired private ArticuloManufacturadoRepository manufacturadoRepository;
    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturado> getAllArticuloManufacturados() {
        return manufacturadoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloManufacturado getArticuloManufacturadoById(Integer id) throws Exception {
        return manufacturadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Manufacturado no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public ArticuloManufacturado createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado am = new ArticuloManufacturado();
        am.setDenominacion(dto.getDenominacion());
        am.setPrecioVenta(dto.getPrecioVenta());
        am.setEstadoActivo(dto.getEstadoActivo());

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + dto.getCategoriaId()));
        am.setCategoria(categoria);

        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + dto.getUnidadMedidaId()));
        am.setUnidadMedida(unidadMedida);

        // Mapear campos específicos de ArticuloManufacturado desde el DTO
        am.setDescripcion(dto.getDescripcion());
        am.setTiempoEstimadoMinutos(dto.getTiempoEstimadoMinutos());
        am.setPreparacion(dto.getPreparacion());

        // Procesar los detalles
        if (dto.getManufacturadoDetalles() != null) {
            for (ArticuloManufacturadoDetalleDTO detalleDto : dto.getManufacturadoDetalles()) {
                ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDto.getArticuloInsumoId())
                        .orElseThrow(() -> new Exception("ArticuloInsumo no encontrado con ID: " + detalleDto.getArticuloInsumoId()));

                ArticuloManufacturadoDetalle nuevoDetalle = new ArticuloManufacturadoDetalle();
                nuevoDetalle.setArticuloInsumo(insumo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad());
                nuevoDetalle.setEstadoActivo(detalleDto.getEstadoActivo() != null ? detalleDto.getEstadoActivo() : true);
                am.addManufacturadoDetalle(nuevoDetalle); // El helper establece la relación bidireccional
            }
        }
        return manufacturadoRepository.save(am);
    }

    @Override
    @Transactional
    public ArticuloManufacturado updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado amExistente = getArticuloManufacturadoById(id);

        // Mapear campos de Articulo base
        amExistente.setDenominacion(dto.getDenominacion());
        amExistente.setPrecioVenta(dto.getPrecioVenta());
        amExistente.setEstadoActivo(dto.getEstadoActivo());

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + dto.getCategoriaId()));
            amExistente.setCategoria(categoria);
        } else {
            throw new Exception("El ID de la categoría es obligatorio.");
        }


        if (dto.getUnidadMedidaId() != null) {
            UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                    .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + dto.getUnidadMedidaId()));
            amExistente.setUnidadMedida(unidadMedida);
        } else {
            throw new Exception("El ID de la unidad de medida es obligatorio.");
        }

        // Mapear campos específicos de ArticuloManufacturado
        amExistente.setDescripcion(dto.getDescripcion());
        amExistente.setTiempoEstimadoMinutos(dto.getTiempoEstimadoMinutos());
        amExistente.setPreparacion(dto.getPreparacion());

        // Estrategia para actualizar detalles: borrar los antiguos y añadir los nuevos.
        amExistente.getManufacturadoDetalles().clear(); // orphanRemoval=true se encarga de borrar de BD

        if (dto.getManufacturadoDetalles() != null) {
            for (ArticuloManufacturadoDetalleDTO detalleDto : dto.getManufacturadoDetalles()) {
                ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDto.getArticuloInsumoId())
                        .orElseThrow(() -> new Exception("ArticuloInsumo no encontrado con ID: " + detalleDto.getArticuloInsumoId()));

                ArticuloManufacturadoDetalle nuevoDetalle = new ArticuloManufacturadoDetalle();
                nuevoDetalle.setArticuloInsumo(insumo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad());
                nuevoDetalle.setEstadoActivo(detalleDto.getEstadoActivo() != null ? detalleDto.getEstadoActivo() : true);
                amExistente.addManufacturadoDetalle(nuevoDetalle);
            }
        }
        return manufacturadoRepository.save(amExistente);
    }

    @Override
    @Transactional
    public void deleteArticuloManufacturado(Integer id) throws Exception {
        if (!manufacturadoRepository.existsById(id)) {
            throw new Exception("Artículo Manufacturado no encontrado con ID: " + id + " para eliminar.");
        }
        manufacturadoRepository.deleteById(id);
    }
}