package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.MovimientosMonetariosDTO;
import com.powerRanger.ElBuenSabor.entities.enums.Estado;
import com.powerRanger.ElBuenSabor.repository.DetallePedidoRepository;
import com.powerRanger.ElBuenSabor.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EstadisticaServiceImpl implements EstadisticaService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    private final Estado ESTADO_PEDIDO_PARA_RANKING = Estado.ENTREGADO;

    // <<-- MODIFICADO: Se agregó el parámetro 'sucursalId' a la firma del método.
    @Override
    @Transactional(readOnly = true)
    public List<ClienteRankingDTO> getRankingClientesPorCantidadPedidos(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        // <<-- MODIFICADO: Se pasa 'sucursalId' a la consulta del repositorio.
        return pedidoRepository.findRankingClientesByCantidadPedidos(
                sucursalId,
                ESTADO_PEDIDO_PARA_RANKING,
                fechaDesde,
                fechaHasta,
                pageable
        );
    }

    // <<-- MODIFICADO: Se agregó el parámetro 'sucursalId' a la firma del método.
    @Override
    @Transactional(readOnly = true)
    public List<ClienteRankingDTO> getRankingClientesPorMontoTotal(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        // <<-- MODIFICADO: Se pasa 'sucursalId' a la consulta del repositorio.
        return pedidoRepository.findRankingClientesByMontoTotal(
                sucursalId,
                ESTADO_PEDIDO_PARA_RANKING,
                fechaDesde,
                fechaHasta,
                pageable
        );
    }

    // <<-- MODIFICADO: Se renombró el método y se agregó 'sucursalId'.
    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoRankingDTO> getRankingProductosCocina(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        // <<-- MODIFICADO: Se llama al nuevo método del repositorio 'findRankingProductosCocina'.
        return detallePedidoRepository.findRankingProductosCocina(
                sucursalId,
                ESTADO_PEDIDO_PARA_RANKING,
                fechaDesde,
                fechaHasta,
                pageable
        );
    }
    
    // <<-- MODIFICADO: Se renombró el método y se agregó 'sucursalId'.
    @Override
    @Transactional(readOnly = true)
    public List<ArticuloInsumoRankingDTO> getRankingBebidas(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        // <<-- MODIFICADO: Se llama al nuevo método del repositorio 'findRankingBebidas'.
        return detallePedidoRepository.findRankingBebidas(
                sucursalId,
                ESTADO_PEDIDO_PARA_RANKING,
                fechaDesde,
                fechaHasta,
                pageable
        );
    }

    // <<-- MODIFICADO: Se agregó el parámetro 'sucursalId' a la firma del método.
    @Override
    @Transactional(readOnly = true)
    public MovimientosMonetariosDTO getMovimientosMonetarios(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }

        // <<-- MODIFICADO: Se pasa 'sucursalId' a las consultas del repositorio.
        Double ingresos = pedidoRepository.sumTotalByEstadoAndFechaRange(sucursalId, fechaDesde, fechaHasta);
        Double costos = pedidoRepository.sumTotalCostoByEstadoAndFechaRange(sucursalId, fechaDesde, fechaHasta);

        ingresos = (ingresos != null) ? ingresos : 0.0;
        costos = (costos != null) ? costos : 0.0;
        Double ganancias = ingresos - costos;

        return new MovimientosMonetariosDTO(ingresos, costos, ganancias);
    }
}