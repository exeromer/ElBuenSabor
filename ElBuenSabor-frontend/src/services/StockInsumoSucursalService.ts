import apiClient from '../services/apiClient';
import type { StockInsumoSucursalRequest, StockInsumoSucursalResponse } from '../types/types';

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
   * Obtiene el stock de un insumo en una sucursal.
   * @param insumoId - ID del insumo.
   * @param sucursalId - ID de la sucursal.
   */
  static async getByInsumoAndSucursal(insumoId: number, sucursalId: number): Promise<StockInsumoSucursalResponse> {
    const response = await apiClient.get<StockInsumoSucursalResponse>(`/stockinsumosucursal/insumo/${insumoId}/sucursal/${sucursalId}`);
    return response.data;
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