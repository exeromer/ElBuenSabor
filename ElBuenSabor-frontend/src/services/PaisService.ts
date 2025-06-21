import apiClient from '../services/apiClient';
import type { PaisResponse } from '../types/types';

type PaisCreateOrUpdate = {
    nombre: string;
};

export class PaisService {
  /**
   * Obtiene todos los países.
   */
  static async getAll(): Promise<PaisResponse[]> {
    const response = await apiClient.get<PaisResponse[]>('/paises');
    return response.data;
  }
  
  /**
   * Obtiene un país por su ID.
   * @param id - El ID del país.
   */
  static async getById(id: number): Promise<PaisResponse> {
    const response = await apiClient.get<PaisResponse>(`/paises/${id}`);
    return response.data;
  }

  /**
   * Crea un nuevo país.
   * @param data - Datos del país a crear.
   */
  static async create(data: PaisCreateOrUpdate): Promise<PaisResponse> {
    const response = await apiClient.post<PaisResponse>('/paises', data);
    return response.data;
  }

  /**
   * Actualiza un país.
   * @param id - El ID del país a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: PaisCreateOrUpdate): Promise<PaisResponse> {
    const response = await apiClient.put<PaisResponse>(`/paises/${id}`, data);
    return response.data;
  }

  /**
   * Elimina un país.
   * @param id - El ID del país a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/paises/${id}`);
  }
}