/**
 * @file imagenService.ts
 * @description Provee funciones para interactuar con los endpoints de gestión de entidades de Imagen de la API.
 * Estas funciones se refieren a los registros de metadatos de imágenes en la base de datos,
 * no a la subida/eliminación de archivos físicos.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { ImagenRequestDTO } from '../types/types';

/**
 * @function createImageEntity
 * @description Crea una nueva entidad de imagen en la base de datos (registro de metadatos de imagen).
 * @param {ImagenRequestDTO} data - Los datos de la imagen a crear.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<any>} Una promesa que resuelve con la respuesta del backend.
 */
export const createImageEntity = async (data: ImagenRequestDTO, token: string): Promise<any> => {
  setAuthToken(token);
  const response = await apiClient.post('/imagenes', data);
  return response.data;
};

/**
 * @function deleteImageEntity
 * @description Elimina una entidad de imagen de la base de datos.
 * @param {number} id - El ID de la entidad de imagen a eliminar.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
 */
export const deleteImageEntity = async (id: number, token: string): Promise<void> => {
  setAuthToken(token);
  await apiClient.delete(`/imagenes/${id}`);
};