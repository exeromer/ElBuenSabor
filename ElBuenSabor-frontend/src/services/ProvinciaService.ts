import apiClient from '../services/apiClient';
import type { ProvinciaResponse } from '../types/types';

type ProvinciaCreateOrUpdate = {
    nombre: string;
    pais: { id: number };
};

export class ProvinciaService {
  /**
   * Obtiene todas las provincias.
   */
  static async getAll(): Promise<ProvinciaResponse[]> {
    const response = await apiClient.get<ProvinciaResponse[]>('/provincias');
    return response.data;
  }

  /**
   * Obtiene una provincia por su ID.
   * @param id - El ID de la provincia.
   */
  static async getById(id: number): Promise<ProvinciaResponse> {
    const response = await apiClient.get<ProvinciaResponse>(`/provincias/${id}`);
    return response.data;
  }

  /**
   * Crea una nueva provincia.
   * @param data - Los datos de la provincia a crear.
   */
  static async create(data: ProvinciaCreateOrUpdate): Promise<ProvinciaResponse> {
    const response = await apiClient.post<ProvinciaResponse>('/provincias', data);
    return response.data;
  }

  /**
   * Actualiza una provincia.
   * @param id - El ID de la provincia a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: ProvinciaCreateOrUpdate): Promise<ProvinciaResponse> {
    const response = await apiClient.put<ProvinciaResponse>(`/provincias/${id}`, data);
    return response.data;
  }
  
  /**
   * Elimina una provincia.
   * @param id - El ID de la provincia a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/provincias/${id}`);
  }
}