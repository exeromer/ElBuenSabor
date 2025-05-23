/**
 * @file sucursalService.ts
 * @description Provee funciones para interactuar con los endpoints de Sucursales de la API.
 * Incluye operaciones para obtener sucursales.
 */

import apiClient from './apiClient';
import type { Sucursal } from '../types/types';

/**
 * @function getSucursales
 * @description Obtiene todas las sucursales activas.
 * @returns {Promise<Sucursal[]>} Una promesa que resuelve con un array de sucursales.
 * @throws {Error} Si ocurre un error durante la petición.
 */
export const getSucursales = async (): Promise<Sucursal[]> => {
  try {
    const response = await apiClient.get<Sucursal[]>('/sucursales');
    return response.data.filter(s => s.estadoActivo);
  } catch (error) {
    console.error('Error al obtener sucursales:', error);
    throw error;
  }
};