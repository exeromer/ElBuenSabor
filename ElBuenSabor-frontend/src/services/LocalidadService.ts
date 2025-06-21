import apiClient from '../services/apiClient';
import type { LocalidadResponse } from '../types/types';

type LocalidadCreateOrUpdate = {
    nombre: string;
    provincia: { id: number };
};

export class LocalidadService {
  /**
   * Obtiene todas las localidades.
   */
  static async getAll(): Promise<LocalidadResponse[]> {
    const response = await apiClient.get<LocalidadResponse[]>('/localidades');
    return response.data;
  }

  /**
   * Obtiene una localidad por su ID.
   * @param id - El ID de la localidad.
   */
  static async getById(id: number): Promise<LocalidadResponse> {
    const response = await apiClient.get<LocalidadResponse>(`/localidades/${id}`);
    return response.data;
  }
  
  /**
   * Crea una nueva localidad.
   * @param data - Los datos de la localidad a crear.
   */
  static async create(data: LocalidadCreateOrUpdate): Promise<LocalidadResponse> {
    const response = await apiClient.post<LocalidadResponse>('/localidades', data);
    return response.data;
  }
  
  /**
   * Actualiza una localidad.
   * @param id - El ID de la localidad a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: LocalidadCreateOrUpdate): Promise<LocalidadResponse> {
    const response = await apiClient.put<LocalidadResponse>(`/localidades/${id}`, data);
    return response.data;
  }
  
  /**
   * Elimina una localidad.
   * @param id - El ID de la localidad.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/localidades/${id}`);
  }
}