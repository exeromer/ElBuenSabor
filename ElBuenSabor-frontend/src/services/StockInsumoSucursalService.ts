import apiClient from './apiClient';
import type { StockInsumoSucursalRequest, StockInsumoSucursalResponse,ArticuloManufacturadoResponse } from '../types/types';

export class StockInsumoSucursalService {
  /**
   * Crea un nuevo registro de stock.
   * @param data - Los datos del stock.
   */
  static async create(data: StockInsumoSucursalRequest): Promise<StockInsumoSucursalResponse> {
    const response = await apiClient.post<StockInsumoSucursalResponse>('/stockinsumosucursal', data);
    return response.data;
  }

  /**
   * Obtiene todos los registros de stock.
   */
  static async getAll(): Promise<StockInsumoSucursalResponse[]> {
    const response = await apiClient.get<StockInsumoSucursalResponse[]>('/stockinsumosucursal');
    return response.data;
  }
  
  /**
   * Obtiene un registro de stock por su ID.
   * @param id - El ID del registro.
   */
  static async getById(id: number): Promise<StockInsumoSucursalResponse> {
    const response = await apiClient.get<StockInsumoSucursalResponse>(`/stockinsumosucursal/${id}`);
    return response.data;
  }

    /**
   * Obtiene el stock de un insumo específico en una sucursal.
   * @param insumoId - El ID del ArticuloInsumo.
   * @param sucursalId - El ID de la Sucursal.
   * @returns El objeto de stock o null si no se encuentra.
   */
  static async getStockByInsumoAndSucursal(insumoId: number, sucursalId: number): Promise<StockInsumoSucursalResponse | null> {
    try {
      const response = await apiClient.get<StockInsumoSucursalResponse>(`/stockinsumosucursal/insumo/${insumoId}/sucursal/${sucursalId}`);
      return response.data;
    } catch (error: any) {
      if (error.response && error.response.status === 404) {
        return null;
      }
      throw error;
    }
  }

  /**
   * Verifica si un Artículo Manufacturado está disponible en una sucursal.
   * Revisa si hay stock suficiente de TODOS los insumos necesarios.
   * @param articulo - El objeto ArticuloManufacturadoResponse a verificar.
   * @param sucursalId - El ID de la sucursal donde se quiere verificar.
   * @returns `true` si está disponible, `false` si no.
   */
  static async checkDisponibilidadManufacturado(articulo: ArticuloManufacturadoResponse, sucursalId: number): Promise<boolean> {
    if (!articulo.manufacturadoDetalles || articulo.manufacturadoDetalles.length === 0) {
      return false;
    }
    const resultadosStock = await Promise.all(
      articulo.manufacturadoDetalles.map(async (detalle) => {
        // Obtenemos el registro de stock para el insumo del detalle.
        const stockInfo = await this.getStockByInsumoAndSucursal(detalle.articuloInsumo.id, sucursalId);

        // Si no hay registro de stock (stockInfo es null), no hay stock.
        if (!stockInfo) {
          return false;
        }

        // Comparamos el stock actual con la cantidad que requiere la receta.
        return stockInfo.stockActual >= detalle.cantidad;
      })
    );

    // El producto está disponible solo si TODAS las verificaciones de stock devolvieron `true`.
    // El método .every() comprueba esto por nosotros.
    return resultadosStock.every(disponible => disponible);
  }


  /**
   * Actualiza un registro de stock.
   * @param id - ID del registro a actualizar.
   * @param data - Nuevos datos del stock.
   */
  static async update(id: number, data: StockInsumoSucursalRequest): Promise<StockInsumoSucursalResponse> {
    const response = await apiClient.put<StockInsumoSucursalResponse>(`/stockinsumosucursal/${id}`, data);
    return response.data;
  }
  
  /**
   * Elimina un registro de stock.
   * @param id - El ID del registro.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/stockinsumosucursal/${id}`);
  }
  
  /**
   * Reduce el stock de un insumo en una sucursal.
   */
  static async reduceStock(insumoId: number, sucursalId: number, cantidad: number): Promise<StockInsumoSucursalResponse> {
    const response = await apiClient.put<StockInsumoSucursalResponse>(`/stockinsumosucursal/reduceStock/insumo/${insumoId}/sucursal/${sucursalId}/cantidad/${cantidad}`);
    return response.data;
  }

  /**
   * Añade stock a un insumo en una sucursal.
   */
  static async addStock(insumoId: number, sucursalId: number, cantidad: number): Promise<StockInsumoSucursalResponse> {
    const response = await apiClient.put<StockInsumoSucursalResponse>(`/stockinsumosucursal/addStock/insumo/${insumoId}/sucursal/${sucursalId}/cantidad/${cantidad}`);
    return response.data;
  }
}