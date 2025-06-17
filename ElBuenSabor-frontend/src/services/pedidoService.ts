// Nueva carpeta/ElBuenSabor-frontend/src/services/pedidoService.ts

/**
 * @file pedidoService.ts
 * @description Provee funciones para interactuar con los endpoints de Pedidos de la API.
 * Incluye operaciones para obtener pedidos de un cliente y crear nuevos pedidos.
 */

import apiClient, { setAuthToken } from './apiClient';
// Importamos Pedido, PedidoRequestDTO y PreferenceMP para los tipos
import type { Pedido, PedidoRequestDTO, PreferenceMP, CrearPedidoRequestDTO } from '../types/types'; 

// Definimos la clase PedidoService
export class PedidoService {

    /**
     * @function getPedidosByClienteAuth0Id
     * @description Obtiene una lista de pedidos asociados a un cliente específico,
     * identificado por su ID de Auth0. Requiere un token de autenticación.
     * @param {string} auth0Id - El ID de Auth0 del cliente. (Aunque el endpoint lo obtiene del token, se mantiene para claridad).
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<Pedido[]>} Una promesa que resuelve con un array de pedidos.
     * @throws {Error} Si ocurre un error durante la petición.
     */
    async getPedidosByClienteAuth0Id(auth0Id: string, token: string): Promise<Pedido[]> {
        setAuthToken(token);
        try {
            const response = await apiClient.get<Pedido[]>(`/pedidos/mis-pedidos`);
            return response.data;
        } catch (error) {
            console.error(`Error al obtener pedidos para Auth0 ID ${auth0Id}:`, error);
            throw error;
        }
    }

        /**
     * @function crearPedidoDesdeCarrito
     * @description Crea un nuevo pedido desde el carrito de un cliente. Llama al endpoint especializado del backend.
     * @param {number} clienteId - El ID del cliente que realiza el pedido.
     * @param {CrearPedidoRequestDTO} pedidoData - Los datos del pedido, incluyendo domicilio y forma de pago.
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<any>} Resuelve con la respuesta del backend (que puede ser el Pedido o un objeto con datos de Mercado Pago).
     */
    async crearPedidoDesdeCarrito(clienteId: number, pedidoData: CrearPedidoRequestDTO, token: string): Promise<any> {
        setAuthToken(token);
        try {
            // Este es el endpoint correcto y robusto de tu backend que maneja toda la lógica.
            const response = await apiClient.post(`/pedidos/cliente/${clienteId}/desde-carrito`, pedidoData);
            return response.data;
        } catch (error) {
            console.error('Error al crear pedido desde el carrito:', error);
            throw error;
        }
    }

    /**
     * @function createPedido
     * @description Crea un nuevo pedido en el backend.
     * @param {PedidoRequestDTO} pedidoData - Los datos del pedido a crear.
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<Pedido>} Una promesa que resuelve con el pedido creado.
     * @throws {Error} Si ocurre un error durante la creación del pedido.
     */
    async createPedido(pedidoData: PedidoRequestDTO, token: string): Promise<Pedido> {
        setAuthToken(token);
        try {
            // El endpoint '/pedidos' de tu backend espera un PedidoRequestDTO.
            // La respuesta debería ser el Pedido completo una vez creado.
            const response = await apiClient.post<Pedido>('/pedidos', pedidoData);
            return response.data;
        } catch (error) {
            console.error('Error al crear pedido:', error);
            throw error;
        }
    }

    /**
     * @function createPreferenceMercadoPago
     * @description Envía los datos del pedido al backend para crear una preferencia de pago en Mercado Pago.
     * @param {PedidoRequestDTO} pedidoData - El objeto del pedido a procesar para Mercado Pago (en formato DTO).
     * @param {string} token - El token JWT para la autenticación.
     * @returns {Promise<string | null>} Una promesa que resuelve con el ID de la preferencia de Mercado Pago o null si falla.
     * @throws {Error} Si ocurre un error durante la creación de la preferencia.
     */
    async createPreferenceMercadoPago(pedidoData: PedidoRequestDTO, token: string): Promise<string | null> { // <-- Cambiado 'pedido: Pedido' a 'pedidoData: PedidoRequestDTO'
        setAuthToken(token);
        try {
            // Asume que tu backend tiene un endpoint /pedidos/create_preference_mp que espera un PedidoRequestDTO
            // y devuelve un objeto con la propiedad 'id' (el ID de la preferencia de MP)
            const response = await apiClient.post<PreferenceMP>(`/pedidos/create_preference_mp`, pedidoData); // <-- Ahora pasa pedidoData
            return response.data.id;
        } catch (error) {
            console.error('Error creating Mercado Pago preference:', error);
            throw error;
        }
    }
}