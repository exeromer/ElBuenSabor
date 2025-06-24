import React, { createContext, useState, useEffect, useContext, type ReactNode } from 'react';
import type { SucursalResponse } from '../types/types';
import { SucursalService } from '../services/sucursalService';
import { useAuth0 } from '@auth0/auth0-react';
import { setAuthToken } from '../services/apiClient';


// 1. Definición del tipo para el contexto
interface SucursalContextType {
  sucursales: SucursalResponse[]; // CAMBIO: Usamos SucursalResponse
  selectedSucursal: SucursalResponse | null; // CAMBIO: Usamos SucursalResponse
  selectSucursal: (sucursalId: number) => void;
  loading: boolean;
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

  useEffect(() => {
    const fetchSucursales = async () => {
      try {
        const data = await SucursalService.getAll();
        setSucursales(data);
        if (data.length > 0) {
          setSelectedSucursal(data[0]);
        }
      } catch (error) {
        console.error("Error al obtener las sucursales:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchSucursales();
  }, []);

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
    loading
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