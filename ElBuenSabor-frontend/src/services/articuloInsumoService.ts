/**
 * @file articuloInsumoService.ts
 * @description Provee funciones para interactuar con los endpoints de Artículos Insumo de la API.
 * Incluye operaciones CRUD para artículos insumo.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { ArticuloInsumo, ArticuloInsumoRequestDTO, ArticuloInsumoResponseDTO } from '../types/types';


/**
 * @class ArticuloInsumoService
 * @description Clase que encapsula las operaciones de la API relacionadas con Artículos Insumo.
 */
export class ArticuloInsumoService { // <-- Clase exportada

  /**
   * @function getArticulosInsumo
   * @description Obtiene todos los artículos insumo activos.
   * @param {string} [searchTerm] - Término de búsqueda opcional para filtrar por denominación.
   * @param {boolean | null} [estadoActivo] - Opcional. Filtra por estado activo. Puede ser `true`, `false` o `null` para todos.
   * @returns {Promise<ArticuloInsumo[]>} Una promesa que resuelve con un array de artículos insumo.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getArticulosInsumo(
      searchTerm?: string,
      estadoActivo?: boolean | null // Añadir estadoActivo, puede ser null para "todos"
  ): Promise<ArticuloInsumo[]> {
    try {
      const params: any = {};
      if (searchTerm) {
        params.denominacion = searchTerm; 
      }
      if (estadoActivo !== undefined && estadoActivo !== null) { // Solo añadir si no es undefined/null
        params.estado = estadoActivo;
      }
      // Si estadoActivo es undefined o null, no se envía el parámetro 'estado' al backend,
      // y el backend debería devolver todos.
      const response = await apiClient.get<ArticuloInsumo[]>('/articulosinsumo', { params });
      return response.data;
    } catch (error) { 
      console.error('Error al obtener artículos insumo:', error);
      throw error;
    }
  }

  /**
   * @function createArticuloInsumo
   * @description Crea un nuevo artículo insumo. Requiere un token de autenticación.
   * @param {ArticuloInsumoRequestDTO} data - Los datos del artículo insumo a crear.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<ArticuloInsumoResponseDTO>} Una promesa que resuelve con el artículo insumo creado.
   */
  async createArticuloInsumo(data: ArticuloInsumoRequestDTO, token: string): Promise<ArticuloInsumoResponseDTO> {
    setAuthToken(token);
    const response = await apiClient.post<ArticuloInsumoResponseDTO>('/articulosinsumo', data);
    return response.data;
  }

  /**
   * @function updateArticuloInsumo
   * @description Actualiza un artículo insumo existente. Requiere un token de autenticación.
   * @param {number} id - El ID del artículo insumo a actualizar.
   * @param {ArticuloInsumoRequestDTO} data - Los nuevos datos del artículo insumo.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<ArticuloInsumoResponseDTO>} Una promesa que resuelve con el artículo insumo actualizado.
   */
  async updateArticuloInsumo(id: number, data: ArticuloInsumoRequestDTO, token: string): Promise<ArticuloInsumoResponseDTO> {
    setAuthToken(token);
    const response = await apiClient.put<ArticuloInsumoResponseDTO>(`/articulosinsumo/${id}`, data);
    return response.data;
  }

  /**
   * @function deleteArticuloInsumo
   * @description Elimina (lógicamente) un artículo insumo. Requiere un token de autenticación.
   * @param {number} id - El ID del artículo insumo a eliminar.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
   */
  async deleteArticuloInsumo(id: number, token: string): Promise<void> {
    setAuthToken(token);
    await apiClient.delete(`/articulosinsumo/${id}`);
  }

  /**
   * @function getArticuloInsumoById
   * @description Obtiene un artículo insumo específico por su ID.
   * @param {number} id - El ID del artículo insumo a obtener.
   * @returns {Promise<ArticuloInsumo>} Una promesa que resuelve con el artículo insumo.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getArticuloInsumoById(id: number): Promise<ArticuloInsumo> {
    try {
      const response = await apiClient.get<ArticuloInsumo>(`/articulosinsumo/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener artículo insumo con ID ${id}:`, error);
      throw error;
    }
  }
}
