import apiClient from '../services/apiClient';
import type { SucursalRequest, SucursalResponse } from '../types/types';

export class SucursalService {
  /**
   * Crea una nueva sucursal.
   * @param data - Los datos de la sucursal.
   */
  static async create(data: SucursalRequest): Promise<SucursalResponse> {
    const response = await apiClient.post<SucursalResponse>('/sucursales', data);
    return response.data;
  }

  /**
   * Obtiene todas las sucursales.
   */
  static async getAll(): Promise<SucursalResponse[]> {
    const response = await apiClient.get<SucursalResponse[]>('/sucursales');
    return response.data;
  }

  /**
   * Obtiene una sucursal por su ID.
   * @param id - El ID de la sucursal.
   */
  static async getById(id: number): Promise<SucursalResponse> {
    const response = await apiClient.get<SucursalResponse>(`/sucursales/${id}`);
    return response.data;
  }

  /**
   * Actualiza una sucursal.
   * @param id - El ID de la sucursal a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: SucursalRequest): Promise<SucursalResponse> {
    const response = await apiClient.put<SucursalResponse>(`/sucursales/${id}`, data);
    return response.data;
  }

  /**
   * Realiza un borrado lógico de una sucursal.
   * @param id - El ID de la sucursal.
   */
  static async delete(id: number): Promise<{ mensaje: string }> {
    const response = await apiClient.delete<{ mensaje: string }>(`/sucursales/${id}`);
    return response.data;
  }
}