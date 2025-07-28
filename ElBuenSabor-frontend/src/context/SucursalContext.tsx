import React, { createContext, useState, useEffect, useContext, type ReactNode, useCallback } from 'react';
import type { SucursalResponse } from '../types/types';
import { SucursalService } from '../services/sucursalService';


interface SucursalContextType {
  sucursales: SucursalResponse[];
  selectedSucursal: SucursalResponse | null;
  selectSucursal: (sucursalId: number) => void;
  loading: boolean;
  reloadSucursales: () => void;
}

export const SucursalContext = createContext<SucursalContextType | undefined>(undefined);

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
      if (data.length > 0) {
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

  const selectSucursal = (sucursalId: number) => {
    const nuevaSucursal = sucursales.find(s => s.id === sucursalId);

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

export const useSucursal = () => {
  const context = useContext(SucursalContext);
  if (context === undefined) {
    throw new Error('useSucursal debe ser usado dentro de un SucursalProvider');
  }
  return context;
};