import apiClient from "./apiClient";
// **CÓDIGO MODIFICADO: Se añaden las importaciones de los nuevos tipos**
import type {
  ClienteRanking,
  ArticuloManufacturadoRanking,
  ArticuloInsumoRanking,
  MovimientosMonetarios,
} from "../types/types";

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
  static async getRankingClientesPorCantidad(
    params: RankingParams = {}
  ): Promise<ClienteRanking[]> {
    const response = await apiClient.get<ClienteRanking[]>(
      "/estadisticas/ranking-clientes/por-cantidad",
      { params }
    );
    return response.data;
  }

  /**
   * Obtiene el ranking de clientes por monto total comprado.
   */
  static async getRankingClientesPorMonto(
    params: RankingParams = {}
  ): Promise<ClienteRanking[]> {
    const response = await apiClient.get<ClienteRanking[]>(
      "/estadisticas/ranking-clientes/por-monto",
      { params }
    );
    return response.data;
  }

  /**
   * Obtiene el ranking de artículos manufacturados más vendidos.
   * **CÓDIGO MODIFICADO: Nombre de la función cambiado de 'getRankingArticulosMasVendidos'**
   */
  static async getRankingArticulosManufacturadosMasVendidos(
    params: RankingParams = {}
  ): Promise<ArticuloManufacturadoRanking[]> {
    const response = await apiClient.get<ArticuloManufacturadoRanking[]>(
      "/estadisticas/articulos-manufacturados/ranking/mas-vendidos",
      { params }
    );
    return response.data;
  }

  // **INICIO DE CÓDIGO NUEVO (Asegúrate de que estas funciones estén en tu archivo)**
  /**
   * Obtiene el ranking de artículos insumos (bebidas) más vendidos.
   */
  static async getRankingArticulosInsumosMasVendidos(
    params: RankingParams = {}
  ): Promise<ArticuloInsumoRanking[]> {
    const response = await apiClient.get<ArticuloInsumoRanking[]>(
      "/estadisticas/articulos-insumos/ranking/mas-vendidos",
      { params }
    );
    return response.data;
  }

  /**
   * Obtiene los movimientos monetarios (ingresos, costos, ganancias).
   */
  static async getMovimientosMonetarios(
    params: Omit<RankingParams, "page" | "size"> = {}
  ): Promise<MovimientosMonetarios> {
    const response = await apiClient.get<MovimientosMonetarios>(
      "/estadisticas/movimientos-monetarios",
      { params }
    );
    return response.data;
  }

  /**
   * Exporta el ranking de clientes a Excel.
   */
  static async exportRankingClientesExcel(
    fechaDesde?: string,
    fechaHasta?: string
  ): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append("fechaDesde", fechaDesde);
    if (fechaHasta) params.append("fechaHasta", fechaHasta);
    const response = await apiClient.get(
      "/estadisticas/ranking-clientes/export/excel",
      {
        params,
        responseType: "blob", // Importante para descargas de archivos
      }
    );
    return response.data;
  }

  /**
   * Exporta el ranking de artículos manufacturados a Excel.
   */
  static async exportRankingArticulosManufacturadosExcel(
    fechaDesde?: string,
    fechaHasta?: string
  ): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append("fechaDesde", fechaDesde);
    if (fechaHasta) params.append("fechaHasta", fechaHasta);
    const response = await apiClient.get(
      "/estadisticas/articulos-manufacturados/export/excel",
      {
        params,
        responseType: "blob",
      }
    );
    return response.data;
  }

  /**
   * Exporta el ranking de artículos insumos a Excel.
   */
  static async exportRankingArticulosInsumosExcel(
    fechaDesde?: string,
    fechaHasta?: string
  ): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append("fechaDesde", fechaDesde);
    if (fechaHasta) params.append("fechaHasta", fechaHasta);
    const response = await apiClient.get(
      "/estadisticas/articulos-insumos/export/excel",
      {
        params,
        responseType: "blob",
      }
    );
    return response.data;
  }

  /**
   * Exporta los movimientos monetarios a Excel.
   */
  static async exportMovimientosMonetariosExcel(
    fechaDesde?: string,
    fechaHasta?: string
  ): Promise<Blob> {
    const params = new URLSearchParams();
    if (fechaDesde) params.append("fechaDesde", fechaDesde);
    if (fechaHasta) params.append("fechaHasta", fechaHasta);
    const response = await apiClient.get(
      "/estadisticas/movimientos-monetarios/export/excel",
      {
        params,
        responseType: "blob",
      }
    );
    return response.data;
  }
  // **FIN DE CÓDIGO NUEVO**
}
