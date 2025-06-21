import React, { createContext, useState, useEffect, useContext, type ReactNode } from 'react';
import type { SucursalResponse } from '../types/types';
import { SucursalService } from '../services/SucursalService';
import { useCart } from './CartContext';

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
  const [sucursales, setSucursales] = useState<SucursalResponse[]>([]); // CAMBIO: Usamos SucursalResponse
  const [selectedSucursal, setSelectedSucursal] = useState<SucursalResponse | null>(null); // CAMBIO: Usamos SucursalResponse
  const [loading, setLoading] = useState<boolean>(true);
  const { clearCart } = useCart();

  // Efecto para cargar las sucursales cuando el componente se monta
  useEffect(() => {
    const fetchSucursales = async () => {
      try {
        const data = await SucursalService.getAll();
        setSucursales(data);
        
        // Lógica para seleccionar la primera sucursal por defecto
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
    if (nuevaSucursal && nuevaSucursal.id !== selectedSucursal?.id) {
      
      const userConfirmation = window.confirm(
        "Al cambiar de sucursal, tu carrito de compras se vaciará. ¿Deseas continuar?"
      );

      if (userConfirmation) {
        setSelectedSucursal(nuevaSucursal);
        clearCart(); // Limpiamos el carrito para evitar inconsistencias
      }
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