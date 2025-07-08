import apiClient from './apiClient';
import type { ClienteRanking, ArticuloManufacturadoRanking } from '../types/types';

interface RankingParams {
  fechaDesde?: string; // Formato YYYY-MM-DD
  fechaHasta?: string; // Formato YYYY-MM-DD
  page?: number;
  size?: number;
}

export class EstadisticaService {
  /**
   * Obtiene el ranking de clientes por cantidad de pedidos.
   */
  static async getRankingClientesPorCantidad(params: RankingParams = {}): Promise<ClienteRanking[]> {
    const response = await apiClient.get<ClienteRanking[]>('/estadisticas/ranking-clientes/por-cantidad', { params });
    return response.data;
  }

  /**
   * Obtiene el ranking de clientes por monto total comprado.
   */
  static async getRankingClientesPorMonto(params: RankingParams = {}): Promise<ClienteRanking[]> {
    const response = await apiClient.get<ClienteRanking[]>('/estadisticas/ranking-clientes/por-monto', { params });
    return response.data;
  }

  /**
   * Obtiene el ranking de artículos manufacturados más vendidos.
   */
  static async getRankingArticulosMasVendidos(params: RankingParams = {}): Promise<ArticuloManufacturadoRanking[]> {
    const response = await apiClient.get<ArticuloManufacturadoRanking[]>('/estadisticas/articulos-manufacturados/ranking/mas-vendidos', { params });
    return response.data;
  }
}