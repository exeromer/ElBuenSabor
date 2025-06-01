/**
 * @file unidadMedidaService.ts
 * @description Provee funciones para interactuar con los endpoints de Unidades de Medida de la API.
 * Incluye operaciones CRUD para unidades de medida.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { UnidadMedida } from '../types/types';

/**
 * @function getUnidadesMedida
 * @description Obtiene todas las unidades de medida.
 * @returns {Promise<UnidadMedida[]>} Una promesa que resuelve con un array de unidades de medida.
 */
export const getUnidadesMedida = async (): Promise<UnidadMedida[]> => {
  const response = await apiClient.get<UnidadMedida[]>('/unidadesmedida');
  return response.data;
};

/**
 * @function createUnidadMedida
 * @description Crea una nueva unidad de medida. Requiere un token de autenticación.
 * @param {UnidadMedida} data - Los datos de la unidad de medida a crear.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<UnidadMedida>} Una promesa que resuelve con la unidad de medida creada.
 */
export const createUnidadMedida = async (data: UnidadMedida, token: string): Promise<UnidadMedida> => {
  setAuthToken(token);
  const response = await apiClient.post<UnidadMedida>('/unidadesmedida', data);
  return response.data;
};

/**
 * @function updateUnidadMedida
 * @description Actualiza una unidad de medida existente. Requiere un token de autenticación.
 * @param {number} id - El ID de la unidad de medida a actualizar.
 * @param {UnidadMedida} data - Los nuevos datos de la unidad de medida.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<UnidadMedida>} Una promesa que resuelve con la unidad de medida actualizada.
 */
export const updateUnidadMedida = async (id: number, data: UnidadMedida, token: string): Promise<UnidadMedida> => {
  setAuthToken(token);
  const response = await apiClient.put<UnidadMedida>(`/unidadesmedida/${id}`, data);
  return response.data;
};

/**
 * @function deleteUnidadMedida
 * @description Elimina (lógicamente) una unidad de medida. Requiere un token de autenticación.
 * @param {number} id - El ID de la unidad de medida a eliminar.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
 */
export const deleteUnidadMedida = async (id: number, token: string): Promise<void> => {
  setAuthToken(token);
  await apiClient.delete(`/unidadesmedida/${id}`);
};