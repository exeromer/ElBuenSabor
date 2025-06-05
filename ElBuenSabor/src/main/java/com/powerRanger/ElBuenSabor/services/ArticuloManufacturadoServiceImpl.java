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
    public List<ArticuloManufacturadoResponseDTO> getAllArticuloManufacturados(String searchTerm, Boolean estadoActivo) {
        List<ArticuloManufacturado> manufacturados;
        String trimmedSearchTerm = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        if (trimmedSearchTerm != null) {
            manufacturados = manufacturadoRepository.searchByDenominacionWithOptionalStatus(trimmedSearchTerm, estadoActivo);
        } else {
            manufacturados = manufacturadoRepository.findAllWithOptionalStatus(estadoActivo);
        }

        return manufacturados.stream().map(am -> {
            ArticuloManufacturadoResponseDTO dto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(am);
            // Necesitamos la entidad completa con sus detalles para calcular las unidades disponibles
            // Si el 'am' de la lista no tiene los detalles cargados (LAZY), hay que recargarlo.
            // Esto puede ser ineficiente (N+1). Idealmente, la query del repositorio haría un JOIN FETCH.
            // Por ahora, para asegurar que funciona:
            ArticuloManufacturado amConDetalles = manufacturadoRepository.findById(am.getId()).orElse(am); // Recarga si es necesario
            dto.setUnidadesDisponiblesCalculadas(calcularUnidadesDisponibles(amConDetalles));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloManufacturadoResponseDTO getArticuloManufacturadoById(Integer id) throws Exception {
        System.out.println("DEBUG SERVICE: getArticuloManufacturadoById llamado con ID: " + id);
        ArticuloManufacturado manufacturado = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Manufacturado no encontrado con ID: " + id));

        ArticuloManufacturadoResponseDTO dto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(manufacturado);
        dto.setUnidadesDisponiblesCalculadas(calcularUnidadesDisponibles(manufacturado));
        System.out.println("DEBUG SERVICE: Manufacturado ID: " + dto.getId() + " (" + dto.getDenominacion() + ") - Unidades Disponibles Asignadas al DTO: " + dto.getUnidadesDisponiblesCalculadas());
        return dto;
    }
    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado am = new ArticuloManufacturado();
        am.setImagenes(new ArrayList<>());
        am.setManufacturadoDetalles(new ArrayList<>());

        mapRequestDtoToEntity(dto, am); // Esto mapea del RequestDTO a la Entidad
        ArticuloManufacturado amGuardado = manufacturadoRepository.save(am);

        // Convertir la entidad guardada a ResponseDTO
        ArticuloManufacturadoResponseDTO responseDto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amGuardado);

        // Calcular y asignar las unidades disponibles AL RESPONSE DTO
        if (responseDto != null && amGuardado.getEstadoActivo()) { // Verifica que sea ResponseDTO y esté activo
            responseDto.setUnidadesDisponiblesCalculadas(calcularUnidadesDisponibles(amGuardado));
        } else if (responseDto != null) {
            responseDto.setUnidadesDisponiblesCalculadas(0);
        }

        return responseDto;
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        ArticuloManufacturado amExistente = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Manufacturado no encontrado con ID: " + id));

        mapRequestDtoToEntity(dto, amExistente); // Mapea del RequestDTO a la Entidad
        ArticuloManufacturado amActualizado = manufacturadoRepository.save(amExistente);

        // Convertir la entidad actualizada a ResponseDTO
        ArticuloManufacturadoResponseDTO responseDto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amActualizado);

        // Calcular y asignar las unidades disponibles AL RESPONSE DTO
        if (responseDto != null && amActualizado.getEstadoActivo()) { // Verifica que sea ResponseDTO y esté activo
            responseDto.setUnidadesDisponiblesCalculadas(calcularUnidadesDisponibles(amActualizado));
        } else if (responseDto != null) {
            responseDto.setUnidadesDisponiblesCalculadas(0);
        }

        return responseDto;
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

    private Integer calcularUnidadesDisponibles(ArticuloManufacturado manufacturado) {
        System.out.println("DEBUG CALC_UNID: Calculando unidades para manufacturado ID: " + manufacturado.getId() + " - " + manufacturado.getDenominacion());
        if (manufacturado.getManufacturadoDetalles() == null || manufacturado.getManufacturadoDetalles().isEmpty()) {
            // Si no tiene receta, no se puede fabricar (o se asume que no necesita insumos contables aquí)
            // Podrías decidir devolver un número muy grande o 0 según la lógica de negocio.
            // Devolver 0 si se espera que tenga receta.
            System.out.println("DEBUG CALC_UNID: Manufacturado ID: " + manufacturado.getId() + " no tiene detalles de receta. Unidades disponibles: 0");
            return 0;
        }

        int unidadesDisponiblesMinimo = Integer.MAX_VALUE; // Empezamos con un valor alto

        for (ArticuloManufacturadoDetalle detalleReceta : manufacturado.getManufacturadoDetalles()) {
            ArticuloInsumo insumoComponente = detalleReceta.getArticuloInsumo();
            if (insumoComponente == null) { // Control por si el dato de la receta es inconsistente
                System.err.println("WARN: Detalle de receta para " + manufacturado.getDenominacion() + " tiene un insumo nulo.");
                return 0; // Si un insumo es nulo en la receta, no se puede fabricar
            }
            System.out.println("DEBUG CALC_UNID:   Procesando componente de receta: " + insumoComponente.getDenominacion() + " (ID: " + insumoComponente.getId() + ")");

            // Es crucial obtener el stock actual FRESCO del insumo desde su repositorio
            ArticuloInsumo insumoConStockActual = articuloInsumoRepository.findById(insumoComponente.getId())
                    .orElse(null); // Manejar si el insumo de la receta ya no existe
            if (insumoConStockActual == null || insumoConStockActual.getStockActual() == null) {
                System.err.println("WARN: Insumo " + insumoComponente.getDenominacion() + " de receta no encontrado o sin stock actual definido.");
                return 0; // Si un insumo no existe o no tiene stock definido, no se puede fabricar
            }

            double stockActual = insumoConStockActual.getStockActual();
            double cantidadNecesariaPorUnidad = detalleReceta.getCantidad();
            System.out.println("DEBUG CALC_UNID:     Insumo: " + insumoConStockActual.getDenominacion() + " - Stock Actual LEÍDO: " + stockActual + ", Cantidad Necesaria por Receta: " + cantidadNecesariaPorUnidad);
            if (cantidadNecesariaPorUnidad <= 0) {
                System.err.println("WARN CALC_UNID:    Insumo " + insumoConStockActual.getDenominacion() + " (ID: " + insumoConStockActual.getId() + ") tiene stockActual NULO. Unidades disponibles: 0");
                continue; // Este insumo no se necesita o la receta tiene un error, no limita la producción.
            }

            if (stockActual < cantidadNecesariaPorUnidad) {
                System.out.println("DEBUG CALC_UNID:     STOCK INSUFICIENTE para " + insumoConStockActual.getDenominacion() + ". Se necesitan " + cantidadNecesariaPorUnidad + ", disponibles " + stockActual + ". Unidades disponibles: 0");
                return 0; // No hay suficiente stock de este insumo para hacer ni una unidad.
            }

            int unidadesConEsteInsumo = (int) Math.floor(stockActual / cantidadNecesariaPorUnidad);
            System.out.println("DEBUG CALC_UNID:     Unidades que se pueden hacer con " + insumoConStockActual.getDenominacion() + ": " + unidadesConEsteInsumo);
            if (unidadesConEsteInsumo < unidadesDisponiblesMinimo) {
                unidadesDisponiblesMinimo = unidadesConEsteInsumo;
            }
        }
        Integer resultadoFinal = (unidadesDisponiblesMinimo == Integer.MAX_VALUE) ? 0 : unidadesDisponiblesMinimo;
        System.out.println("DEBUG CALC_UNID: Final para manufacturado ID: " + manufacturado.getId() + " - Unidades Disponibles Calculadas: " + resultadoFinal);
        return resultadoFinal;
    }

}