import apiClient from './apiClient';
import type { PaisResponse, ProvinciaResponse, LocalidadResponse } from '../types/types';

export class UbicacionService { 
  async getAllPaises(): Promise<PaisResponse[]> {
    try {
      const response = await apiClient.get<PaisResponse[]>('/paises');
      return response.data;
    } catch (error) {
      console.error('Error al obtener países:', error);
      throw error;
    }
  }

  async getAllProvincias(): Promise<ProvinciaResponse[]> {
    try {
      const response = await apiClient.get<ProvinciaResponse[]>('/provincias');
      return response.data;
    } catch (error) {
      console.error('Error al obtener provincias:', error);
      throw error;
    }
  }
  async getAllLocalidades(): Promise<LocalidadResponse[]> {
    try {
      const response = await apiClient.get<LocalidadResponse[]>('/localidades');
      return response.data;
    } catch (error) {
      console.error('Error al obtener localidades:', error);
      throw error;
    }
  }
}
