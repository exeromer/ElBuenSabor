/**
 * @file pedidoService.ts
 * @description Provee funciones para interactuar con los endpoints de Pedidos de la API.
 * Incluye operaciones para obtener pedidos de un cliente y crear nuevos pedidos.
 */

import apiClient, { setAuthToken } from './apiClient';
import type { Pedido, PedidoRequestDTO } from '../types/types';

/**
 * @function getPedidosByClienteAuth0Id
 * @description Obtiene una lista de pedidos asociados a un cliente específico,
 * identificado por su ID de Auth0. Requiere un token de autenticación.
 * @param {string} auth0Id - El ID de Auth0 del cliente. (Aunque el endpoint lo obtiene del token, se mantiene para claridad).
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<Pedido[]>} Una promesa que resuelve con un array de pedidos.
 * @throws {Error} Si ocurre un error durante la petición.
 */
export const getPedidosByClienteAuth0Id = async (auth0Id: string, token: string): Promise<Pedido[]> => {
  setAuthToken(token);
  try {
    const response = await apiClient.get<Pedido[]>(`/pedidos/mis-pedidos`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener pedidos para Auth0 ID ${auth0Id}:`, error);
    throw error;
  }
};

/**
 * @function createPedido
 * @description Crea un nuevo pedido en el backend.
 * @param {PedidoRequestDTO} pedidoData - Los datos del pedido a crear.
 * @param {string} token - El token JWT para la autenticación.
 * @returns {Promise<any>} Una promesa que resuelve con la respuesta del backend tras la creación del pedido.
 * @throws {Error} Si ocurre un error durante la creación del pedido.
 */
export const createPedido = async (pedidoData: PedidoRequestDTO, token: string): Promise<any> => {
  setAuthToken(token);
  try {
    const response = await apiClient.post('/pedidos', pedidoData);
    return response.data;
  } catch (error) {
    console.error('Error al crear pedido:', error);
    throw error;
  }
};