import React, { createContext, useState, useEffect, useContext, type ReactNode, useCallback } from 'react';
import type { SucursalResponse } from '../types/types';
import { SucursalService } from '../services/sucursalService';


// 1. Definición del tipo para el contexto
interface SucursalContextType {
  sucursales: SucursalResponse[]; // CAMBIO: Usamos SucursalResponse
  selectedSucursal: SucursalResponse | null; // CAMBIO: Usamos SucursalResponse
  selectSucursal: (sucursalId: number) => void;
  loading: boolean;
  reloadSucursales: () => void;
}

// 2. Creación del Contexto
export const SucursalContext = createContext<SucursalContextType | undefined>(undefined);

// 3. Creación del Proveedor del Contexto (Provider)
interface SucursalProviderProps {
  children: ReactNode;
}

export const SucursalProvider: React.FC<SucursalProviderProps> = ({ children }) => {

  const [sucursales, setSucursales] = useState<SucursalResponse[]>([]);
  const [selectedSucursal, setSelectedSucursal] = useState<SucursalResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

    const fetchSucursales = useCallback(async () => {
    setLoading(true);
    try {
      const data = await SucursalService.getAll();
      setSucursales(data);
      // Mantenemos la lógica de selección, pero con cuidado
      if (data.length > 0) {
        // Si ya hay una sucursal seleccionada, la mantenemos, si no, seleccionamos la primera
        setSelectedSucursal(prev => data.find(s => s.id === prev?.id) || data[0]);
      }
    } catch (error) {
      console.error("Error al obtener las sucursales:", error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSucursales();
  }, [fetchSucursales]);

  // Función para cambiar la sucursal seleccionada
  const selectSucursal = (sucursalId: number) => {
    const nuevaSucursal = sucursales.find(s => s.id === sucursalId);

    // Verificamos si la sucursal existe y es diferente a la actual
    if (nuevaSucursal) {
      setSelectedSucursal(nuevaSucursal);
    }
  };

  const value = {
    sucursales,
    selectedSucursal,
    selectSucursal,
    loading,
    reloadSucursales: fetchSucursales
  };

  return (
    <SucursalContext.Provider value={value}>
      {children}
    </SucursalContext.Provider>
  );
};

// 4. Hook personalizado para usar el contexto fácilmente
export const useSucursal = () => {
  const context = useContext(SucursalContext);
  if (context === undefined) {
    throw new Error('useSucursal debe ser usado dentro de un SucursalProvider');
  }
  return context;
};