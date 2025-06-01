/**
 * @file CartContext.tsx
 * @description Contexto de React para gestionar el estado global del carrito de compras.
 * Proporciona las funciones y el estado necesarios para añadir, eliminar, actualizar cantidades,
 * vaciar el carrito y obtener el total y el conteo de ítems.
 * El carrito se persiste automáticamente en el `localStorage` del navegador.
 *
 * @context `CartContext`: Objeto de contexto creado para compartir el estado del carrito.
 * @provider `CartProvider`: Componente proveedor que envuelve la aplicación y gestiona el estado del carrito.
 * @hook `useCart`: Hook personalizado para consumir el `CartContext` y acceder a sus valores.
 *
 * @hook `useState`: Gestiona el estado interno del carrito (`cart`).
 * @hook `useEffect`: Sincroniza el carrito con `localStorage` cada vez que cambia.
 */
import { createContext, useContext, useState, type ReactNode, useEffect } from 'react';
import type { ArticuloManufacturado, ArticuloInsumo, CartItem } from '../types/types'; // Importa los tipos necesarios

/**
 * @interface CartContextType
 * @description Define la estructura del objeto de contexto que se provee a los consumidores.
 * @property {CartItem[]} cart - El array de ítems en el carrito.
 * @property {(item: ArticuloManufacturado | ArticuloInsumo, quantity?: number) => void} addToCart - Función para añadir un artículo al carrito.
 * @property {(itemId: number) => void} removeFromCart - Función para eliminar un artículo del carrito por su ID.
 * @property {(itemId: number, newQuantity: number) => void} updateQuantity - Función para actualizar la cantidad de un artículo en el carrito.
 * @property {() => void} clearCart - Función para vaciar completamente el carrito.
 * @property {() => number} getCartTotal - Función que devuelve el total monetario del carrito.
 * @property {() => number} getCartItemCount - Función que devuelve el número total de ítems (cantidad sumada) en el carrito.
 * @property {(itemId: number) => number} getItemQuantity - **NUEVO** Función que devuelve la cantidad de un artículo específico en el carrito.
 */
interface CartContextType {
  cart: CartItem[];
  addToCart: (item: ArticuloManufacturado | ArticuloInsumo, quantity?: number) => void;
  removeFromCart: (itemId: number) => void;
  updateQuantity: (itemId: number, newQuantity: number) => void;
  clearCart: () => void;
  getCartTotal: () => number;
  getCartItemCount: () => number;
  getItemQuantity: (itemId: number) => number; // Agregada la nueva propiedad
}

/**
 * @constant CartContext
 * @description Crea el contexto de React para el carrito. Inicialmente `undefined`.
 */
const CartContext = createContext<CartContextType | undefined>(undefined);

/**
 * @component CartProvider
 * @description Componente proveedor que encapsula la lógica del carrito de compras.
 * Proporciona el estado y las funciones del carrito a todos sus componentes hijos.
 * El estado del carrito se carga desde `localStorage` al inicio y se guarda cada vez que cambia.
 * @param {object} props - Las propiedades del componente.
 * @param {ReactNode} props.children - Los componentes hijos que tendrán acceso al contexto del carrito.
 */
