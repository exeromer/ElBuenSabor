// Nueva carpeta/ElBuenSabor-frontend/src/services/usuarioService.ts
/**
 * @file usuarioService.ts
 * @description Provee funciones para interactuar con los endpoints de Usuarios de la API.
 * Encapsula la lógica de llamadas y la codificación de URLs.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { Usuario, UsuarioRequestDTO } from '../types/types';

export class UsuarioService {

    /**
     * @function getUsuarioByAuth0Id
     * @description Obtiene los datos de un usuario basado en su Auth0 ID.
     * Codifica el Auth0 ID para que sea seguro en la URL.
     * @param {string} auth0Id - El ID de Auth0 del usuario.
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<Usuario>} Una promesa que resuelve con los datos del usuario.
     * @throws {Error} Si el usuario no se encuentra, o si ocurre un error en la petición.
     */
    async getUsuarioByAuth0Id(auth0Id: string, token: string): Promise<Usuario> {
        setAuthToken(token);
        try {
            // ¡Importante! Codificar el auth0Id para que sea seguro en la URL
            const encodedAuth0Id = encodeURIComponent(auth0Id);
            const response = await apiClient.get<Usuario>(`/usuarios/auth0/${encodedAuth0Id}`);
            return response.data;
        } catch (error) {
            console.error(`Error al obtener usuario por Auth0 ID ${auth0Id}:`, error);
            throw error;
        }
    }

    /**
     * @function getAllUsuarios
     * @description Obtiene todos los usuarios (solo para ADMIN).
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<Usuario[]>} Una promesa que resuelve con un array de usuarios.
     */
    async getAllUsuarios(token: string): Promise<Usuario[]> {
        setAuthToken(token);
        const response = await apiClient.get<Usuario[]>('/usuarios');
        return response.data;
    }

    /**
     * @function getUsuarioById
     * @description Obtiene un usuario específico por su ID (solo para ADMIN).
     * @param {number} id - El ID del usuario a obtener.
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<Usuario>} Una promesa que resuelve con el usuario.
     */
    async getUsuarioById(id: number, token: string): Promise<Usuario> {
        setAuthToken(token);
        const response = await apiClient.get<Usuario>(`/usuarios/${id}`);
        return response.data;
    }

    /**
     * @function createUsuario
     * @description Crea un nuevo usuario (solo para ADMIN).
     * @param {UsuarioRequestDTO} data - Los datos del usuario a crear.
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<Usuario>} Una promesa que resuelve con el usuario creado.
     */
    async createUsuario(data: UsuarioRequestDTO, token: string): Promise<Usuario> {
        setAuthToken(token);
        const response = await apiClient.post<Usuario>('/usuarios', data);
        return response.data;
    }

    /**
     * @function updateUsuario
     * @description Actualiza un usuario existente (solo para ADMIN).
     * @param {number} id - El ID del usuario a actualizar.
     * @param {UsuarioRequestDTO} data - Los nuevos datos del usuario.
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<Usuario>} Una promesa que resuelve con el usuario actualizado.
     */
    async updateUsuario(id: number, data: UsuarioRequestDTO, token: string): Promise<Usuario> {
        setAuthToken(token);
        const response = await apiClient.put<Usuario>(`/usuarios/${id}`, data);
        return response.data;
    }

    /**
     * @function softDeleteUsuario
     * @description Realiza un borrado lógico de un usuario (solo para ADMIN).
     * @param {number} id - El ID del usuario a eliminar lógicamente.
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
     */
    async softDeleteUsuario(id: number, token: string): Promise<void> {
        setAuthToken(token);
        await apiClient.delete(`/usuarios/${id}`);
    }
}