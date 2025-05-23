/**
 * @file ProductCard.tsx
 * @description Componente de tarjeta individual para mostrar un artículo manufacturado (producto).
 * Presenta la imagen, denominación, descripción y precio de venta de un producto,
 * y permite al usuario añadirlo al carrito de compras.
 *
 * @props `product`: Objeto `ArticuloManufacturado` con los datos del producto a mostrar.
 * @hook `useCart`: Hook personalizado para interactuar con la lógica del carrito de compras.
 * @function `getImageUrl`: Función de utilidad para construir la URL completa de la imagen del producto.
 */
import React from 'react';
import { Card, Button } from 'react-bootstrap';
import type { ArticuloManufacturado } from '../../types/types'; // Importa el tipo ArticuloManufacturado
import { useCart } from '../../context/CartContext'; // Importa el hook personalizado para el carrito
import { getImageUrl } from '../../services/fileUploadService'; // Importa la función para obtener la URL de la imagen

/**
 * @interface ProductCardProps
 * @description Propiedades que el componente `ProductCard` espera recibir.
 * @property {ArticuloManufacturado} product - El objeto de artículo manufacturado a mostrar en la tarjeta.
 */
interface ProductCardProps {
  product: ArticuloManufacturado;
}

const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  /**
   * @hook useCart
   * @description Accede a las funciones del carrito de compras desde el `CartContext`.
   * @returns {object} Un objeto con `addToCart` (función para añadir ítems al carrito).
   */
  const { addToCart } = useCart();

  /**
   * @function handleAddToCart
   * @description Manejador de eventos para el botón "Agregar".
   * Añade una unidad del producto actual al carrito de compras.
   */
  const handleAddToCart = () => {
    addToCart(product, 1); // Agrega 1 unidad del producto al carrito
  };

  /**
   * @constant defaultImage
   * @description Ruta a una imagen de marcador de posición (placeholder) si el producto no tiene imágenes asociadas.
   */
  const defaultImage = '/placeholder-food.png'; // Asegúrate de que esta ruta sea accesible desde `public/`

  return (
    // Tarjeta de Bootstrap que ocupa el 100% de la altura de su columna y tiene una sombra
    <Card className="h-100 shadow-sm">
      {/* Imagen del producto */}
      <Card.Img
        variant="top" // Posiciona la imagen en la parte superior de la tarjeta
        src={
          // Si el producto tiene imágenes, usa la primera; de lo contrario, usa la imagen por defecto.
          // Se utiliza getImageUrl para asegurarse de que la URL sea correcta.
          product.imagenes && product.imagenes.length > 0
            ? getImageUrl(product.imagenes[0].denominacion)
            : defaultImage
        }
        alt={`Imagen de ${product.denominacion}`} // Texto alternativo para accesibilidad
        style={{ height: '180px', objectFit: 'cover' }} // Estilos para controlar el tamaño y ajuste de la imagen
      />
      {/* Cuerpo de la tarjeta con contenido */}
      <Card.Body className="d-flex flex-column">
        {/* Título del producto */}
        <Card.Title className="mb-2">{product.denominacion}</Card.Title> {/* Añadido mb-2 para margen inferior */}
        {/* Descripción del producto */}
        <Card.Text className="text-muted flex-grow-1 overflow-hidden" style={{ maxHeight: '60px' }}> {/* flex-grow-1 para ocupar espacio, overflow y max-height para descripción larga */}
          {product.descripcion}
        </Card.Text>
        {/* Sección inferior de la tarjeta con precio y botón, alineada en la parte inferior */}
        <div className="mt-auto d-flex justify-content-between align-items-center">
          {/* Precio de venta del producto, con formato de dos decimales */}
          <Card.Text className="fs-4 fw-bold mb-0">${product.precioVenta.toFixed(2)}</Card.Text>
          {/* Botón para agregar el producto al carrito */}
          <Button variant="success" onClick={handleAddToCart}>
            Agregar al Carrito {/* Texto más descriptivo */}
          </Button>
        </div>
      </Card.Body>
    </Card>
  );
};

export default ProductCard;