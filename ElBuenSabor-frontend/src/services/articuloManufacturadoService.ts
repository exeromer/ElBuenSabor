/**
 * @file articuloManufacturadoService.ts
 * @description Provee funciones para interactuar con los endpoints de Artículos Manufacturados de la API.
 * Incluye operaciones CRUD para artículos manufacturados.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { ArticuloManufacturado, ArticuloManufacturadoRequestDTO } from '../types/types';

/**
 * @class ArticuloManufacturadoService
 * @description Clase que encapsula las operaciones de la API relacionadas con Artículos Manufacturados.
 */
export class ArticuloManufacturadoService {
  /**
   * @function getArticulosManufacturados
   * @description Obtiene todos los artículos manufacturados, opcionalmente filtrados por un término de búsqueda.
   * El backend se encarga de filtrar por estadoActivo=true si no se especifica lo contrario.
   * @param {string} [searchTerm] - El término de búsqueda opcional para filtrar por denominación.
   * @returns {Promise<ArticuloManufacturado[]>} Una promesa que resuelve con un array de artículos manufacturados.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getArticulosManufacturados(
      searchTerm?: string,
      estadoActivo?: boolean | null
  ): Promise<ArticuloManufacturado[]> {
    try {
      const params: any = {};
      if (searchTerm) {
        params.denominacion = searchTerm;
      }
      if (estadoActivo !== undefined && estadoActivo !== null) {
        params.estado = estadoActivo;
      }
      const response = await apiClient.get<ArticuloManufacturado[]>('/articulosmanufacturados', { params });
      return response.data;
    } catch (error) {
      console.error('Error al obtener artículos manufacturados:', error);
      throw error;
    }
  }

  /**
   * @function getArticuloManufacturadoById
   * @description Obtiene un artículo manufacturado específico por su ID.
   * @param {number} id - El ID del artículo manufacturado a obtener.
   * @returns {Promise<ArticuloManufacturado>} Una promesa que resuelve con el artículo manufacturado.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getArticuloManufacturadoById(id: number): Promise<ArticuloManufacturado> {
    try {
      const response = await apiClient.get<ArticuloManufacturado>(`/articulosmanufacturados/${id}`);
      return response.data;
    } catch (error) {
      console.error(`Error al obtener artículo manufacturado con ID ${id}:`, error);
      throw error;
    }
  }

  /**
   * @function createArticuloManufacturado
   * @description Crea un nuevo artículo manufacturado. Requiere un token de autenticación.
   * @param {ArticuloManufacturadoRequestDTO} data - Los datos del artículo manufacturado a crear.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<ArticuloManufacturado>} Una promesa que resuelve con el artículo manufacturado creado.
   */
  async createArticuloManufacturado(data: ArticuloManufacturadoRequestDTO, token: string): Promise<ArticuloManufacturado> {
    setAuthToken(token);
    const response = await apiClient.post<ArticuloManufacturado>('/articulosmanufacturados', data);
    return response.data;
  }

  /**
   * @function updateArticuloManufacturado
   * @description Actualiza un artículo manufacturado existente. Requiere un token de autenticación.
   * @param {number} id - El ID del artículo manufacturado a actualizar.
   * @param {ArticuloManufacturadoRequestDTO} data - Los nuevos datos del artículo manufacturado.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<ArticuloManufacturado>} Una promesa que resuelve con el artículo manufacturado actualizado.
   */
  async updateArticuloManufacturado(id: number, data: ArticuloManufacturadoRequestDTO, token: string): Promise<ArticuloManufacturado> {
    setAuthToken(token);
    const response = await apiClient.put<ArticuloManufacturado>(`/articulosmanufacturados/${id}`, data);
    return response.data;
  }

  /**
   * @function deleteArticuloManufacturado
   * @description Elimina (lógicamente) un artículo manufacturado.
   * Requiere un token de autenticación.
   * @param {number} id - El ID del artículo manufacturado a eliminar.
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
   */
  async deleteArticuloManufacturado(id: number, token: string): Promise<void> {
    setAuthToken(token);
    await apiClient.delete(`/articulosmanufacturados/${id}`);
  }
}