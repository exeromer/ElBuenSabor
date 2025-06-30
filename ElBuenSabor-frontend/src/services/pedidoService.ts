import apiClient from './apiClient';
import type { PedidoRequest, PedidoResponse, CrearPedidoRequest, PedidoEstadoRequest } from '../types/types';
import type { Estado } from '../types/enums';
export class PedidoService {
  /**
   * Crea un pedido para el cliente autenticado.
   * @param data - Los datos del pedido.
   */
  static async create(clienteId: number,data: PedidoRequest): Promise<PedidoResponse> {
    const response = await apiClient.post<PedidoResponse>(`/pedidos/cliente/${clienteId}/desde-carrito`, data);
    return response.data;
  }
  
  /**
   * Crea un pedido como administrador (requiere clienteId en el DTO).
   * @param data - Los datos del pedido, incluyendo clienteId.
   */
  static async createByAdmin(data: PedidoRequest): Promise<PedidoResponse> {
    const response = await apiClient.post<PedidoResponse>('/pedidos/admin', data);
    return response.data;
  }

  /**
   * Obtiene todos los pedidos (ruta de admin).
   */
  static async getAll(): Promise<PedidoResponse[]> {
    const response = await apiClient.get<PedidoResponse[]>('/pedidos');
    return response.data;
  }

  /**
   * Obtiene los pedidos del usuario actualmente autenticado.
   */
  static async getMisPedidos(): Promise<PedidoResponse[]> {
    const response = await apiClient.get<PedidoResponse[]>('/pedidos/mis-pedidos');
    return response.data;
  }
  
  /**
   * Obtiene todos los pedidos de un cliente específico por su ID.
   * @param clienteId - El ID del cliente.
   */
  static async getByClienteId(clienteId: number): Promise<PedidoResponse[]> {
    const response = await apiClient.get<PedidoResponse[]>(`/pedidos/cliente/${clienteId}`);
    return response.data;
  }
  
  /**
   * Obtiene un pedido por su ID.
   * @param id - El ID del pedido.
   */
  static async getById(id: number): Promise<PedidoResponse> {
    const response = await apiClient.get<PedidoResponse>(`/pedidos/${id}`);
    return response.data;
  }
  
  /**
   * Crea un pedido a partir del carrito de un cliente.
   * @param clienteId - El ID del cliente.
   * @param data - Datos adicionales del pedido (dirección, tipo de envío, etc.).
   */
  static async createFromCarrito(clienteId: number, data: CrearPedidoRequest): Promise<PedidoResponse> {
    const response = await apiClient.post<PedidoResponse>(`/pedidos/cliente/${clienteId}/desde-carrito`, data);
    return response.data;
  }

  /**
   * Actualiza el estado de un pedido.
   * @param id - El ID del pedido.
   * @param nuevoEstado - El nuevo estado del pedido.
   */
  static async updateEstado(id: number, nuevoEstado: Estado): Promise<PedidoResponse> {
    const requestData: PedidoEstadoRequest = { nuevoEstado };
    const response = await apiClient.put<PedidoResponse>(`/pedidos/${id}/estado`, requestData);
    return response.data;
  }
  
  /**
   * Realiza un borrado lógico de un pedido.
   * @param id - El ID del pedido.
   */
  static async delete(id: number): Promise<{ mensaje: string }> {
    const response = await apiClient.delete<{ mensaje: string }>(`/pedidos/${id}`);
    return response.data;
  }
}