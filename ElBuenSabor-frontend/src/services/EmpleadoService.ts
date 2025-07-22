import apiClient from './apiClient';
import type { EmpleadoResponse, EmpleadoRequest } from '../types/types'; 

export class EmpleadoService {
  /**
   * Obtiene todos los empleados.
   */
  static async getAll(): Promise<EmpleadoResponse[]> {
    const response = await apiClient.get<EmpleadoResponse[]>('/empleados');
    return response.data;
  }

  /**
   * Obtiene un empleado por su ID.
   * @param id - El ID del empleado.
   */
  static async getById(id: number): Promise<EmpleadoResponse> {
    const response = await apiClient.get<EmpleadoResponse>(`/empleados/${id}`);
    return response.data;
  }

  /**
   * Crea un nuevo empleado.
   * @param data - Los datos del empleado a crear.
   */
  static async create(data: EmpleadoRequest): Promise<EmpleadoResponse> {
    const response = await apiClient.post<EmpleadoResponse>('/empleados', data);
    return response.data;
  }

  /**
   * Actualiza un empleado existente.
   * @param id - El ID del empleado a actualizar.
   * @param data - Los nuevos datos del empleado.
   */
  static async update(id: number, data: EmpleadoRequest): Promise<EmpleadoResponse> {
    const response = await apiClient.put<EmpleadoResponse>(`/empleados/${id}`, data);
    return response.data;
  }
    /**
   * Actualiza el perfil del empleado autenticado.
   */
  static async updateMiPerfil(data: Partial<EmpleadoRequest>): Promise<EmpleadoResponse> {
    const response = await apiClient.put<EmpleadoResponse>('/empleados/perfil', data);
    return response.data;
  }
  
  /**
   * Realiza una baja lógica de un empleado.
   * @param id - El ID del empleado.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/empleados/${id}`);
  }

  /**
   * Obtiene un empleado por el ID de su usuario asociado.
   */
  static async getByUsuarioId(usuarioId: number): Promise<EmpleadoResponse> {
    const response = await apiClient.get<EmpleadoResponse>(`/empleados/usuario/${usuarioId}`);
    return response.data;
  }

  /**
   * Obtiene el perfil del empleado actualmente autenticado.
   */
  static async getMiPerfil(): Promise<EmpleadoResponse> {
    const response = await apiClient.get<EmpleadoResponse>('/empleados/perfil');
    return response.data;
  }
}