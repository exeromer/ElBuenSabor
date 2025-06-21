import apiClient from '../services/apiClient';
import type { ClienteRequest, ClienteResponse } from '../types/types';

export class ClienteService {
  /**
   * Crea un nuevo cliente.
   * @param data - Los datos del cliente a crear.
   */
  static async create(data: ClienteRequest): Promise<ClienteResponse> {
    const response = await apiClient.post<ClienteResponse>('/clientes', data);
    return response.data;
  }

  /**
   * Obtiene todos los clientes, con filtro opcional por término de búsqueda.
   * @param searchTerm - Término para buscar en nombre, apellido o email.
   */
  static async getAll(searchTerm?: string): Promise<ClienteResponse[]> {
    const response = await apiClient.get<ClienteResponse[]>('/clientes', {
      params: { searchTerm }
    });
    return response.data;
  }

  /**
   * Obtiene el perfil del cliente autenticado.
   */
  static async getMiPerfil(): Promise<ClienteResponse> {
    const response = await apiClient.get<ClienteResponse>('/clientes/perfil');
    return response.data;
  }

  /**
   * Obtiene un cliente por su ID.
   * @param id - El ID del cliente.
   */
  static async getById(id: number): Promise<ClienteResponse> {
    const response = await apiClient.get<ClienteResponse>(`/clientes/${id}`);
    return response.data;
  }

  /**
   * Actualiza un cliente existente.
   * @param id - El ID del cliente a actualizar.
   * @param data - Los nuevos datos del cliente.
   */
  static async update(id: number, data: ClienteRequest): Promise<ClienteResponse> {
    const response = await apiClient.put<ClienteResponse>(`/clientes/${id}`, data);
    return response.data;
  }

  /**
   * Realiza un borrado lógico de un cliente.
   * @param id - El ID del cliente.
   */
  static async delete(id: number): Promise<{ mensaje: string }> {
    const response = await apiClient.delete<{ mensaje: string }>(`/clientes/${id}`);
    return response.data;
  }
}