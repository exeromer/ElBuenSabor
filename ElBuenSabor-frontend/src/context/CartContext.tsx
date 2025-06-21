// src/context/CartContext.tsx

import React, { createContext, useContext, useState, type ReactNode, useEffect, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useUser } from './UserContext';
import { CarritoService } from '../services/CarritoService';
import { ArticuloService } from '../services/ArticuloService';
import type { ArticuloResponse, CarritoResponse } from '../types/types';

// Tipo enriquecido para el estado local
export interface EnrichedCartItem {
  id: number; // ID del CarritoItem
  articulo: ArticuloResponse;
  quantity: number;
  subtotal: number;
}

// Interfaz del Contexto, AHORA INCLUYE EL ESTADO DEL MODAL
interface CartContextType {
  cart: EnrichedCartItem[];
  totalPrice: number;
  totalItems: number;
  isLoading: boolean;
  error: string | null;
  isCartOpen: boolean;
  openCart: () => void;
  closeCart: () => void;
  addToCart: (articulo: ArticuloResponse, quantity?: number) => Promise<void>;
  removeFromCart: (cartItemId: number) => Promise<void>;
  updateQuantity: (cartItemId: number, newQuantity: number) => Promise<void>;
  clearCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth0();
  const { cliente, isLoading: isUserLoading } = useUser();

  const [cart, setCart] = useState<EnrichedCartItem[]>([]);
  const [totalPrice, setTotalPrice] = useState<number>(0);
  const [totalItems, setTotalItems] = useState<number>(0);
  const [isCartOpen, setIsCartOpen] = useState<boolean>(false); // <-- Estado para el modal
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const enrichCartItems = useCallback(async (backendCart: CarritoResponse): Promise<EnrichedCartItem[]> => {
    if (!backendCart.items || backendCart.items.length === 0) return [];
    try {
      const promises = backendCart.items.map(item => ArticuloService.getById(item.articuloId));
      const articulosDetallados = await Promise.all(promises);
      const articulosMap = new Map(articulosDetallados.map(art => [art.id, art]));
      return backendCart.items.map(item => ({
        id: item.id,
        quantity: item.cantidad,
        subtotal: item.subtotalItem,
        articulo: articulosMap.get(item.articuloId)!,
      })).filter(item => item.articulo);
    } catch (err) {
      console.error("Error al enriquecer el carrito:", err);
      setError("No se pudieron cargar los detalles de los productos.");
      return [];
    }
  }, []);

  const handleCartUpdate = useCallback(async (updatedBackendCart: CarritoResponse) => {
    const richItems = await enrichCartItems(updatedBackendCart);
    setCart(richItems);
    setTotalPrice(updatedBackendCart.totalCarrito);
    const totalItemCount = richItems.reduce((sum, item) => sum + item.quantity, 0);
    setTotalItems(totalItemCount);
  }, [enrichCartItems]);

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated || !cliente?.id) {
      setCart([]);
      setTotalPrice(0);
      setTotalItems(0);
      return;
    }
    setIsLoading(true);
    try {
      const fetchedBackendCart = await CarritoService.getCarrito(cliente.id);
      await handleCartUpdate(fetchedBackendCart);
    } catch (err) {
      console.error("Error al obtener el carrito:", err);
      setError("No se pudo cargar el carrito.");
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated, cliente, handleCartUpdate]);

  useEffect(() => {
    if (!isUserLoading) {
      fetchCart();
    }
  }, [isUserLoading, fetchCart]);

  const executeCartAction = async (action: () => Promise<CarritoResponse>) => {
    setIsLoading(true);
    try {
      const updatedBackendCart = await action();
      await handleCartUpdate(updatedBackendCart);
    } catch (err) {
      console.error("Error en operación del carrito:", err);
      setError("No se pudo actualizar el carrito.");
    } finally {
      setIsLoading(false);
    }
  };

  const addToCart = async (articulo: ArticuloResponse, quantity: number = 1) => {
    if (!cliente?.id || !articulo.id) return;
    await executeCartAction(() =>
      CarritoService.addItem(cliente.id!, { articuloId: articulo.id!, cantidad: quantity })
    );
  };

  const updateQuantity = async (cartItemId: number, newQuantity: number) => {
    if (!cliente?.id) return;
    if (newQuantity > 0) {
      await executeCartAction(() =>
        CarritoService.updateItemQuantity(cliente.id!, cartItemId, { nuevaCantidad: newQuantity })
      );
    } else {
      await removeFromCart(cartItemId);
    }
  };

  const removeFromCart = async (cartItemId: number) => {
    if (!cliente?.id) return;
    await executeCartAction(() => CarritoService.deleteItem(cliente.id!, cartItemId));
  };

  const clearCart = async () => {
    if (!cliente?.id) return;
    await executeCartAction(() => CarritoService.clear(cliente.id!));
  };

  // Funciones para controlar el modal
  const openCart = () => setIsCartOpen(true);
  const closeCart = () => setIsCartOpen(false);

  return (
    <CartContext.Provider value={{
      cart,
      totalPrice,
      totalItems,
      isLoading,
      error,
      isCartOpen, // <-- Se comparte con los componentes
      openCart,   // <-- Se comparte con los componentes
      closeCart,  // <-- Se comparte con los componentes
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart,
    }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart debe ser usado dentro de un CartProvider');
  }
  return context;
};