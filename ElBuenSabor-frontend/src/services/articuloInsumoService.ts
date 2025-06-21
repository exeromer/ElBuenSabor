import apiClient from '../services/apiClient';
import type { ArticuloInsumoRequest, ArticuloInsumoResponse } from '../types/types';

export class ArticuloInsumoService {
  /**
   * Crea un nuevo artículo de tipo Insumo.
   * @param data - Datos del insumo a crear.
   */
  static async create(data: ArticuloInsumoRequest): Promise<ArticuloInsumoResponse> {
    const response = await apiClient.post<ArticuloInsumoResponse>('/articulosinsumo', data);
    return response.data;
  }

  /**
   * Obtiene todos los artículos de tipo Insumo, con filtros opcionales.
   * @param denominacion - Filtra por denominación.
   * @param estadoActivo - Filtra por estado (true para activos, false para inactivos).
   */
  static async getAll(denominacion?: string, estadoActivo?: boolean): Promise<ArticuloInsumoResponse[]> {
    const params = new URLSearchParams();
    if (denominacion) params.append('denominacion', denominacion);
    if (estadoActivo !== undefined) params.append('estado', String(estadoActivo));

    const response = await apiClient.get<ArticuloInsumoResponse[]>('/articulosinsumo', { params });
    return response.data;
  }

  /**
   * Obtiene un insumo por su ID.
   * @param id - El ID del insumo.
   */
  static async getById(id: number): Promise<ArticuloInsumoResponse> {
    const response = await apiClient.get<ArticuloInsumoResponse>(`/articulosinsumo/${id}`);
    return response.data;
  }

  /**
   * Actualiza un insumo existente.
   * @param id - El ID del insumo a actualizar.
   * @param data - Los nuevos datos para el insumo.
   */
  static async update(id: number, data: ArticuloInsumoRequest): Promise<ArticuloInsumoResponse> {
    const response = await apiClient.put<ArticuloInsumoResponse>(`/articulosinsumo/${id}`, data);
    return response.data;
  }

  /**
   * Realiza un borrado lógico de un insumo.
   * @param id - El ID del insumo a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/articulosinsumo/${id}`);
  }
}