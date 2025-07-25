package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRequestDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoDetalleDTO; // DTO de Request para detalle
import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoResponseDTO; // DTO de Response
import com.powerRanger.ElBuenSabor.dtos.StockInsumoSucursalResponseDTO; // Importar StockInsumoSucursalResponseDTO
import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturado;
import com.powerRanger.ElBuenSabor.entities.ArticuloManufacturadoDetalle;
import com.powerRanger.ElBuenSabor.entities.ArticuloInsumo;
import com.powerRanger.ElBuenSabor.entities.Categoria;
import com.powerRanger.ElBuenSabor.entities.UnidadMedida;
import com.powerRanger.ElBuenSabor.mappers.Mappers;
import com.powerRanger.ElBuenSabor.repository.ArticuloManufacturadoRepository;
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
public class ArticuloManufacturadoServiceImpl implements ArticuloManufacturadoService {

    @Autowired private ArticuloManufacturadoRepository manufacturadoRepository;
    @Autowired private ArticuloInsumoRepository articuloInsumoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UnidadMedidaRepository unidadMedidaRepository;
    @Autowired private Mappers mappers;
    @Autowired private StockInsumoSucursalService stockInsumoSucursalService; // Inyectar el nuevo servicio de stock por sucursal

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

    private double calcularCostoProducto(ArticuloManufacturadoRequestDTO dto) throws Exception {
        double costoTotal = 0.0;
        if (dto.getManufacturadoDetalles() == null || dto.getManufacturadoDetalles().isEmpty()) {
            throw new Exception("Un producto manufacturado debe tener al menos un ingrediente en su receta.");
        }

        for (ArticuloManufacturadoDetalleDTO detalleDto : dto.getManufacturadoDetalles()) {
            ArticuloInsumo insumo = articuloInsumoRepository.findById(detalleDto.getArticuloInsumoId())
                    .orElseThrow(() -> new Exception("Insumo con ID " + detalleDto.getArticuloInsumoId() + " no encontrado."));

            if (insumo.getPrecioCompra() == null) {
                throw new Exception("El insumo '" + insumo.getDenominacion() + "' no tiene un precio de compra definido y no se puede calcular el costo.");
            }

            costoTotal += detalleDto.getCantidad() * insumo.getPrecioCompra();
        }
        return costoTotal;
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
            // Aquí no tenemos un sucursalId para calcular unidades disponibles.
            // Se asume que para una lista general, las unidades disponibles se calcularán en el frontend
            // cuando se seleccione una sucursal, o se mostrará "N/A" o "0".
            dto.setUnidadesDisponiblesCalculadas(0); //
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
        // Similar al getAll, no tenemos un sucursalId aquí para calcular unidades.
        // Si este método es llamado para una vista de detalle donde ya se conoce la sucursal,
        // la firma de este método debería incluir `Integer sucursalId`.
        dto.setUnidadesDisponiblesCalculadas(0); //
        System.out.println("DEBUG SERVICE: Manufacturado ID: " + dto.getId() + " (" + dto.getDenominacion() + ") - Unidades Disponibles Asignadas al DTO: " + dto.getUnidadesDisponiblesCalculadas());
        return dto;
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO createArticuloManufacturado(@Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        double costoCalculado = calcularCostoProducto(dto);
        if (dto.getPrecioVenta() < costoCalculado) {
            throw new Exception("El precio de venta ($" + dto.getPrecioVenta() + ") no puede ser menor que el costo total de los ingredientes ($" + String.format("%.2f", costoCalculado) + ").");
        }

        ArticuloManufacturado am = new ArticuloManufacturado();
        am.setImagenes(new ArrayList<>());
        am.setManufacturadoDetalles(new ArrayList<>());

        mapRequestDtoToEntity(dto, am);
        ArticuloManufacturado amGuardado = manufacturadoRepository.save(am);

        ArticuloManufacturadoResponseDTO responseDto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amGuardado);
        responseDto.setUnidadesDisponiblesCalculadas(0); //

        return responseDto;
    }

    @Override
    @Transactional
    public ArticuloManufacturadoResponseDTO updateArticuloManufacturado(Integer id, @Valid ArticuloManufacturadoRequestDTO dto) throws Exception {
        double costoCalculado = calcularCostoProducto(dto);
        if (dto.getPrecioVenta() < costoCalculado) {
            throw new Exception("El precio de venta ($" + dto.getPrecioVenta() + ") no puede ser menor que el costo total de los ingredientes ($" + String.format("%.2f", costoCalculado) + ").");
        }
        ArticuloManufacturado amExistente = manufacturadoRepository.findById(id)
                .orElseThrow(() -> new Exception("Artículo Manufacturado no encontrado con ID: " + id));

        mapRequestDtoToEntity(dto, amExistente);
        ArticuloManufacturado amActualizado = manufacturadoRepository.save(amExistente);

        ArticuloManufacturadoResponseDTO responseDto = (ArticuloManufacturadoResponseDTO) mappers.convertArticuloToResponseDto(amActualizado);
        responseDto.setUnidadesDisponiblesCalculadas(0); //

        return responseDto;
    }

