/**
 * @file ProductCard.tsx
 * @description Componente de tarjeta individual para mostrar un artículo manufacturado (producto).
 * Presenta la imagen, denominación, descripción y precio de venta de un producto,
 * y permite al usuario añadirlo al carrito de compras, o ajustar la cantidad si ya está en él.
 * También incluye un botón para ver los detalles del producto en un modal.
 *
 * @props `product`: Objeto `ArticuloManufacturado` con los datos del producto a mostrar.
 * @hook `useCart`: Hook personalizado para interactuar con la lógica del carrito de compras.
 * @function `getImageUrl`: Función de utilidad para construir la URL completa de la imagen del producto.
 * @component `DetalleModal`: Modal para mostrar los detalles del producto.
 */
import React, { useState } from 'react';
import { Card, Button,Badge } from 'react-bootstrap';
import type { ArticuloManufacturado } from '../../../types/types';
import { useCart } from '../../../context/CartContext';
import { getImageUrl } from '../../../services/fileUploadService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faMinus, faTrash, faEye } from '@fortawesome/free-solid-svg-icons';
import './ProductCard.sass';
import DetalleModal from '../../../components/products/DetalleModal/DetalleModal';

interface ProductCardProps {
  product: ArticuloManufacturado;
}

const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const {
    addToCart,
    updateQuantity,
    removeFromCart,
    getItemQuantity,
  } = useCart();

  const [showDetailModal, setShowDetailModal] = useState(false);

  const quantity = getItemQuantity(product.id);
  const defaultImage = '/placeholder-food.png';

  // Determinar disponibilidad
  const isAvailable = product.estadoActivo && (typeof product.unidadesDisponiblesCalculadas === 'number' && product.unidadesDisponiblesCalculadas > 0);
  // O si prefieres un booleano directo desde el backend: const isAvailable = product.disponible;
  const handleAddToCart = () => {
    if (!isAvailable) return; // Doble chequeo
    addToCart(product, 1);
  };

  const handleIncreaseQuantity = () => {
    updateQuantity(product.id, quantity + 1);
  };

  const handleDecreaseQuantity = () => {
    if (quantity === 1) {
      removeFromCart(product.id);
    } else {
      updateQuantity(product.id, quantity - 1);
    }
  };

  const handleRemoveFromCart = () => {
    removeFromCart(product.id);
  };

  const handleShowDetailModal = () => setShowDetailModal(true);
  const handleCloseDetailModal = () => setShowDetailModal(false);

  return (
    <Card className="h-100 shadow-sm product-card">
      <Card.Img
        variant="top"
        src={
          product.imagenes && product.imagenes.length > 0
            ? getImageUrl(product.imagenes[0].denominacion)
            : defaultImage
        }
        alt={`Imagen de ${product.denominacion}`}
        className="product-card-img"
      />
      <Card.Body className="d-flex flex-column product-card-body">
        <Card.Title className="mb-2 product-card-title" onClick={isAvailable ? handleShowDetailModal : undefined} style={{ cursor: isAvailable ? 'pointer' : 'default' }}>
          {product.denominacion}
        </Card.Title>
        <Card.Text className="text-muted flex-grow-1 overflow-hidden product-card-description">
          {product.descripcion}
        </Card.Text>

        {/* Nuevo contenedor para el footer: Precio y luego grupo de botones */}
        <div className="mt-auto product-card-bottom-section">
          {/* Precio más grande y centrado */}
          <Card.Text className="product-card-price-display">${product.precioVenta.toFixed(2)}</Card.Text>
                   {!product.estadoActivo ? (
            <Badge bg="secondary" className="my-2">No Activo</Badge>
          ) : !isAvailable ? (
            <Badge bg="danger" className="my-2">No disponible</Badge>
          ) : null}

          {/* Grupo de botones (Agregar o Controles de Cantidad) */}
          <div className="product-card-buttons-group">
            {quantity === 0 ? (
              <Button variant="success" onClick={handleAddToCart} className="product-card-add-button" disabled={!isAvailable}>
                Agregar al Carrito
              </Button>
            ) : (
              <div className="product-card-controls-wrapper d-flex align-items-center justify-content-center">
                <Button variant="outline-danger" onClick={handleRemoveFromCart} className="product-card-control-button product-card-remove-all" disabled={!isAvailable}>
                  <FontAwesomeIcon icon={faTrash} />
                </Button>
                <Button variant="outline-secondary" onClick={handleDecreaseQuantity} className="product-card-control-button product-card-minus-button" disabled={!isAvailable}>
                  <FontAwesomeIcon icon={faMinus} />
                </Button>
                <span className="mx-2 product-card-quantity-display">{quantity}</span>
                <Button variant="outline-primary" onClick={handleIncreaseQuantity} className="product-card-control-button product-card-plus-button" disabled={!isAvailable}>
                  <FontAwesomeIcon icon={faPlus} />
                </Button>
              </div>
            )}
            {/* Botón para ver detalles */}
            <Button variant="outline-info" onClick={handleShowDetailModal} className="product-card-details-button">
              <FontAwesomeIcon icon={faEye} className="me-2" /> Ver Detalles
            </Button>
          </div>
        </div>
      </Card.Body>
      <DetalleModal product={product} show={showDetailModal} onHide={handleCloseDetailModal} />
    </Card>
  );
};

export default ProductCard;