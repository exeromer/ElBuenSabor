/**
 * @file CartContext.tsx
 * @description Contexto de React para gestionar el estado global del carrito de compras.
 * Obtiene el carrito "ligero" del backend y lo enriquece con los detalles completos de cada artículo
 * para que los componentes del UI tengan toda la información necesaria (imágenes, tiempos, etc.).
 * Todas las operaciones (añadir, eliminar, etc.) se sincronizan con la API del backend.
 */
import React, { createContext, useContext, useState, type ReactNode, useEffect, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useUser } from './UserContext';
import { CarritoService } from '../services/carritoService';
import { ArticuloManufacturadoService } from '../services/articuloManufacturadoService'; 
import type { ArticuloManufacturado, CarritoResponseDTO, CartItem } from '../types/types';

interface CartContextType {
  cart: CartItem[];
  isLoading: boolean;
  error: string | null;
  addToCart: (item: ArticuloManufacturado, quantity?: number) => Promise<void>;
  removeFromCart: (itemId: number) => Promise<void>;
  updateQuantity: (itemId: number, newQuantity: number) => Promise<void>;
  clearCart: () => Promise<void>;
  getCartItemCount: () => number;
  getItemQuantity: (articuloId: number) => number;
  getCartTotal: () => number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

const carritoService = new CarritoService();
const articuloManufacturadoService = new ArticuloManufacturadoService(); 

export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { getAccessTokenSilently, isAuthenticated } = useAuth0();
  const { cliente, isLoading: isUserLoading } = useUser();

  const [cart, setCart] = useState<CartItem[]>([]);
  const [backendCart, setBackendCart] = useState<CarritoResponseDTO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const enrichCartItems = useCallback(async (lightCart: CarritoResponseDTO): Promise<CartItem[]> => {
    if (!lightCart.items || lightCart.items.length === 0) {
      return [];
    }
  
    // 1. Obtener todos los IDs de artículos que necesitamos buscar
    const articleIds = lightCart.items.map(item => item.articuloId);
    if (articleIds.length === 0) return [];

    // 2. Crear un array de promesas para obtener los detalles de cada artículo
    const promises = articleIds.map(id =>
      articuloManufacturadoService.getArticuloManufacturadoById(id)
    );
  
    try {
      // 3. Esperar a que todas las búsquedas de detalles se completen
      const articulosDetallados = await Promise.all(promises);
      
      // 4. Crear un mapa para buscar fácilmente los detalles por ID
      const articulosMap = new Map<number, ArticuloManufacturado>();
      articulosDetallados.forEach(art => {
        if (art && art.id) {
          articulosMap.set(art.id, art);
        }
      });
  
      // 5. Construir el array "enriquecido" de forma segura
      const richCartItems: CartItem[] = [];
      lightCart.items.forEach(item => {
        // Solo incluimos el ítem si tiene un ID y hemos encontrado sus detalles
        if (item.id !== undefined && articulosMap.has(item.articuloId)) {
          richCartItems.push({
            id: item.id, // El ID ahora es un número garantizado
            articulo: articulosMap.get(item.articuloId)!, // El '!' es seguro por el .has()
            quantity: item.cantidad,
          });
        }
      });
  
      return richCartItems;
    } catch (err) {
      console.error("Error al enriquecer el carrito:", err);
      setError("No se pudieron cargar los detalles completos de los productos en el carrito.");
      return [];
    }
  }, []);

  const fetchAndSetCart = useCallback(async () => {
    if (!isAuthenticated || !cliente?.id) {
      setCart([]);
      setBackendCart(null);
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    setError(null);

    try {
      const token = await getAccessTokenSilently();
      const fetchedBackendCart = await carritoService.getCart(cliente.id, token);
      setBackendCart(fetchedBackendCart);
      const richItems = await enrichCartItems(fetchedBackendCart);
      setCart(richItems);
    } catch (err) {
      console.error("Error al obtener el carrito:", err);
      setError("No se pudo cargar el carrito.");
      setCart([]);
      setBackendCart(null);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, cliente, getAccessTokenSilently, enrichCartItems]);

  useEffect(() => {
    if (!isUserLoading) {
      fetchAndSetCart();
    }
  }, [isUserLoading, fetchAndSetCart]);
  
  const handleApiAndUpdateState = async (apiCall: () => Promise<CarritoResponseDTO>) => {
    setIsLoading(true);
    try {
      const updatedBackendCart = await apiCall();
      setBackendCart(updatedBackendCart);
      const richItems = await enrichCartItems(updatedBackendCart);
      setCart(richItems);
    } catch (err) {
      console.error("Error en operación del carrito:", err);
      setError("No se pudo actualizar el carrito.");
    } finally {
      setIsLoading(false);
    }
  };

  const addToCart = async (item: ArticuloManufacturado, quantity: number = 1) => {
    if (!cliente?.id || !item.id) {
      setError("Debes iniciar sesión para añadir productos.");
      return;
    }
    const token = await getAccessTokenSilently();
    await handleApiAndUpdateState(() => carritoService.addItem(cliente.id!, { articuloId: item.id!, cantidad: quantity }, token));
  };
  
  const updateQuantity = async (itemId: number, newQuantity: number) => {
    if (!cliente?.id) return;
    const token = await getAccessTokenSilently();
    if (newQuantity > 0) {
      await handleApiAndUpdateState(() => carritoService.updateItemQuantity(cliente.id!, itemId, { nuevaCantidad: newQuantity }, token));
    } else {
      await handleApiAndUpdateState(() => carritoService.removeItem(cliente.id!, itemId, token));
    }
  };

  const removeFromCart = async (itemId: number) => {
    await updateQuantity(itemId, 0);
  };
  
  const clearCart = async () => {
    if (!cliente?.id) return;
    const token = await getAccessTokenSilently();
    await handleApiAndUpdateState(() => carritoService.clearCart(cliente.id!, token));
  };

  const getItemQuantity = (articuloId: number): number => {
    const item = cart.find(cartItem => cartItem.articulo.id === articuloId);
    return item ? item.quantity : 0;
  };
  
  const getCartItemCount = (): number => {
    return cart.reduce((total, item) => total + item.quantity, 0);
  };

  const getCartTotal = (): number => {
    return backendCart?.totalCarrito ?? 0;
  };

  return (
    <CartContext.Provider value={{ cart, isLoading, error, addToCart, removeFromCart, updateQuantity, clearCart, getCartItemCount, getItemQuantity, getCartTotal }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};