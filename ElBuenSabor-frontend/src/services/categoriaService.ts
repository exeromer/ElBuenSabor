/**
 * @file categoriaService.ts
 * @description Provee funciones para interactuar con los endpoints de Categorías de la API.
 * Incluye operaciones CRUD para categorías.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { Categoria } from '../types/types';

/**
 * @function getCategorias
 * @description Obtiene todas las categorías activas.
 * @returns {Promise<Categoria[]>} Una promesa que resuelve con un array de categorías.
 * @throws {Error} Si ocurre un error durante la petición.
 */
export const getCategorias = async (): Promise<Categoria[]> => {
  try {
    const response = await apiClient.get<Categoria[]>('/categorias');
    return response.data.filter(cat => cat.estadoActivo);
  } catch (error) {
    console.error('Error al obtener categorías:', error);
    throw error;
  }
};

/**
 * @function createCategoria
 * @description Crea una nueva categoría. Requiere un token de autenticación.
 * @param {Categoria} data - Los datos de la categoría a crear.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Categoria>} Una promesa que resuelve con la categoría creada.
 */
export const createCategoria = async (data: Categoria, token: string): Promise<Categoria> => {
  setAuthToken(token);
  const response = await apiClient.post<Categoria>('/categorias', data);
  return response.data;
};

/**
 * @function updateCategoria
 * @description Actualiza una categoría existente. Requiere un token de autenticación.
 * @param {number} id - El ID de la categoría a actualizar.
 * @param {Categoria} data - Los nuevos datos de la categoría.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Categoria>} Una promesa que resuelve con la categoría actualizada.
 */
export const updateCategoria = async (id: number, data: Categoria, token: string): Promise<Categoria> => {
  setAuthToken(token);
  const response = await apiClient.put<Categoria>(`/categorias/${id}`, data);
  return response.data;
};

/**
 * @function deleteCategoria
 * @description Elimina (lógicamente) una categoría. Requiere un token de autenticación.
 * @param {number} id - El ID de la categoría a eliminar.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
 */
export const deleteCategoria = async (id: number, token: string): Promise<void> => {
  setAuthToken(token);
  await apiClient.delete(`/categorias/${id}`);
};