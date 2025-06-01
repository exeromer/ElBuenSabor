package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoDetalleDTO; // DTO de Request para detalle
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO; // DTO de Response
import com.powerRanger.ElBuenSabor.entities.*;
import com.powerRanger.ElBuenSabor.mappers.Mappers; // Asumiendo que Mappers está en este paquete
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
public class ArticuloManufacturadoServiceImpl implements ArticuloManufacturadoService {

    @Autowired private ArticuloManufacturadoRepository manufacturadoRepository;
    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private Mappers mappers; // Usar la clase Mappers

    private void mapRequestDtoToEntity(ArticuloManufacturadoRequestDTO dto, ArticuloManufacturado am) throws Exception {
        // Mapear campos de Articulo base
        am.setDenominacion(dto.getDenominacion());
        am.setPrecioVenta(dto.getPrecioVenta());
        am.setEstadoActivo(dto.getEstadoActivo());

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new Exception("Categoría no encontrada con ID: " + dto.getCategoriaId()));
        am.setCategoria(categoria);

        UnidadMedida unidadMedida = unidadMedidaRepository.findById(dto.getUnidadMedidaId())
                .orElseThrow(() -> new Exception("Unidad de medida no encontrada con ID: " + dto.getUnidadMedidaId()));
        am.setUnidadMedida(unidadMedida);

        // Mapear campos específicos de ArticuloManufacturado
        am.setDescripcion(dto.getDescripcion());
        am.setTiempoEstimadoMinutos(dto.getTiempoEstimadoMinutos());
        am.setPreparacion(dto.getPreparacion());

        // Manejar detalles
        // Estrategia: borrar los detalles existentes y añadir los nuevos.
        // orphanRemoval=true en ArticuloManufacturado.manufacturadoDetalles se encarga de borrar de BD.
        if (am.getManufacturadoDetalles() == null) am.setManufacturadoDetalles(new ArrayList<>());
        am.getManufacturadoDetalles().clear(); // Limpiar para asegurar que orphanRemoval actúe si es una actualización

        if (dto.getManufacturadoDetalles() != null && !dto.getManufacturadoDetalles().isEmpty()) {
            for (ArticuloManufacturadoDetalleDTO detalleDto : dto.getManufacturadoDetalles()) {
                ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDto.getArticuloInsumoId())
                        .orElseThrow(() -> new Exception("ArticuloInsumo no encontrado con ID: " + detalleDto.getArticuloInsumoId()));

                ArticuloManufacturadoDetalle nuevoDetalle = new ArticuloManufacturadoDetalle();
                nuevoDetalle.setArticuloInsumo(insumo);
                nuevoDetalle.setCantidad(detalleDto.getCantidad());
                nuevoDetalle.setEstadoActivo(detalleDto.getEstadoActivo() != null ? detalleDto.getEstadoActivo() : true);
                am.addManufacturadoDetalle(nuevoDetalle); // Usa el helper para añadir y establecer la relación bidireccional
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoResponseDTO> getAllArticuloManufacturados(String searchTerm) { // Modificado
        List<ArticuloManufacturado> manufacturados;
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            manufacturados = manufacturadoRepository.searchByDenominacionActivos(searchTerm.trim());
            System.out.println("DEBUG: Buscando ArticuloManufacturado con término: '" + searchTerm.trim() + "', Encontrados: " + manufacturados.size());
        } else {
            manufacturados = manufacturadoRepository.findByEstadoActivoTrue();
            System.out.println("DEBUG: Obteniendo todos los ArticuloManufacturado activos, Encontrados: " + manufacturados.size());
        }
        return manufacturados.stream()
                .map(am -> (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(am)) // Asegúrate que el mapper devuelva el tipo correcto
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public ArticuloManufacturadoResponseDTO getArticuloManufacturadoById(Integer id) throws Exception {
        ArticuloManufacturado am = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Manufacturado no encontrado con ID: " + id));
        return (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(am);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado am = new ArticuloManufacturado();
        // Asegurar que las listas estén inicializadas antes de mapDtoToEntity
        am.setImagenes(new ArrayList<>()); // Heredado de Articulo
        am.setManufacturadoDetalles(new ArrayList<>());

        mapRequestDtoToEntity(dto, am);
        ArticuloManufacturado amGuardado = manufacturadoRepository.save(am);
        return (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amGuardado);
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado amExistente = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Manufacturado no encontrado con ID: " + id));

        mapRequestDtoToEntity(dto, amExistente);
        ArticuloManufacturado amActualizado = manufacturadoRepository.save(amExistente);
        return (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amActualizado);
    }

    @Override
    @Transactional
    public void deleteArticuloManufacturado(Integer id) throws Exception {
        if (!manufacturadoRepository.existsById(id)) {
            throw new Exception("Artículo Manufacturado no encontrado con ID: " + id + " para eliminar.");
        }
        // CascadeType.ALL y orphanRemoval=true en manufacturadoDetalles se encargarán de borrar detalles.
        manufacturadoRepository.deleteById(id);
    }
}