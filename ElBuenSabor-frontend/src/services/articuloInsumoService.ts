/**
 * @file articuloInsumoService.ts
 * @description Provee funciones para interactuar con los endpoints de Artículos Insumo de la API.
 * Incluye operaciones CRUD para artículos insumo.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { ArticuloInsumo, ArticuloInsumoRequestDTO, ArticuloInsumoResponseDTO } from '../types/types';



/**
 * @function getArticulosInsumo
 * @description Obtiene todos los artículos insumo activos.
 * @returns {Promise<ArticuloInsumo[]>} Una promesa que resuelve con un array de artículos insumo.
 * @throws {Error} Si ocurre un error durante la petición.
 */
export const getArticulosInsumo = async (
    searchTerm?: string,
    // page?: number, // Descomentar si implementas paginación en backend
    // size?: number   // Descomentar si implementas paginación en backend
): Promise<ArticuloInsumo[]> => { // O Promise<{ content: ArticuloInsumo[], totalElements: number }> si hay paginación
  try {
    const params: any = {};
    if (searchTerm) {
      params.denominacion = searchTerm; // O el nombre del parámetro que usa tu backend, ej. "searchTerm"
    }
    // if (page !== undefined) params.page = page;
    // if (size !== undefined) params.size = size;

    const response = await apiClient.get<ArticuloInsumo[]>('/articulosinsumo', { params });
    // Si el backend devuelve un objeto Page, ajusta esto:
    // return response.data; // Si devuelve Page, sería response.data o response.data.content
    return response.data; // Asumiendo que devuelve la lista directamente por ahora
  } catch (error) {
    console.error('Error al obtener artículos insumo:', error);
    throw error;
  }
};


/**
 * @function createArticuloInsumo
 * @description Crea un nuevo artículo insumo. Requiere un token de autenticación.
 * @param {ArticuloInsumo} data - Los datos del artículo insumo a crear.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<ArticuloInsumo>} Una promesa que resuelve con el artículo insumo creado.
 */
export const createArticuloInsumo = async (data: ArticuloInsumoRequestDTO, token: string): Promise<ArticuloInsumoResponseDTO> => {
  setAuthToken(token);
  const response = await apiClient.post<ArticuloInsumoResponseDTO>('/articulosinsumo', data);
  return response.data;
};

/**
 * @function updateArticuloInsumo
 * @description Actualiza un artículo insumo existente. Requiere un token de autenticación.
 * @param {number} id - El ID del artículo insumo a actualizar.
 * @param {ArticuloInsumo} data - Los nuevos datos del artículo insumo.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<ArticuloInsumo>} Una promesa que resuelve con el artículo insumo actualizado.
 */
export const updateArticuloInsumo = async (id: number, data: ArticuloInsumoRequestDTO, token: string): Promise<ArticuloInsumoResponseDTO> => {
  setAuthToken(token);
  const response = await apiClient.put<ArticuloInsumoResponseDTO>(`/articulosinsumo/${id}`, data);
  return response.data;
};

/**
 * @function deleteArticuloInsumo
 * @description Elimina (lógicamente) un artículo insumo. Requiere un token de autenticación.
 * @param {number} id - El ID del artículo insumo a eliminar.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
 */
export const deleteArticuloInsumo = async (id: number, token: string): Promise<void> => {
  setAuthToken(token);
  await apiClient.delete(`/articulosinsumo/${id}`);
};


