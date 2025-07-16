package com.powerRanger.ElBuenSabor.services;

import com.powerRanger.ElBuenSabor.dtos.ArticuloManufacturadoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ClienteRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.ArticuloInsumoRankingDTO;
import com.powerRanger.ElBuenSabor.dtos.MovimientosMonetariosDTO;
import java.time.LocalDate;
import java.util.List;

public interface EstadisticaService {
    List<ClienteRankingDTO> getRankingClientesPorCantidadPedidos(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;
    List<ClienteRankingDTO> getRankingClientesPorMontoTotal(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;
    List<ArticuloManufacturadoRankingDTO> getRankingArticulosManufacturadosMasVendidos(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;
    List<ArticuloInsumoRankingDTO> getRankingArticulosInsumosMasVendidos(LocalDate fechaDesde, LocalDate fechaHasta, int page, int size) throws Exception;
    MovimientosMonetariosDTO getMovimientosMonetarios(LocalDate fechaDesde, LocalDate fechaHasta) throws Exception;
    
    
}