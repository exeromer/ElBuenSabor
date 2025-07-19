import axios from 'axios';
import apiClient from './apiClient';
import type { PaisResponse, ProvinciaResponse, LocalidadResponse, GeorefLocalidad } from '../types/types';

export class UbicacionService {
  async getAllPaises(): Promise<PaisResponse[]> {
    const response = await apiClient.get<PaisResponse[]>('/paises');
    return response.data;
  }

  async getAllProvincias(): Promise<ProvinciaResponse[]> {
    const response = await apiClient.get<ProvinciaResponse[]>('/provincias');
    return response.data;
  }

   static async getLocalidadesPorProvincia(provinciaNombre: string): Promise<GeorefLocalidad[]> {
    try {
      const response = await axios.get<{ localidades: GeorefLocalidad[] }>(
        `https://apis.datos.gob.ar/georef/api/localidades`, {
          params: {
            provincia: provinciaNombre,
            campos: 'id,nombre',
            max: 1000 // Traer hasta 1000 localidades por provincia
          }
        }
      );
      // Ordenamos alfabéticamente para una mejor experiencia de usuario
      return response.data.localidades.sort((a, b) => a.nombre.localeCompare(b.nombre));
    } catch (error) {
      console.error('Error al obtener localidades desde Georef:', error);
      throw new Error('No se pudieron cargar las localidades.');
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
