// src/context/CartContext.tsx

import React, { createContext, useContext, useState, type ReactNode, useEffect, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useUser } from './UserContext';
import { CarritoService } from '../services/carritoService';
import { ArticuloService } from '../services/ArticuloService';
import { useSucursal } from './SucursalContext';
import { PromocionService } from '../services/PromocionService'; // <-- 2. Importamos el servicio de promociones
import type { ArticuloResponse, CarritoResponse, PromocionResponse, SucursalSimpleResponse } from '../types/types';
import type { TipoEnvio, FormaPago } from '../types/enums';



// Tipo enriquecido para el estado local
export interface EnrichedCartItem {
  id: number; // ID del CarritoItem
  articulo: ArticuloResponse;
  quantity: number;
  subtotal: number;
}

// Interfaz del Contexto
interface CartContextType {
  cart: EnrichedCartItem[];
  subtotal: number; // El subtotal bruto (suma de precios de venta)
  descuento: number; // La suma de TODOS los descuentos aplicados
  totalFinal: number; // El precio final que el cliente pagará
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
  aplicarDescuentosAdicionales: (tipoEnvio: TipoEnvio, formaPago: FormaPago) => void;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth0();
  const { cliente, isLoading: isUserLoading } = useUser();
  const { selectedSucursal } = useSucursal();

  const [cart, setCart] = useState<EnrichedCartItem[]>([]);
  const [promocionesActivas, setPromocionesActivas] = useState<PromocionResponse[]>([]);
  const [subtotal, setSubtotal] = useState<number>(0);
  const [descuento, setDescuento] = useState<number>(0);
  const [totalFinal, setTotalFinal] = useState<number>(0);

  const [totalItems, setTotalItems] = useState<number>(0);
  const [isCartOpen, setIsCartOpen] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const calcularTotales = useCallback((
    currentCart: EnrichedCartItem[],
    promos: PromocionResponse[],
    tipoEnvio?: TipoEnvio,
    formaPago?: FormaPago
  ) => {
    let subtotalBruto = 0;
    let descuentoPromociones = 0;

    currentCart.forEach(item => {
      const precioUnitario = item.articulo.precioVenta;
      subtotalBruto += precioUnitario * item.quantity;

      const promoParaItem = promos.find(p => p.detallesPromocion.some(d => d.articulo.id === item.articulo.id));

      if (promoParaItem) {
        if (promoParaItem.tipoPromocion === 'CANTIDAD' && promoParaItem.precioPromocional) {
          const detallePromo = promoParaItem.detallesPromocion[0];
          if (item.quantity >= detallePromo.cantidad) {
            const gruposDePromo = Math.floor(item.quantity / detallePromo.cantidad);
            const precioOriginalDelGrupo = detallePromo.cantidad * precioUnitario;
            descuentoPromociones += gruposDePromo * (precioOriginalDelGrupo - promoParaItem.precioPromocional);
          }
        }
        // Aquí se puede añadir la lógica para promo de tipo PORCENTAJE
      }
    });

    const subtotalConPromos = subtotalBruto - descuentoPromociones;
    let descuentoAdicional = 0;

    if (tipoEnvio === 'TAKEAWAY' && formaPago === 'EFECTIVO') {
      descuentoAdicional = subtotalConPromos * 0.10;
    }

    setSubtotal(subtotalBruto);
    setDescuento(descuentoPromociones + descuentoAdicional);
    setTotalFinal(subtotalConPromos - descuentoAdicional);
    setTotalItems(currentCart.reduce((sum, item) => sum + item.quantity, 0));
  }, []);

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

  useEffect(() => {
    const loadData = async () => {
      if (!isUserLoading && isAuthenticated && cliente?.id && selectedSucursal?.id) {
        setIsLoading(true);
        try {
          const [backendCart, todasLasPromos] = await Promise.all([
            CarritoService.getCarrito(cliente.id),
            PromocionService.getAll()
          ]);

          // La lógica de filtrado de promociones ahora usa tipos explícitos
          const promosDeSucursal = todasLasPromos.filter((p: PromocionResponse) =>
            p.estadoActivo && p.sucursales.some((s: SucursalSimpleResponse) => s.id === selectedSucursal.id)
          );
          setPromocionesActivas(promosDeSucursal);

          const richItems = await enrichCartItems(backendCart);
          setCart(richItems);
          calcularTotales(richItems, promosDeSucursal);
        } catch (err) {
          console.error("Error al cargar datos del contexto del carrito:", err);
          setError("No se pudo cargar la información del carrito.");
        } finally {
          setIsLoading(false);
        }
      } else if (!isUserLoading) {
        setCart([]);
        setPromocionesActivas([]);
        calcularTotales([], []);
      }
    };
    loadData();
  }, [isUserLoading, isAuthenticated, cliente, selectedSucursal, calcularTotales, enrichCartItems]);


  const handleCartUpdate = useCallback(async (updatedBackendCart: CarritoResponse) => {
    const richItems = await enrichCartItems(updatedBackendCart);
    setCart(richItems);
    calcularTotales(richItems, promocionesActivas);
  }, [enrichCartItems, promocionesActivas, calcularTotales]);

  const aplicarDescuentosAdicionales = (tipoEnvio: TipoEnvio, formaPago: FormaPago) => {
    calcularTotales(cart, promocionesActivas, tipoEnvio, formaPago);
  };

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated || !cliente?.id) {
      setCart([]);
      setSubtotal(0);
      setDescuento(0);
      setTotalFinal(0);
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
      subtotal,
      descuento,
      totalFinal,
      totalItems,
      isLoading,
      error,
      isCartOpen,
      openCart,
      closeCart,
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart,
      aplicarDescuentosAdicionales
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