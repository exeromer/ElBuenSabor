import apiClient from './apiClient';
import type { EmpresaRequest, EmpresaResponse } from '../types/types';

export class EmpresaService {
  /**
   * Crea una nueva empresa.
   * @param data - Los datos de la empresa.
   */
  static async create(data: EmpresaRequest): Promise<EmpresaResponse> {
    const response = await apiClient.post<EmpresaResponse>('/empresas', data);
    return response.data;
  }

  /**
   * Obtiene todas las empresas.
   */
  static async getAll(): Promise<EmpresaResponse[]> {
    const response = await apiClient.get<EmpresaResponse[]>('/empresas');
    return response.data;
  }

  /**
   * Obtiene una empresa por su ID.
   * @param id - El ID de la empresa.
   */
  static async getById(id: number): Promise<EmpresaResponse> {
    const response = await apiClient.get<EmpresaResponse>(`/empresas/${id}`);
    return response.data;
  }

  /**
   * Actualiza una empresa.
   * @param id - El ID de la empresa a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: EmpresaRequest): Promise<EmpresaResponse> {
    const response = await apiClient.put<EmpresaResponse>(`/empresas/${id}`, data);
    return response.data;
  }

  /**
   * Elimina una empresa.
   * @param id - El ID de la empresa.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/empresas/${id}`);
  }
}