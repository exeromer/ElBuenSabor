import apiClient from './apiClient';
import type { ArticuloManufacturadoRequest, ArticuloManufacturadoResponse } from '../types/types';

export class ArticuloManufacturadoService {
  /**
   * Crea un nuevo artículo manufacturado.
   * @param data - Los datos del artículo a crear.
   */
  static async create(data: ArticuloManufacturadoRequest): Promise<ArticuloManufacturadoResponse> {
    const response = await apiClient.post<ArticuloManufacturadoResponse>('/articulosmanufacturados', data);
    return response.data;
  }

  /**
   * Obtiene todos los artículos manufacturados, con filtros opcionales.
   * @param denominacion - Filtra por denominación.
   * @param estadoActivo - Filtra por estado (true para activos, false para inactivos).
   */
  static async getAll(denominacion?: string, estadoActivo?: boolean): Promise<ArticuloManufacturadoResponse[]> {
     const params = new URLSearchParams();
    if (denominacion) params.append('denominacion', denominacion);
    if (estadoActivo !== undefined) params.append('estado', String(estadoActivo));

    const response = await apiClient.get<ArticuloManufacturadoResponse[]>('/articulosmanufacturados', { params });
    return response.data;
  }

  /**
   * Obtiene un artículo manufacturado por su ID.
   * @param id - El ID del artículo.
   */
  static async getById(id: number): Promise<ArticuloManufacturadoResponse> {
    const response = await apiClient.get<ArticuloManufacturadoResponse>(`/articulosmanufacturados/${id}`);
    return response.data;
  }

  /**
   * Actualiza un artículo manufacturado.
   * @param id - El ID del artículo a actualizar.
   * @param data - Los nuevos datos del artículo.
   */
  static async update(id: number, data: ArticuloManufacturadoRequest): Promise<ArticuloManufacturadoResponse> {
    const response = await apiClient.put<ArticuloManufacturadoResponse>(`/articulosmanufacturados/${id}`, data);
    return response.data;
  }

  /**
   * Realiza un borrado lógico de un artículo manufacturado.
   * @param id - El ID del artículo a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/articulosmanufacturados/${id}`);
  }
}