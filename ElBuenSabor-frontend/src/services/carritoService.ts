import apiClient from '../services/apiClient';
import type { AddItemToCartRequest, CarritoResponse, UpdateCartItemQuantityRequest } from '../types/types';

export class CarritoService {
  private static getBaseUrl(clienteId: number): string {
    return `/clientes/${clienteId}/carrito`;
  }

  /**
   * Obtiene o crea el carrito para un cliente.
   * @param clienteId - El ID del cliente.
   */
  static async getCarrito(clienteId: number): Promise<CarritoResponse> {
    const response = await apiClient.get<CarritoResponse>(this.getBaseUrl(clienteId));
    return response.data;
  }

  /**
   * Agrega un ítem al carrito del cliente.
   * @param clienteId - El ID del cliente.
   * @param itemData - Los datos del ítem a agregar.
   */
  static async addItem(clienteId: number, itemData: AddItemToCartRequest): Promise<CarritoResponse> {
    const response = await apiClient.post<CarritoResponse>(`${this.getBaseUrl(clienteId)}/items`, itemData);
    return response.data;
  }

  /**
   * Actualiza la cantidad de un ítem en el carrito.
   * @param clienteId - El ID del cliente.
   * @param carritoItemId - El ID del ítem en el carrito.
   * @param data - La nueva cantidad.
   */
  static async updateItemQuantity(clienteId: number, carritoItemId: number, data: UpdateCartItemQuantityRequest): Promise<CarritoResponse> {
    const response = await apiClient.put<CarritoResponse>(`${this.getBaseUrl(clienteId)}/items/${carritoItemId}`, data);
    return response.data;
  }
  
  /**
   * Elimina un ítem del carrito.
   * @param clienteId - El ID del cliente.
   * @param carritoItemId - El ID del ítem a eliminar.
   */
  static async deleteItem(clienteId: number, carritoItemId: number): Promise<CarritoResponse> {
    const response = await apiClient.delete<CarritoResponse>(`${this.getBaseUrl(clienteId)}/items/${carritoItemId}`);
    return response.data;
  }

  /**
   * Vacía todos los ítems del carrito de un cliente.
   * @param clienteId - El ID del cliente.
   */
  static async clear(clienteId: number): Promise<CarritoResponse> {
    const response = await apiClient.delete<CarritoResponse>(`${this.getBaseUrl(clienteId)}/items`);
    return response.data;
  }
}