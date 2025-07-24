package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.MovimientosMonetariosDTO;
import java.time.LocalDate;
import java.util.List;

public interface EstadisticaService {
    // MODIFICADOS: Añadido 'sucursalId' a todos los rankings
    List<ClienteRankingDTO> getRankingClientesPorCantidadPedidos(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;
    List<ClienteRankingDTO> getRankingClientesPorMontoTotal(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;
    
    // RENOMBRADOS Y MODIFICADOS
    List<ArticuloManufacturadoRankingDTO> getRankingProductosCocina(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;
    List<ArticuloInsumoRankingDTO> getRankingBebidas(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;

    MovimientosMonetariosDTO getMovimientosMonetarios(Integer sucursalId, LocalDate fechaDesde, LocalDate fechaHasta) throws Exception;
   
}