    @Override
    @Transactional
    public void deleteArticuloManufacturado(Integer id) throws Exception {
        if (!manufacturadoRepository.existsById(id)) {
            throw new Exception("Artículo Manufacturado no encontrado con ID: " + id + " para eliminar.");
        }
        manufacturadoRepository.deleteById(id);
    }

    // Método que ahora recibe sucursalId
    public Integer calcularUnidadesDisponibles(ArticuloManufacturado manufacturado, Integer sucursalId) throws Exception {
        System.out.println("DEBUG CALC_UNID: Calculando unidades para manufacturado ID: " + manufacturado.getId() + " - " + manufacturado.getDenominacion() + " en sucursal ID: " + sucursalId);

        if (manufacturado.getManufacturadoDetalles() == null || manufacturado.getManufacturadoDetalles().isEmpty()) {
            System.out.println("DEBUG CALC_UNID: Manufacturado ID: " + manufacturado.getId() + " no tiene detalles de receta. Unidades disponibles: 0");
            return 0;
        }

        int unidadesDisponiblesMinimo = Integer.MAX_VALUE;

        for (ArticuloManufacturadoDetalle detalleReceta : manufacturado.getManufacturadoDetalles()) {
            ArticuloInsumo insumoComponente = detalleReceta.getArticuloInsumo();
            if (insumoComponente == null) {
                System.err.println("WARN: Detalle de receta para " + manufacturado.getDenominacion() + " tiene un insumo nulo.");
                return 0;
            }
            System.out.println("DEBUG CALC_UNID:   Procesando componente de receta: " + insumoComponente.getDenominacion() + " (ID: " + insumoComponente.getId() + ")");

            // OBTENER EL DTO DEL STOCK POR SUCURSAL
            StockInsumoSucursalResponseDTO stockInsumoSucursalDTO = stockInsumoSucursalService.getStockByInsumoAndSucursal(
                    insumoComponente.getId(), sucursalId);

            // Acceder al stockActual desde el DTO
            Double stockActual = stockInsumoSucursalDTO.getStockActual();
            Double cantidadNecesariaPorUnidad = detalleReceta.getCantidad();
            System.out.println("DEBUG CALC_UNID:     Insumo: " + insumoComponente.getDenominacion() + " - Stock Actual LEÍDO: " + stockActual + ", Cantidad Necesaria por Receta: " + cantidadNecesariaPorUnidad);

            if (cantidadNecesariaPorUnidad <= 0) {
                System.err.println("WARN CALC_UNID:    Insumo " + insumoComponente.getDenominacion() + " (ID: " + insumoComponente.getId() + ") tiene cantidadNecesariaPorUnidad CERO/NEGATIVA. Esto es un error en la receta. Unidades disponibles: 0");
                return 0;
            }

            if (stockActual == null || stockActual < cantidadNecesariaPorUnidad) {
                System.out.println("DEBUG CALC_UNID:     STOCK INSUFICIENTE para " + insumoComponente.getDenominacion() + ". Se necesitan " + cantidadNecesariaPorUnidad + ", disponibles " + (stockActual != null ? stockActual : 0.0) + ". Unidades disponibles: 0");
                return 0;
            }

            int unidadesConEsteInsumo = (int) Math.floor(stockActual / cantidadNecesariaPorUnidad);
            System.out.println("DEBUG CALC_UNID:     Unidades que se pueden hacer con " + insumoComponente.getDenominacion() + ": " + unidadesConEsteInsumo);
            if (unidadesConEsteInsumo < unidadesDisponiblesMinimo) {
                unidadesDisponiblesMinimo = unidadesConEsteInsumo;
            }
        }
        Integer resultadoFinal = (unidadesDisponiblesMinimo == Integer.MAX_VALUE) ? 0 : unidadesDisponiblesMinimo;
        System.out.println("DEBUG CALC_UNID: Final para manufacturado ID: " + manufacturado.getId() + " - Unidades Disponibles Calculadas: " + resultadoFinal);
        return resultadoFinal;
    }
}