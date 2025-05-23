/**
 * @file ubicacionService.ts
 * @description Provee funciones para interactuar con los endpoints de ubicaciones (Países, Provincias, Localidades) de la API.
 */

import apiClient from './apiClient';
import type { Pais, Provincia, Localidad } from '../types/types';

/**
 * @function getAllPaises
 * @description Obtiene una lista de todos los países.
 * @returns {Promise<Pais[]>} Una promesa que resuelve con un array de países.
 */
export const getAllPaises = async (): Promise<Pais[]> => {
  const response = await apiClient.get<Pais[]>('/paises');
  return response.data;
};

/**
 * @function getAllProvincias
 * @description Obtiene una lista de todas las provincias.
 * @returns {Promise<Provincia[]>} Una promesa que resuelve con un array de provincias.
 */
export const getAllProvincias = async (): Promise<Provincia[]> => {
  const response = await apiClient.get<Provincia[]>('/provincias');
  return response.data;
};

/**
 * @function getAllLocalidades
 * @description Obtiene una lista de todas las localidades.
 * @returns {Promise<Localidad[]>} Una promesa que resuelve con un array de localidades.
 */
export const getAllLocalidades = async (): Promise<Localidad[]> => {
  const response = await apiClient.get<Localidad[]>('/localidades');
  return response.data;
};