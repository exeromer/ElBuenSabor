/**
 * @file clienteUsuarioService.ts
 * @description Provee funciones para interactuar con los endpoints de Clientes y Usuarios de la API.
 * Incluye operaciones para gestión de clientes y usuarios (principalmente para roles de administración).
 */

import apiClient, { setAuthToken } from './apiClient';
import type { Cliente, ClienteRequestDTO, Usuario, UsuarioRequestDTO } from '../types/types';

/**
 * @function getClienteByAuth0Id
 * @description Obtiene los datos de un cliente basado en su Auth0 ID.
 * Este proceso implica primero obtener el `Usuario` asociado al Auth0 ID,
 * y luego obtener el `Cliente` vinculado a ese `Usuario`.
 * @param {string} auth0Id - El ID de Auth0 del usuario.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Cliente>} Una promesa que resuelve con los datos del cliente.
 * @throws {Error} Si el usuario o cliente no se encuentran, o si ocurre un error en la petición.
 */
export const getClienteByAuth0Id = async (auth0Id: string, token: string): Promise<Cliente> => {
  setAuthToken(token);
  try {
    const userResponse = await apiClient.get<Usuario>(`/usuarios/auth0/${auth0Id}`);
    const userId = userResponse.data.id;
    const clientResponse = await apiClient.get<Cliente>(`/clientes/usuario/${userId}`);
    return clientResponse.data;
  } catch (error) {
    console.error(`Error al obtener cliente para Auth0 ID ${auth0Id}:`, error);
    throw error;
  }
};

/**
 * @function getAllUsuarios
 * @description Obtiene todos los usuarios (solo para ADMIN).
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Usuario[]>} Una promesa que resuelve con un array de usuarios.
 */
export const getAllUsuarios = async (token: string): Promise<Usuario[]> => {
  setAuthToken(token);
  const response = await apiClient.get<Usuario[]>('/usuarios');
  return response.data;
};

/**
 * @function getUsuarioById
 * @description Obtiene un usuario específico por su ID (solo para ADMIN).
 * @param {number} id - El ID del usuario a obtener.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Usuario>} Una promesa que resuelve con el usuario.
 */
export const getUsuarioById = async (id: number, token: string): Promise<Usuario> => {
  setAuthToken(token);
  const response = await apiClient.get<Usuario>(`/usuarios/${id}`);
  return response.data;
};

/**
 * @function createUsuario
 * @description Crea un nuevo usuario (solo para ADMIN).
 * @param {UsuarioRequestDTO} data - Los datos del usuario a crear.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Usuario>} Una promesa que resuelve con el usuario creado.
 */
export const createUsuario = async (data: UsuarioRequestDTO, token: string): Promise<Usuario> => {
  setAuthToken(token);
  const response = await apiClient.post<Usuario>('/usuarios', data);
  return response.data;
};

/**
 * @function updateUsuario
 * @description Actualiza un usuario existente (solo para ADMIN).
 * @param {number} id - El ID del usuario a actualizar.
 * @param {UsuarioRequestDTO} data - Los nuevos datos del usuario.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Usuario>} Una promesa que resuelve con el usuario actualizado.
 */
export const updateUsuario = async (id: number, data: UsuarioRequestDTO, token: string): Promise<Usuario> => {
  setAuthToken(token);
  const response = await apiClient.put<Usuario>(`/usuarios/${id}`, data);
  return response.data;
};

/**
 * @function softDeleteUsuario
 * @description Realiza un borrado lógico de un usuario (solo para ADMIN).
 * @param {number} id - El ID del usuario a eliminar lógicamente.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
 */
export const softDeleteUsuario = async (id: number, token: string): Promise<void> => {
  setAuthToken(token);
  await apiClient.delete(`/usuarios/${id}`);
};

/**
 * @function getAllClientes
 * @description Obtiene todos los clientes (para ADMIN).
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Cliente[]>} Una promesa que resuelve con un array de clientes.
 */
export const getAllClientes = async (token: string): Promise<Cliente[]> => {
  setAuthToken(token);
  const response = await apiClient.get<Cliente[]>('/clientes');
  return response.data;
};

/**
 * @function getClienteById
 * @description Obtiene un cliente específico por su ID (para ADMIN).
 * @param {number} id - El ID del cliente a obtener.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Cliente>} Una promesa que resuelve con el cliente.
 */
export const getClienteById = async (id: number, token: string): Promise<Cliente> => {
  setAuthToken(token);
  const response = await apiClient.get<Cliente>(`/clientes/${id}`);
  return response.data;
};

/**
 * @function createCliente
 * @description Crea un nuevo cliente (para ADMIN).
 * @param {ClienteRequestDTO} data - Los datos del cliente a crear.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Cliente>} Una promesa que resuelve con el cliente creado.
 */
export const createCliente = async (data: ClienteRequestDTO, token: string): Promise<Cliente> => {
  setAuthToken(token);
  const response = await apiClient.post<Cliente>('/clientes', data);
  return response.data;
};

/**
 * @function updateCliente
 * @description Actualiza un cliente existente (para ADMIN).
 * @param {number} id - El ID del cliente a actualizar.
 * @param {ClienteRequestDTO} data - Los nuevos datos del cliente.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Cliente>} Una promesa que resuelve con el cliente actualizado.
 */
export const updateCliente = async (id: number, data: ClienteRequestDTO, token: string): Promise<Cliente> => {
  setAuthToken(token);
  const response = await apiClient.put<Cliente>(`/clientes/${id}`, data);
  return response.data;
};

/**
 * @function softDeleteCliente
 * @description Realiza un borrado lógico de un cliente (para ADMIN).
 * @param {number} id - El ID del cliente a eliminar lógicamente.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<void>} Una promesa que resuelve cuando la operación se completa.
 */
export const softDeleteCliente = async (id: number, token: string): Promise<void> => {
  setAuthToken(token);
  await apiClient.delete(`/clientes/${id}`);
};