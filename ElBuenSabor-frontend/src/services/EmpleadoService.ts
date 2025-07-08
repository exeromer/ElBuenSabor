import apiClient from './apiClient';
import type { EmpleadoResponse } from '../types/types';

export class EmpleadoService {
  /**
   * Obtiene un empleado por el ID de su usuario asociado.
   * Este endpoint es clave para obtener el perfil de un empleado logueado.
   * @param usuarioId - El ID del Usuario.
   */
  static async getByUsuarioId(usuarioId: number): Promise<EmpleadoResponse> {
    const response = await apiClient.get<EmpleadoResponse>(`/empleados/usuario/${usuarioId}`);
    return response.data;
  }
}