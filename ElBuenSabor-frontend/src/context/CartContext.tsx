import React, { createContext, useContext, useState, type ReactNode, useEffect, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useUser } from './UserContext';
import { CarritoService } from '../services/carritoService';
import { ArticuloService } from '../services/ArticuloService';
import { useSucursal } from './SucursalContext';
import { PromocionService } from '../services/PromocionService';
import type { ArticuloResponse, CarritoResponse, PromocionResponse, SucursalSimpleResponse } from '../types/types';
import type { TipoEnvio, FormaPago } from '../types/enums';

export interface EnrichedCartItem {
  id: number;
  articulo: ArticuloResponse;
  quantity: number;
  subtotal: number;
}

interface CartContextType {
  cart: EnrichedCartItem[];
  subtotal: number;
  descuento: number;
  totalFinal: number;
  totalItems: number;
  isLoading: boolean;
  error: string | null;
  isCartOpen: boolean;
  tipoEnvio: TipoEnvio;
  formaPago: FormaPago;
  setTipoEnvio: (tipo: TipoEnvio) => void;
  setFormaPago: (forma: FormaPago) => void;
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
  const { selectedSucursal } = useSucursal();
  const [cart, setCart] = useState<EnrichedCartItem[]>([]);
  const [promocionesActivas, setPromocionesActivas] = useState<PromocionResponse[]>([]);
  const [subtotal, setSubtotal] = useState<number>(0);
  const [descuento, setDescuento] = useState<number>(0);
  const [totalFinal, setTotalFinal] = useState<number>(0);
  const [tipoEnvio, setTipoEnvio] = useState<TipoEnvio>('DELIVERY');
  const [formaPago, setFormaPago] = useState<FormaPago>('MERCADO_PAGO');
  const [totalItems, setTotalItems] = useState<number>(0);
  const [isCartOpen, setIsCartOpen] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);


  const calcularTotales = useCallback((
    currentCart: EnrichedCartItem[],
    promos: PromocionResponse[],
    currentTipoEnvio: TipoEnvio,
    currentFormaPago: FormaPago
  ) => {
    const subtotalBruto = currentCart.reduce((sum, item) => sum + (item.articulo.precioVenta * item.quantity), 0);
    let descuentoPromociones = 0;

    const itemsParaDescuento = new Map(currentCart.map(item => [item.articulo.id, item.quantity]));
    promos.forEach(promo => {
      let seAplicaPromo = true;
      let costoOriginalCombo = 0;

      for (const detalle of promo.detallesPromocion) {
        const cantidadEnCarrito = itemsParaDescuento.get(detalle.articulo.id) || 0;
        if (cantidadEnCarrito < detalle.cantidad) {
          seAplicaPromo = false;
          break;
        }
      }

      if (seAplicaPromo) {
        let vecesAplicable = Infinity;
        promo.detallesPromocion.forEach(detalle => {
          const cantidadEnCarrito = itemsParaDescuento.get(detalle.articulo.id) || 0;
          vecesAplicable = Math.min(vecesAplicable, Math.floor(cantidadEnCarrito / detalle.cantidad));
        });

        if (vecesAplicable > 0 && vecesAplicable !== Infinity) {
          promo.detallesPromocion.forEach(detalle => {
            itemsParaDescuento.set(detalle.articulo.id, (itemsParaDescuento.get(detalle.articulo.id)!) - (detalle.cantidad * vecesAplicable));
            costoOriginalCombo += detalle.articulo.precioVenta * detalle.cantidad;
          });

          if (promo.tipoPromocion === 'CANTIDAD' || promo.tipoPromocion === 'COMBO') {
            if (promo.precioPromocional) {
              descuentoPromociones += (costoOriginalCombo - promo.precioPromocional) * vecesAplicable;
            }
          } else if (promo.tipoPromocion === 'PORCENTAJE') {
            if (promo.porcentajeDescuento) {
              descuentoPromociones += (costoOriginalCombo * (promo.porcentajeDescuento / 100)) * vecesAplicable;
            }
          }
        }
      }
    });

    const subtotalConPromos = subtotalBruto - descuentoPromociones;
    let descuentoAdicional = 0;

    if (currentTipoEnvio === 'TAKEAWAY' && currentFormaPago === 'EFECTIVO') {
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
    const loadInitialData = async () => {
      if (!isUserLoading && isAuthenticated && cliente?.id && selectedSucursal?.id) {
        setIsLoading(true);
        try {
          const [backendCart, todasLasPromos] = await Promise.all([
            CarritoService.getCarrito(cliente.id),
            PromocionService.getAll()
          ]);

          const promosDeSucursal = todasLasPromos.filter((p: PromocionResponse) =>
            p.estadoActivo && p.sucursales.some((s: SucursalSimpleResponse) => s.id === selectedSucursal.id)
          );
          setPromocionesActivas(promosDeSucursal);
          const richItems = await enrichCartItems(backendCart);
          setCart(richItems);
        } catch (err) {
          console.error("Error al cargar datos del contexto:", err);
          setError("No se pudo cargar la información del carrito.");
        } finally {
          setIsLoading(false);
        }
      } else if (!isUserLoading) {
        setCart([]);
        setPromocionesActivas([]);
      }
    };
    loadInitialData();
  }, [isUserLoading, isAuthenticated, cliente, selectedSucursal, enrichCartItems]);

  useEffect(() => {
    calcularTotales(cart, promocionesActivas, tipoEnvio, formaPago);
  }, [cart, promocionesActivas, tipoEnvio, formaPago, calcularTotales]);

    const handleCartUpdate = useCallback(async (updatedBackendCart: CarritoResponse) => {
    const richItems = await enrichCartItems(updatedBackendCart);
    setCart(richItems);
  }, [enrichCartItems]);

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
      tipoEnvio,
      formaPago,
      setTipoEnvio,
      setFormaPago,
      openCart,
      closeCart,
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