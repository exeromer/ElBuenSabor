/**
 * @file fileUploadService.ts
 * @description Provee funciones para la subida y eliminación de archivos (imágenes) del servidor de archivos.
 * También incluye una función para construir URLs de imágenes.
 */

import apiClient, { setAuthToken } from './apiClient';

/**
 * @class FileUploadService
 * @description Clase que encapsula las operaciones de subida/eliminación de archivos y construcción de URLs.
 */
export class FileUploadService { // <-- Clase exportada
  // URL base para todas las peticiones a la API, obtenida de las variables de entorno de Vite.
  // Es crucial para que el servicio sepa dónde realizar las solicitudes.
  private API_BASE_URL: string = import.meta.env.VITE_API_URL;

  /**
   * @function getImageUrl
   * @description Construye la URL completa para visualizar una imagen.
   * Asume que el backend de Spring devuelve la URL completa o un nombre de archivo/UUID.
   * @param {string} filename - El nombre del archivo o la URL completa de la imagen.
   * @returns {string} La URL completa de la imagen.
   */
  getImageUrl = (filename: string): string => { // <-- Método de instancia
    if (filename.startsWith('http')) {
      return filename;
    }
    return `${this.API_BASE_URL}/files/view/${filename}`;
  };

  /**
   * @function uploadFile
   * @description Sube un archivo (imagen) al servidor de archivos.
   * Permite asociar la imagen a un artículo o promoción.
   * @param {File} file - El objeto File a subir.
   * @param {string} token - El token JWT para la autenticación.
   * @param {number} [articuloId] - Opcional. ID del artículo al que se asocia la imagen.
   * @param {number} [promocionId] - Opcional. ID de la promoción a la que se asocia la imagen.
   * @returns {Promise<any>} Una promesa que resuelve con la respuesta del servidor (ej. la URL del archivo subido).
   */
  async uploadFile(file: File, token: string, articuloId?: number, promocionId?: number): Promise<any> {
    setAuthToken(token);
    const formData = new FormData();
    formData.append('file', file);
    if (articuloId) formData.append('articuloId', articuloId.toString());
    if (promocionId) formData.append('promocionId', promocionId.toString());

    const response = await apiClient.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  /**
   * @function deleteFileFromServer
   * @description Elimina un archivo del servidor de archivos.
   * @param {string} filename - El nombre del archivo a eliminar (o UUID).
   * @param {string} token - El token JWT para la autenticación.
   * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
   */
  async deleteFileFromServer(filename: string, token: string): Promise<void> {
    setAuthToken(token);
    await apiClient.delete(`/files/delete/${filename}`);
  }
}
