import apiClient from './apiClient';
import type { ImagenResponse } from '../types/types';

// La respuesta de subida es un objeto complejo
interface UploadResponse {
  message: string;
  filename: string;
  url: string;
  imagenDB: ImagenResponse; // Asumiendo que ImagenResponse está en types.ts
}

// Opciones para la subida de archivos
interface UploadOptions {
  articuloId?: number;
  promocionId?: number;
}

export class FileUploadService {
  /**
   * Sube un único archivo al servidor.
   * @param file - El archivo a subir.
   * @param options - IDs opcionales para asociar la imagen.
   */
  static async uploadFile(file: File, options: UploadOptions = {}): Promise<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (options.articuloId) {
      formData.append('articuloId', String(options.articuloId));
    }
    if (options.promocionId) {
      formData.append('promocionId', String(options.promocionId));
    }

    const response = await apiClient.post<UploadResponse>('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  }

  /**
   * Elimina un archivo del almacenamiento del servidor por su nombre.
   * @param filename - El nombre del archivo a eliminar.
   */
  static async deleteFile(filename: string): Promise<{ message: string }> {
     const response = await apiClient.delete<{ message: string }>(`/files/delete/${filename}`);
     return response.data;
  }
  
  // Nota: uploadMultipleFiles y serveFile no suelen ser llamados directamente como un servicio de clase,
  // el primero se puede implementar con un bucle de `uploadFile` en el frontend,
  // y el segundo es una URL directa para usar en etiquetas <img>.
}