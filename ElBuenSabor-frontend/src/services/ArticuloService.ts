import apiClient from './apiClient';
import type { ArticuloResponse } from '../types/types';

export class ArticuloService {
  /**
   * Obtiene todos los artículos (insumos y manufacturados).
   */
  static async getAll(): Promise<ArticuloResponse[]> {
    const response = await apiClient.get<ArticuloResponse[]>('/articulos');
    return response.data;
  }

  /**
   * Obtiene un artículo específico por su ID.
   * @param id - El ID del artículo.
   */
  static async getById(id: number): Promise<ArticuloResponse> {
    const response = await apiClient.get<ArticuloResponse>(`/articulos/${id}`);
    return response.data;
  }
  
  /**
   * Busca un artículo por su denominación.
   * @param denominacion - El término de búsqueda.
   */
  static async findByDenominacion(denominacion: string): Promise<ArticuloResponse> {
    const response = await apiClient.get<ArticuloResponse>('/articulos/buscar', {
      params: { denominacion }
    });
    return response.data;
  }

  /**
   * Realiza un borrado lógico de un artículo por su ID.
   * @param id - El ID del artículo a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/articulos/${id}`);
  }
}