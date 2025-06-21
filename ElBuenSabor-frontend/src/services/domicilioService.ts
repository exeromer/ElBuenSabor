import apiClient from '../services/apiClient';
import type { DomicilioRequest, DomicilioResponse } from '../types/types';

export class DomicilioService {
  /**
   * Crea un nuevo domicilio.
   * @param data - Los datos del domicilio a crear.
   */
  static async create(data: DomicilioRequest): Promise<DomicilioResponse> {
    const response = await apiClient.post<DomicilioResponse>('/domicilios', data);
    return response.data;
  }

  /**
   * Obtiene todos los domicilios.
   */
  static async getAll(): Promise<DomicilioResponse[]> {
    const response = await apiClient.get<DomicilioResponse[]>('/domicilios');
    return response.data;
  }

  /**
   * Obtiene un domicilio por su ID.
   * @param id - El ID del domicilio.
   */
  static async getById(id: number): Promise<DomicilioResponse> {
    const response = await apiClient.get<DomicilioResponse>(`/domicilios/${id}`);
    return response.data;
  }
  
  /**
   * Actualiza un domicilio existente.
   * @param id - El ID del domicilio a actualizar.
   * @param data - Los nuevos datos del domicilio.
   */
  static async update(id: number, data: DomicilioRequest): Promise<DomicilioResponse> {
    const response = await apiClient.put<DomicilioResponse>(`/domicilios/${id}`, data);
    return response.data;
  }
  
  /**
   * Elimina un domicilio.
   * @param id - El ID del domicilio a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/domicilios/${id}`);
  }
}