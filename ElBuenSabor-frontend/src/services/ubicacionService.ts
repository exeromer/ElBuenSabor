/**
 * @file ubicacionService.ts
 * @description Provee funciones para interactuar con los endpoints de ubicaciones (Países, Provincias, Localidades) de la API.
 */

import apiClient from './apiClient';
import type { Pais, Provincia, Localidad } from '../types/types';

/**
 * @class UbicacionService
 * @description Clase que encapsula las operaciones de la API relacionadas con Ubicaciones (Países, Provincias, Localidades).
 */
export class UbicacionService { // <-- Clase exportada

  /**
   * @function getAllPaises
   * @description Obtiene una lista de todos los países.
   * @returns {Promise<Pais[]>} Una promesa que resuelve con un array de países.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getAllPaises(): Promise<Pais[]> {
    try {
      const response = await apiClient.get<Pais[]>('/paises');
      return response.data;
    } catch (error) {
      console.error('Error al obtener países:', error);
      throw error;
    }
  }

  /**
   * @function getAllProvincias
   * @description Obtiene una lista de todas las provincias.
   * @returns {Promise<Provincia[]>} Una promesa que resuelve con un array de provincias.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getAllProvincias(): Promise<Provincia[]> {
    try {
      const response = await apiClient.get<Provincia[]>('/provincias');
      return response.data;
    } catch (error) {
      console.error('Error al obtener provincias:', error);
      throw error;
    }
  }

  /**
   * @function getAllLocalidades
   * @description Obtiene una lista de todas las localidades.
   * @returns {Promise<Localidad[]>} Una promesa que resuelve con un array de localidades.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getAllLocalidades(): Promise<Localidad[]> {
    try {
      const response = await apiClient.get<Localidad[]>('/localidades');
      return response.data;
    } catch (error) {
      console.error('Error al obtener localidades:', error);
      throw error;
    }
  }
}
