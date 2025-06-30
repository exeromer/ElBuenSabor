import apiClient from './apiClient';
import type { UnidadMedidaResponse } from '../types/types';

type UnidadMedidaCreateOrUpdate = {
    id?: number;
    denominacion: string;
};

export class UnidadMedidaService {
  /**
   * Obtiene todas las unidades de medida.
   */
  static async getAll(): Promise<UnidadMedidaResponse[]> {
    const response = await apiClient.get<UnidadMedidaResponse[]>('/unidadesmedida');
    return response.data;
  }
  
  /**
   * Obtiene una unidad de medida por su ID.
   * @param id - El ID de la unidad.
   */
  static async getById(id: number): Promise<UnidadMedidaResponse> {
    const response = await apiClient.get<UnidadMedidaResponse>(`/unidadesmedida/${id}`);
    return response.data;
  }

  /**
   * Crea una nueva unidad de medida.
   * @param data - Datos de la unidad.
   */
  static async create(data: UnidadMedidaCreateOrUpdate): Promise<UnidadMedidaResponse> {
    const response = await apiClient.post<UnidadMedidaResponse>('/unidadesmedida', data);
    return response.data;
  }

  /**
   * Actualiza una unidad de medida.
   * @param id - El ID de la unidad a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: UnidadMedidaCreateOrUpdate): Promise<UnidadMedidaResponse> {
    const response = await apiClient.put<UnidadMedidaResponse>(`/unidadesmedida/${id}`, data);
    return response.data;
  }

  /**
   * Elimina una unidad de medida.
   * @param id - El ID de la unidad a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/unidadesmedida/${id}`);
  }
}