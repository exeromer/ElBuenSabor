/**
 * @file apiClient.ts
 * @description Configuración base de la instancia de Axios para todas las peticiones API.
 * Define la URL base de la API y un interceptor para gestionar el token de autenticación JWT.
 * Este archivo es el punto de entrada para todas las interacciones HTTP con el backend.
 */

import axios from 'axios';

/**
 * @constant API_BASE_URL
 * @description URL base para todas las peticiones a la API, obtenida de las variables de entorno de Vite.
 * Es crucial para que el servicio sepa dónde realizar las solicitudes.
 */
const API_BASE_URL = import.meta.env.VITE_API_URL;

/**
 * @constant apiClient
 * @description Instancia configurada de Axios. Se utiliza para todas las peticiones HTTP.
 * Incluye la URL base y el encabezado `Content-Type`. También maneja la inyección
 * del token de autorización para peticiones protegidas.
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * @function setAuthToken
 * @description Configura o elimina el token de autenticación JWT en los encabezados
 * por defecto de Axios. Esto asegura que todas las peticiones subsiguientes
 * enviadas por `apiClient` incluyan el token si está presente, lo cual es vital
 * para acceder a rutas protegidas en el backend.
 * @param {string | null} token - El token JWT a establecer, o `null` para eliminarlo.
 */
export const setAuthToken = (token: string | null) => {
  if (token) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

export default apiClient;