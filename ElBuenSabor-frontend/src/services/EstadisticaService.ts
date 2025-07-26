import apiClient from './apiClient';
import type {
  ClienteRanking,
  ArticuloManufacturadoRanking,
  ArticuloInsumoRanking,
  MovimientosMonetarios,
} from '../types/types';

// <<-- CAMBIO REALIZADO: La interfaz ahora requiere sucursalId -->>
interface RankingParams {
  sucursalId: number;
  fechaDesde?: string; // Formato YYYY-MM-DD
  fechaHasta?: string; // Formato YYYY-MM-DD
  page?: number;
  size?: number;
}

// <<-- CAMBIO REALIZADO: Interfaz de movimientos también necesita sucursalId -->>
interface MovimientosParams {
  sucursalId: number;
  fechaDesde?: string;
  fechaHasta?: string;
}

export class EstadisticaService {
  // --- RANKING DE CLIENTES ---

  // <<-- CAMBIO REALIZADO: Se actualizó la URL y la lógica para usar sucursalId -->>
  static async getRankingClientesPorCantidad(params: RankingParams): Promise<ClienteRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ClienteRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/ranking-clientes/por-cantidad`,
      { params: queryParams },
    );
    return response.data;
  }

  // <<-- CAMBIO REALIZADO: Se actualizó la URL y la lógica para usar sucursalId -->>
  static async getRankingClientesPorMonto(params: RankingParams): Promise<ClienteRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ClienteRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/ranking-clientes/por-monto`,
      { params: queryParams },
    );
    return response.data;
  }

  // --- RANKING DE PRODUCTOS ---

  // <<-- CAMBIO REALIZADO: Nuevo método para productos de cocina -->>
  static async getRankingProductosCocina(params: RankingParams): Promise<ArticuloManufacturadoRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ArticuloManufacturadoRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/productos-cocina/ranking`, // <-- Nueva URL
      { params: queryParams },
    );
    return response.data;
  }

  // <<-- CAMBIO REALIZADO: Nuevo método para bebidas -->>
  static async getRankingBebidas(params: RankingParams): Promise<ArticuloInsumoRanking[]> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<ArticuloInsumoRanking[]>(
      `/estadisticas/sucursal/${sucursalId}/bebidas/ranking`, // <-- Nueva URL
      { params: queryParams },
    );
    return response.data;
  }

  // --- MOVIMIENTOS MONETARIOS ---

  // <<-- CAMBIO REALIZADO: Se actualizó la URL para usar sucursalId -->>
  static async getMovimientosMonetarios(params: MovimientosParams): Promise<MovimientosMonetarios> {
    const { sucursalId, ...queryParams } = params;
    const response = await apiClient.get<MovimientosMonetarios>(
      `/estadisticas/sucursal/${sucursalId}/movimientos-monetarios`,
      { params: queryParams },
    );
    return response.data;
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
    return response.data;
  }

  // <<-- CAMBIO REALIZADO: Nuevo método de exportación para productos de cocina -->>
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
    return response.data;
  }

  // <<-- CAMBIO REALIZADO: Nuevo método de exportación para bebidas -->>
  static async exportRankingBebidasExcel(sucursalId: number, fechaDesde?: string, fechaHasta?: string): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await apiClient.get(`/estadisticas/sucursal/${sucursalId}/bebidas/export/excel`, {
      params,
      responseType: 'blob',
    });
    return response.data;
  }
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
    return response.data;
  }
}
