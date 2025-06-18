/**
 * @file domicilioService.ts
 * @description Provee funciones para interactuar con los endpoints de Domicilios de la API.
 * Incluye operaciones CRUD para domicilios.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { Domicilio, DomicilioRequestDTO } from '../types/types';

/**
 * @class DomicilioService
 * @description Clase que encapsula las operaciones de la API relacionadas con Domicilios.
 */
export class DomicilioService {
  /**
   * @function createDomicilio
   * @description Crea un nuevo domicilio. Requiere un token de autenticación.
   * @param {DomicilioRequestDTO} data - Los datos del domicilio a crear.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<Domicilio>} Una promesa que resuelve con el domicilio creado.
   */
  async createDomicilio(data: DomicilioRequestDTO, token: string): Promise<Domicilio> {
    setAuthToken(token);
    const response = await apiClient.post<Domicilio>('/domicilios', data);
    return response.data;
  }

  /**
   * @function updateDomicilio
   * @description Actualiza un domicilio existente. Requiere un token de autenticación.
   * @param {number} id - El ID del domicilio a actualizar.
   * @param {DomicilioRequestDTO} data - Los nuevos datos del domicilio.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<Domicilio>} Una promesa que resuelve con el domicilio actualizado.
   */
  async updateDomicilio(id: number, data: DomicilioRequestDTO, token: string): Promise<Domicilio> {
    setAuthToken(token);
    const response = await apiClient.put<Domicilio>(`/domicilios/${id}`, data);
    return response.data;
  }

  /**
   * @function deleteDomicilio
   * @description Elimina (lógicamente) un domicilio. Requiere un token de autenticación.
   * @param {number} id - El ID del domicilio a eliminar.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
   */
  async deleteDomicilio(id: number, token: string): Promise<void> {
    setAuthToken(token);
    await apiClient.delete(`/domicilios/${id}`);
  }
}