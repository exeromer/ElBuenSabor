import apiClient from '../services/apiClient';
import type { UsuarioRequest, UsuarioResponse } from '../types/types';

export class UsuarioService {
  /**
   * Crea un nuevo usuario.
   * @param data - Los datos del usuario a crear.
   */
  static async create(data: UsuarioRequest): Promise<UsuarioResponse> {
    const response = await apiClient.post<UsuarioResponse>('/usuarios', data);
    return response.data;
  }

  /**
   * Obtiene todos los usuarios, con filtro opcional.
   * @param searchTerm - Término de búsqueda.
   */
  static async getAll(searchTerm?: string): Promise<UsuarioResponse[]> {
    const response = await apiClient.get<UsuarioResponse[]>('/usuarios', {
      params: { searchTerm },
    });
    return response.data;
  }

  /**
   * Obtiene un usuario por su ID.
   * @param id - El ID del usuario.
   */
  static async getById(id: number): Promise<UsuarioResponse> {
    const response = await apiClient.get<UsuarioResponse>(`/usuarios/${id}`);
    return response.data;
  }

  /**
   * Obtiene un usuario por su nombre de usuario.
   * @param username - El nombre de usuario.
   */
  static async getByUsername(username: string): Promise<UsuarioResponse> {
    const response = await apiClient.get<UsuarioResponse>(`/usuarios/username/${username}`);
    return response.data;
  }

  /**
   * Obtiene un usuario por su Auth0 ID.
   * @param auth0Id - El ID de Auth0.
   */
  static async getByAuth0Id(auth0Id: string): Promise<UsuarioResponse> {
    const response = await apiClient.get<UsuarioResponse>(`/usuarios/auth0/${auth0Id}`);
    return response.data;
  }
  
  /**
   * Actualiza un usuario.
   * @param id - El ID del usuario.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: UsuarioRequest): Promise<UsuarioResponse> {
    const response = await apiClient.put<UsuarioResponse>(`/usuarios/${id}`, data);
    return response.data;
  }

  /**
   * Realiza un borrado lógico de un usuario.
   * @param id - El ID del usuario.
   */
  static async delete(id: number): Promise<{ mensaje: string }> {
    const response = await apiClient.delete<{ mensaje: string }>(`/usuarios/${id}`);
    return response.data;
  }
}