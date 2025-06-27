import apiClient from '../services/apiClient';
import type { CategoriaResponse, CategoriaRequest } from '../types/types';

// La entidad Categoria para POST/PUT, según el controller
type CategoriaCreateOrUpdate = {
  id?: number;
  denominacion: string;
  articulos?: object[];
  estadoActivo?: boolean;
};

export class CategoriaService {
  /**
   * Obtiene todas las categorías.
   */
  static async getAll(): Promise<CategoriaResponse[]> {
    const response = await apiClient.get<CategoriaResponse[]>('/categorias');
    return response.data;
  }

  /**
   * Obtiene todas las categorías de una sucursal específica.
   * @param id - El ID de la sucursal.
   */
  static async getBySucursalId(id: number): Promise<CategoriaResponse[]> {
    try {
      const response = await apiClient.get<CategoriaResponse[]>(`/categorias/sucursal/${id}`);
      return response.data;
    } catch (error: any) {
      if (error.response && error.response.status === 404) {
        return [];
      }
      // Si es otro tipo de error, lo seguimos lanzando.
      throw error;
    }
  }


  /**
   * Obtiene una categoría por su ID.
   * @param id - El ID de la categoría.
   */
  static async getById(id: number): Promise<CategoriaResponse> {
    const response = await apiClient.get<CategoriaResponse>(`/categorias/${id}`);
    return response.data;
  }

  /**
   * Crea una nueva categoría.
   * NOTA: El backend espera una entidad, no un DTO.
   * @param data - Los datos de la categoría a crear.
   */
  static async create(data: CategoriaRequest): Promise<CategoriaResponse> {
    const response = await apiClient.post<CategoriaResponse>('/categorias', data);
    return response.data;
  }

  /**
   * Actualiza una categoría.
   * NOTA: El backend espera una entidad, no un DTO.
   * @param id - El ID de la categoría a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: CategoriaRequest): Promise<CategoriaResponse> {
    const response = await apiClient.put<CategoriaResponse>(`/categorias/${id}`, data);
    return response.data;
  }

  /**
   * Elimina una categoría.
   * @param id - El ID de la categoría a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/categorias/${id}`);
  }
}