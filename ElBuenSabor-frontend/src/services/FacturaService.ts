import apiClient from './apiClient';
import type { FacturaCreateRequest, FacturaResponse } from '../types/types';

export class FacturaService {
  /**
   * Obtiene todas las facturas activas.
   */
  static async getAllActivas(): Promise<FacturaResponse[]> {
    const response = await apiClient.get<FacturaResponse[]>('/facturas/activas');
    return response.data;
  }
  
  /**
   * Obtiene todas las facturas, incluyendo las anuladas.
   */
  static async getAllIncludingAnuladas(): Promise<FacturaResponse[]> {
    const response = await apiClient.get<FacturaResponse[]>('/facturas');
    return response.data;
  }
  
  /**
   * Obtiene una factura activa por su ID.
   * @param id - El ID de la factura.
   */
  static async findByIdActiva(id: number): Promise<FacturaResponse> {
    const response = await apiClient.get<FacturaResponse>(`/facturas/${id}/activa`);
    return response.data;
  }

  /**
   * Obtiene una factura por su ID, incluyendo anuladas.
   * @param id - El ID de la factura.
   */
  static async findByIdIncludingAnuladas(id: number): Promise<FacturaResponse> {
    const response = await apiClient.get<FacturaResponse>(`/facturas/${id}`);
    return response.data;
  }

  /**
   * Genera una nueva factura a partir de un pedido.
   * @param data - Contiene el ID del pedido.
   */
  static async generarDesdePedido(data: FacturaCreateRequest): Promise<FacturaResponse> {
    const response = await apiClient.post<FacturaResponse>('/facturas/generar-desde-pedido', data);
    return response.data;
  }

  /**
   * Anula una factura existente.
   * @param id - El ID de la factura a anular.
   */
  static async anularFactura(id: number): Promise<FacturaResponse> {
    const response = await apiClient.post<FacturaResponse>(`/facturas/anular/${id}`);
    return response.data;
  }
}