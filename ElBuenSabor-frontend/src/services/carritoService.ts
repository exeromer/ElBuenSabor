// src/services/carritoService.ts
import apiClient from './apiClient';
import type { CarritoResponseDTO, AddItemToCartRequestDTO, UpdateCartItemQuantityRequestDTO } from '../types/types';

export class CarritoService {

    private getHeaders(token: string) {
        return {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        };
    }

    async getCart(clienteId: number, token: string): Promise<CarritoResponseDTO> {
        const response = await apiClient.get<CarritoResponseDTO>(`/clientes/${clienteId}/carrito`, { headers: this.getHeaders(token) });
        return response.data;
    }

    async addItem(clienteId: number, itemData: AddItemToCartRequestDTO, token: string): Promise<CarritoResponseDTO> {
        const response = await apiClient.post<CarritoResponseDTO>(`/clientes/${clienteId}/carrito/items`, itemData, { headers: this.getHeaders(token) });
        return response.data;
    }

    async updateItemQuantity(clienteId: number, itemId: number, quantityData: UpdateCartItemQuantityRequestDTO, token: string): Promise<CarritoResponseDTO> {
        const response = await apiClient.put<CarritoResponseDTO>(`/clientes/${clienteId}/carrito/items/${itemId}`, quantityData, { headers: this.getHeaders(token) });
        return response.data;
    }

    async removeItem(clienteId: number, itemId: number, token: string): Promise<CarritoResponseDTO> {
        const response = await apiClient.delete<CarritoResponseDTO>(`/clientes/${clienteId}/carrito/items/${itemId}`, { headers: this.getHeaders(token) });
        return response.data;
    }

    async clearCart(clienteId: number, token: string): Promise<CarritoResponseDTO> {
        const response = await apiClient.delete<CarritoResponseDTO>(`/clientes/${clienteId}/carrito/items`, { headers: this.getHeaders(token) });
        return response.data;
    }
}