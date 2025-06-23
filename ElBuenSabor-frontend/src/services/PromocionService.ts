import apiClient from './apiClient';
// Asegúrate de que estos tipos existan y sean correctos en types.ts
import type { PromocionResponse, PromocionDetalleRequest} from '../types/types'; 
import type { TipoPromocion } from '../types/enums';

// Tipo para crear o actualizar una promoción, basado en el DTO del backend.
type PromocionCreateOrUpdate = {
    denominacion: string;
    fechaDesde: string; // formato "YYYY-MM-DD"
    fechaHasta: string; // formato "YYYY-MM-DD"
    horaDesde: string; // formato "HH:mm:ss"
    horaHasta: string; // formato "HH:mm:ss"
    descripcionDescuento?: string;
    precioPromocional?: number;
    tipoPromocion: TipoPromocion; // Asumiendo que tienes un enum para esto
    porcentajeDescuento?: number;
    imagenIds?: number[];
    detallesPromocion: PromocionDetalleRequest[];
    estadoActivo: boolean;
    sucursalIds: number[];
};

export class PromocionService {
  /**
   * Obtiene todas las promociones (sin filtrar).
   */
  static async getAll(): Promise<PromocionResponse[]> {
    const response = await apiClient.get<PromocionResponse[]>('/promociones');
    return response.data;
  }

  /**
   * Obtiene todas las promociones de una sucursal específica.
   * @param id - El ID de la sucursal.
   */
  static async getBySucursalId(id: number): Promise<PromocionResponse[]> {
    const response = await apiClient.get<PromocionResponse[]>(`/promociones/sucursal/${id}`);
    return response.data;
  }

  
  /**
   * Crea una nueva promoción.
   * @param data - Los datos de la promoción a crear.
   */
  static async create(data: PromocionCreateOrUpdate): Promise<PromocionResponse> {
    const response = await apiClient.post<PromocionResponse>('/promociones', data);
    return response.data;
  }

  /**
   * Actualiza una promoción.
   * @param id - El ID de la promoción a actualizar.
   * @param data - Los nuevos datos.
   */
  static async update(id: number, data: PromocionCreateOrUpdate): Promise<PromocionResponse> {
    const response = await apiClient.put<PromocionResponse>(`/promociones/${id}`, data);
    return response.data;
  }

  /**
   * Elimina una promoción.
   * @param id - El ID de la promoción a eliminar.
   */
  static async delete(id: number): Promise<void> {
    await apiClient.delete(`/promociones/${id}`);
  }
}