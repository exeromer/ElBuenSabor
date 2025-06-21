import apiClient from '../services/apiClient';
import type { PromocionRequest, PromocionResponse } from '../types/types';

export class PromocionService {
  /**
   * Crea una nueva promoción.
   * @param data - Los datos de la promoción.
   */
  static async create(data: PromocionRequest): Promise<PromocionResponse> {
    const response = await apiClient.post<PromocionResponse>('/promociones', data);
    return response.data;
  }

  /**
   * Obtiene todas las promociones.
   */
  static async getAll(): Promise<PromocionResponse[]> {
    const response = await apiClient.get<PromocionResponse[]>('/promociones');
    return response.data;
  }

  /**
   * Obtiene una promoción por su ID.
   * @param id - El ID de la promoción.
   */
  static async getById(id: number): Promise<PromocionResponse> {
    const response = await apiClient.get<PromocionResponse>(`/promociones/${id}`);
    return response.data;
  }

  /**
   * Actualiza una promoción.
   * @param id - El ID de la promoción a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: PromocionRequest): Promise<PromocionResponse> {
    const response = await apiClient.put<PromocionResponse>(`/promociones/${id}`, data);
    return response.data;
  }

  /**
   * Realiza un borrado lógico de una promoción.
   * @param id - El ID de la promoción.
   */
  static async delete(id: number): Promise<{ mensaje: string }> {
    const response = await apiClient.delete<{ mensaje: string }>(`/promociones/${id}`);
    return response.data;
  }
}