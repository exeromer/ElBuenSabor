import apiClient from './apiClient';
import type {
  ClienteRanking,
  ArticuloManufacturadoRanking,
  ArticuloInsumoRanking,
  MovimientosMonetarios,
} from '../types/types';

// Parámetros para la obtención de rankings.
interface RankingParams {
  sucursalId: number;
  fechaDesde?: string; // Formato YYYY-MM-DD
  fechaHasta?: string; // Formato YYYY-MM-DD
  page?: number;
  size?: number;
}

// Parámetros para la consulta de movimientos monetarios.
interface MovimientosParams {
  sucursalId: number;
  fechaDesde?: string;
  fechaHasta?: string;
}

//Servicio encargado de consumir los endpoints relacionados
// con estadísticas y reportes.

export class EstadisticaService {
  // --- RANKING DE CLIENTES ---

  // Obtiene el ranking de clientes según la cantidad de compras realizadas.
  static async getRankingClientesPorCantidad(params: RankingParams): Promise<ClienteRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ClienteRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/ranking-clientes/por-cantidad`,
      { params: queryParams },
    );
    return response.data || [];
  }

  // Obtiene el ranking de clientes según el monto total consumido.
  static async getRankingClientesPorMonto(params: RankingParams): Promise<ClienteRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ClienteRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/ranking-clientes/por-monto`,
      { params: queryParams },
    );
    return response.data || [];
  }

  // --- RANKING DE PRODUCTOS ---

  //Obtiene el ranking de productos elaborados en cocina (manufacturados).
  static async getRankingProductosCocina(params: RankingParams): Promise<ArticuloManufacturadoRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ArticuloManufacturadoRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/productos-cocina/ranking`, // <-- Nueva URL
      { params: queryParams },
    );
    return response.data || [];
  }

  // Obtiene el ranking de bebidas vendidas en una sucursal
  static async getRankingBebidas(params: RankingParams): Promise<ArticuloInsumoRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ArticuloInsumoRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/bebidas/ranking`, // <-- Nueva URL
      { params: queryParams },
    );
    return response.data || [];
  }

  // --- MOVIMIENTOS MONETARIOS ---

  //  Recupera los movimientos monetarios (ingresos/egresos) de una sucursal.
  static async getMovimientosMonetarios(params: MovimientosParams): Promise<MovimientosMonetarios> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<MovimientosMonetarios>(
      `/estadisticas/sucursal/${sucursalId}/movimientos-monetarios`,
      { params: queryParams },
    );
    return response.data || [];
  }

  // --- EXPORTACIÓN A EXCEL ---
  static async exportRankingClientesExcel(sucursalId: number, fechaDesde?: string, fechaHasta?: string): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await apiClient.get(`/estadisticas/sucursal/${sucursalId}/ranking-clientes/export/excel`, {
      params,
      responseType: 'blob',
    });
    return response.data || [];
  }

  //  Método de exportación para productos de cocina
  static async exportRankingProductosCocinaExcel(
    sucursalId: number,
    fechaDesde?: string,
    fechaHasta?: string,
  ): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await apiClient.get(
      `/estadisticas/sucursal/${sucursalId}/productos-cocina/export/excel`, // <-- Nueva URL
      {
        params,
        responseType: 'blob',
      },
    );
    return response.data || [];
  }

  //  Método de exportación para bebidas
  static async exportRankingBebidasExcel(sucursalId: number, fechaDesde?: string, fechaHasta?: string): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await apiClient.get(`/estadisticas/sucursal/${sucursalId}/bebidas/export/excel`, {
      params,
      responseType: 'blob',
    });
    return response.data || [];
  }
  //Metodo para exportacion de movimientos monetarios. 
  static async exportMovimientosMonetariosExcel(
    sucursalId: number,
    fechaDesde?: string,
    fechaHasta?: string,
  ): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await apiClient.get(`/estadisticas/sucursal/${sucursalId}/movimientos-monetarios/export/excel`, {
      params,
      responseType: 'arraybuffer',
    });
    return response.data || [];
  }
}
