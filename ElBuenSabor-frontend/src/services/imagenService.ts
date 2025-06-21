import apiClient from '../services/apiClient';
import type { ImagenRequest, ImagenResponse } from '../types/types';

export class ImagenService {
  /**
   * Crea un nuevo registro de imagen.
   * @param data - Los datos de la imagen (URL, asociaciones).
   */
  static async create(data: ImagenRequest): Promise<ImagenResponse> {
    const response = await apiClient.post<ImagenResponse>('/imagenes', data);
    return response.data;
  }
  
  /**
   * Obtiene todos los registros de imágenes.
   */
  static async getAll(): Promise<ImagenResponse[]> {
    const response = await apiClient.get<ImagenResponse[]>('/imagenes');
    return response.data;
  }

  /**
   * Obtiene un registro de imagen por su ID.
   * @param id - El ID del registro de la imagen.
   */
  static async getById(id: number): Promise<ImagenResponse> {
    const response = await apiClient.get<ImagenResponse>(`/imagenes/${id}`);
    return response.data;
  }

  /**
   * Actualiza un registro de imagen.
   * @param id - El ID del registro a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: ImagenRequest): Promise<ImagenResponse> {
    const response = await apiClient.put<ImagenResponse>(`/imagenes/${id}`, data);
    return response.data;
  }
  
  /**
   * Elimina un registro de imagen y su archivo físico asociado.
   * @param id - El ID del registro a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/imagenes/${id}`);
  }
}