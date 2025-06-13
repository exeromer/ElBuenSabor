/**
 * @file unidadMedidaService.ts
 * @description Provee funciones para interactuar con los endpoints de Unidades de Medida de la API.
 * Incluye operaciones CRUD para unidades de medida.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { UnidadMedida } from '../types/types';

/**
 * @class UnidadMedidaService
 * @description Clase que encapsula las operaciones de la API relacionadas con Unidades de Medida.
 */
export class UnidadMedidaService { // <-- Clase exportada

  /**
   * @function getUnidadesMedida
   * @description Obtiene todas las unidades de medida.
   * @returns {Promise<UnidadMedida[]>} Una promesa que resuelve con un array de unidades de medida.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getUnidadesMedida(): Promise<UnidadMedida[]> {
    try {
      const response = await apiClient.get<UnidadMedida[]>('/unidadesmedida');
      return response.data;
    } catch (error) {
      console.error('Error al obtener unidades de medida:', error);
      throw error;
    }
  }

  /**
   * @function createUnidadMedida
   * @description Crea una nueva unidad de medida. Requiere un token de autenticación.
   * @param {UnidadMedida} data - Los datos de la unidad de medida a crear.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<UnidadMedida>} Una promesa que resuelve con la unidad de medida creada.
   */
  async createUnidadMedida(data: UnidadMedida, token: string): Promise<UnidadMedida> {
    setAuthToken(token);
    const response = await apiClient.post<UnidadMedida>('/unidadesmedida', data);
    return response.data;
  }

  /**
   * @function updateUnidadMedida
   * @description Actualiza una unidad de medida existente. Requiere un token de autenticación.
   * @param {number} id - El ID de la unidad de medida a actualizar.
   * @param {UnidadMedida} data - Los nuevos datos de la unidad de medida.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<UnidadMedida>} Una promesa que resuelve con la unidad de medida actualizada.
   */
  async updateUnidadMedida(id: number, data: UnidadMedida, token: string): Promise<UnidadMedida> {
    setAuthToken(token);
    const response = await apiClient.put<UnidadMedida>(`/unidadesmedida/${id}`, data);
    return response.data;
  }

  /**
   * @function deleteUnidadMedida
   * @description Elimina (lógicamente) una unidad de medida. Requiere un token de autenticación.
   * @param {number} id - El ID de la unidad de medida a eliminar.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
   */
  async deleteUnidadMedida(id: number, token: string): Promise<void> {
    setAuthToken(token);
    await apiClient.delete(`/unidadesmedida/${id}`);
  }
}
