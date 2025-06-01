// src/hooks/useSearchableData.ts
import React, { useState, useEffect, useCallback } from 'react';
// Asegúrate que la ruta a tu función debounce sea correcta.
// Si la definiste en `src/components/utils/Functions/debounce.ts` como parece en tu ManageProductsPage.txt
import { debounce } from '../components/utils/Functions/debounce';
// Definición de la dirección del ordenamiento
type SortDirection = 'ascending' | 'descending';

// Interfaz para la configuración del ordenamiento
// Usamos 'keyof T' para asegurar que la clave de ordenamiento sea una propiedad válida del tipo T
// o 'string' si necesitamos más flexibilidad (ej. para propiedades anidadas, aunque eso complica el sort).
export interface SortConfig<T> {
  key: keyof T | string; 
  direction: SortDirection;
}

interface FetchDataFunction<T> {
  (searchTerm: string): Promise<T[]>;
}

/**
 * @interface FetchDataFunction
 * @description Firma de la función que el hook usará para obtener los datos (ya filtrados por el backend).
 * @template T - El tipo de los ítems en la lista.
 * @param {string} searchTerm - El término de búsqueda actual.
 * @returns {Promise<T[]>} Una promesa que resuelve con un array de ítems.
 */
interface FetchDataFunction<T> {
  (searchTerm: string): Promise<T[]>;
}

/**
 * @interface UseSearchableDataProps
 * @description Props para el hook useSearchableData.
 * @template T
 * @property {FetchDataFunction<T>} fetchData - Función para obtener los datos.
 * @property {number} [initialPageSize=10] - Tamaño de página inicial.
 * @property {number} [debounceTime=500] - Tiempo de debounce para la búsqueda.
 */
export interface UseSearchableDataProps<T> {
  fetchData: FetchDataFunction<T>;
  debounceTime?: number;
}

/**
 * @hook useSearchableData
 * @description Hook genérico para manejar la lógica de búsqueda y paginación del lado del cliente.
 * @template T - El tipo de los ítems.
 */
export function useSearchableData<T>({
  fetchData,
  debounceTime = 500
}: UseSearchableDataProps<T>) {
  const [allItems, setAllItems] = useState<T[]>([]); // Todos los ítems después de la búsqueda del backend
  const [searchTerm, setSearchTerm] = useState('');
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [sortConfig, setSortConfig] = useState<SortConfig<T> | null>(null);

  /**
   * @function debouncedFetch
   * @description Función "debounced" para obtener datos del backend.
   */
  const debouncedFetch = useCallback(
    debounce(async (currentSearchTerm: string) => {
      setIsLoading(true);
      setError(null);
      try {
        const result = await fetchData(currentSearchTerm);
        setAllItems(result); // Directamente en allItems
        setTotalItems(result.length);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Error al cargar datos');
        setAllItems([]);
        setTotalItems(0);
        console.error("Error en debouncedFetch:", err);
      } finally {
        setIsLoading(false);
      }
    }, debounceTime),
    [fetchData, debounceTime]
  );

  useEffect(() => {
    debouncedFetch(searchTerm);
  }, [searchTerm, debouncedFetch]);
  
  /**
   * @function reload
   * @description Permite recargar los datos manualmente con el término de búsqueda actual.
   * Se envuelve en useCallback para mantener una referencia estable si se pasa a componentes hijos.
   */
  const reload = useCallback(() => {
    debouncedFetch(searchTerm);
  }, [debouncedFetch, searchTerm]);

  /**
   * @function requestSort
   * @description Actualiza la configuración de ordenamiento. Si se ordena por la misma clave, invierte la dirección.
   * @param {keyof T | string} key - La clave del ítem por la cual ordenar.
   */
  const requestSort = (key: keyof T | string) => {
    let direction: SortDirection = 'ascending';
    if (sortConfig && sortConfig.key === key && sortConfig.direction === 'ascending') {
      direction = 'descending';
    }
    // Si quisieras un tercer estado (sin orden) podrías añadir otra condición aquí
    // y setSortConfig(null)
    setSortConfig({ key, direction });
  };

  // --- Lógica para Ordenar y Devolver Items ---
  // Usamos useMemo para que sortedItems solo se recalcule si allItems o sortConfig cambian.
  const sortedItems = React.useMemo(() => {
    let sortableItems = [...allItems]; // Trabajar con una copia
    if (sortConfig !== null) {
      sortableItems.sort((a, b) => {
        // Acceder a los valores de forma segura
        const valA = a[sortConfig.key as keyof T];
        const valB = b[sortConfig.key as keyof T];

        // Comparación básica (puedes extenderla para otros tipos de datos si es necesario)
        if (valA === null || valA === undefined) return sortConfig.direction === 'ascending' ? -1 : 1;
        if (valB === null || valB === undefined) return sortConfig.direction === 'ascending' ? 1 : -1;

        if (typeof valA === 'string' && typeof valB === 'string') {
          return sortConfig.direction === 'ascending'
            ? valA.localeCompare(valB)
            : valB.localeCompare(valA);
        }
        if (typeof valA === 'number' && typeof valB === 'number') {
          return sortConfig.direction === 'ascending' ? valA - valB : valB - valA;
        }
        // Para otros tipos, podrías necesitar lógica de comparación específica o no ordenar
        return 0;
      });
    }
    return sortableItems;

  }, [allItems, sortConfig]);
  return {
    items: sortedItems,
    searchTerm,
    setSearchTerm,
    totalItems,
    isLoading,
    error,
    reload,
    sortConfig,
    sortedItems,
    requestSort,
  };
}