export const CartProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  /**
   * @state cart
   * @description Estado que almacena el array de `CartItem`s.
   * Se inicializa intentando cargar un carrito guardado desde `localStorage`.
   * Si no hay un carrito guardado, se inicializa como un array vacío.
   */
  const [cart, setCart] = useState<CartItem[]>(() => {
    try {
      const savedCart = localStorage.getItem('el-buen-sabor-cart');
      return savedCart ? JSON.parse(savedCart) : [];
    } catch (error) {
      console.error("Error al parsear el carrito de localStorage:", error);
      return []; // Devuelve un carrito vacío en caso de error de parseo
    }
  });

  /**
   * @hook useEffect
   * @description Hook que se ejecuta cada vez que el estado `cart` cambia.
   * Persiste el estado actual del carrito en `localStorage`.
   */
  useEffect(() => {
    try {
      localStorage.setItem('el-buen-sabor-cart', JSON.stringify(cart));
    } catch (error) {
      console.error("Error al guardar el carrito en localStorage:", error);
    }
  }, [cart]); // Dependencia: se re-ejecuta cada vez que `cart` cambia

  /**
   * @function addToCart
   * @description Añade un artículo al carrito o incrementa su cantidad si ya existe.
   * @param {ArticuloManufacturado | ArticuloInsumo} item - El artículo a añadir (puede ser manufacturado o insumo).
   * @param {number} [quantity=1] - La cantidad de unidades a añadir. Por defecto es 1.
   */
  const addToCart = (item: ArticuloManufacturado | ArticuloInsumo, quantity: number = 1) => {
    setCart((prevCart) => {
      const existingItemIndex = prevCart.findIndex((cartItem) => cartItem.articulo.id === item.id);

      if (existingItemIndex > -1) {
        // Si el artículo ya está en el carrito, crea una nueva copia del carrito
        // y actualiza la cantidad del ítem existente para evitar mutación directa.
        const newCart = [...prevCart];
        newCart[existingItemIndex] = {
          ...newCart[existingItemIndex],
          quantity: newCart[existingItemIndex].quantity + quantity,
        };
        return newCart;
      } else {
        // Si el artículo no está, lo añade como un nuevo `CartItem`.
        return [...prevCart, { articulo: item, quantity }];
      }
    });
  };

  /**
   * @function removeFromCart
   * @description Elimina un artículo del carrito por su ID.
   * @param {number} itemId - El ID del artículo a eliminar del carrito.
   */
  const removeFromCart = (itemId: number) => {
    setCart((prevCart) => prevCart.filter((item) => item.articulo.id !== itemId));
  };

  /**
   * @function updateQuantity
   * @description Actualiza la cantidad de un artículo específico en el carrito.
   * Si `newQuantity` es 0 o menor, el artículo se elimina del carrito.
   * @param {number} itemId - El ID del artículo cuya cantidad se actualizará.
   * @param {number} newQuantity - La nueva cantidad deseada para el artículo.
   */
  const updateQuantity = (itemId: number, newQuantity: number) => {
    setCart((prevCart) => {
      const newCart = prevCart.map((item) =>
        item.articulo.id === itemId ? { ...item, quantity: newQuantity } : item
      );
      // Filtra y elimina los ítems cuya cantidad sea 0 o menos.
      return newCart.filter(item => item.quantity > 0);
    });
  };

  /**
   * @function getItemQuantity
   * @description **NUEVO:** Obtiene la cantidad actual de un artículo específico en el carrito.
   * @param {number} itemId - El ID del artículo cuya cantidad se desea obtener.
   * @returns {number} La cantidad del artículo en el carrito, o 0 si no se encuentra.
   */
  const getItemQuantity = (itemId: number): number => {
    const item = cart.find(cartItem => cartItem.articulo.id === itemId);
    return item ? item.quantity : 0;
  };

  /**
   * @function clearCart
   * @description Vacía completamente el carrito de compras.
   */
  const clearCart = () => {
    setCart([]);
  };

  /**
   * @function getCartTotal
   * @description Calcula y devuelve el precio total de todos los artículos en el carrito.
   * @returns {number} El total monetario del carrito.
   */
  const getCartTotal = () => {
    return cart.reduce((total, item) => total + (item.articulo.precioVenta * item.quantity), 0);
  };

  /**
   * @function getCartItemCount
   * @description Calcula y devuelve el número total de unidades de artículos en el carrito.
   * (Ej. si tienes 2 hamburguesas y 1 bebida, el conteo sería 3).
   * @returns {number} El número total de ítems contados por cantidad.
   */
  const getCartItemCount = () => {
    return cart.reduce((count, item) => count + item.quantity, 0);
  };

  return (
    // Provee el estado del carrito y las funciones de manipulación a los componentes hijos
    <CartContext.Provider
      value={{
        cart,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
        getCartTotal,
        getCartItemCount,
        getItemQuantity, // Incluida la nueva función en el valor del contexto
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

/**
 * @hook useCart
 * @description Hook personalizado para consumir el `CartContext`.
 * Simplifica el acceso a las funciones y el estado del carrito desde cualquier componente
 * que esté dentro de `CartProvider`. Lanza un error si se usa fuera del proveedor.
 * @returns {CartContextType} El objeto de contexto del carrito.
 * @throws {Error} Si `useCart` se usa fuera de un `CartProvider`.
 */
export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    // Esto es un error de desarrollo, indica que el hook se usó incorrectamente.
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};