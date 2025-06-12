/**
 * @file sucursalService.ts
 * @description Provee funciones para interactuar con los endpoints de Sucursales de la API.
 * Incluye operaciones para obtener sucursales.
 */

import apiClient from './apiClient';
import type { Sucursal } from '../types/types';

/**
 * @class SucursalService
 * @description Clase que encapsula las operaciones de la API relacionadas con Sucursales.
 */
export class SucursalService { // <-- Clase exportada

  /**
   * @function getSucursales
   * @description Obtiene todas las sucursales activas.
   * @returns {Promise<Sucursal[]>} Una promesa que resuelve con un array de sucursales.
   * @throws {Error} Si ocurre un error durante la petición.
   */
  async getSucursales(): Promise<Sucursal[]> {
    try {
      const response = await apiClient.get<Sucursal[]>('/sucursales');
      return response.data.filter(s => s.estadoActivo);
    } catch (error) {
      console.error('Error al obtener sucursales:', error);
      throw error;
    }
  }

  // Puedes añadir más métodos CRUD para Sucursales aquí si los necesitas en el futuro,
  // como getSucursalById, createSucursal, updateSucursal, deleteSucursal.
  // Por ejemplo:
  /*
  async getSucursalById(id: number): Promise<Sucursal> {
    const response = await apiClient.get<Sucursal>(`/sucursales/${id}`);
    return response.data;
  }
  */
}
