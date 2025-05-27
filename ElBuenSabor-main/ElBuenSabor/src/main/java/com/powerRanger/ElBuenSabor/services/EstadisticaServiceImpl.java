package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
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

    @Override
    @Transactional(readOnly = true)
    public List<ClienteRankingDTO> getRankingClientesPorCantidadPedidos(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        return pedidoRepository.findRankingClientesByCantidadPedidos(
                ESTADO_PEDIDO_PARA_RANKING,
                fechaDesde,
                fechaHasta,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteRankingDTO> getRankingClientesPorMontoTotal(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        return pedidoRepository.findRankingClientesByMontoTotal(
                ESTADO_PEDIDO_PARA_RANKING,
                fechaDesde,
                fechaHasta,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticuloManufacturadoRankingDTO> getRankingArticulosManufacturadosMasVendidos(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception {
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            throw new Exception("La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        return detallePedidoRepository.findRankingArticulosManufacturadosMasVendidos(
                ESTADO_PEDIDO_PARA_RANKING,
                fechaDesde,
                fechaHasta,
                pageable
        );
    }
}