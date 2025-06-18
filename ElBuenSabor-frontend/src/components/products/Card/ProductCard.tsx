import React, { useState } from 'react';
import { Card, Button, Badge } from 'react-bootstrap';
import type { ArticuloManufacturado } from '../../../types/types';
import { useCart } from '../../../context/CartContext';
import { FileUploadService } from '../../../services/fileUploadService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faMinus, faTrash, faEye } from '@fortawesome/free-solid-svg-icons';
import DetalleModal from '../../../components/products/DetalleModal/DetalleModal';
import './ProductCard.sass';

interface ProductCardProps {
  product: ArticuloManufacturado;
}

const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const { cart, addToCart, updateQuantity, removeFromCart } = useCart();
  const [showDetailModal, setShowDetailModal] = useState(false);
  const fileUploadService = new FileUploadService();

  const cartItem = cart.find(item => item.articulo.id === product.id);
  const quantity = cartItem ? cartItem.quantity : 0;

  const defaultImage = '/placeholder-food.png';
  const isAvailable = product.estadoActivo && (typeof product.unidadesDisponiblesCalculadas === 'number' && product.unidadesDisponiblesCalculadas > 0);

  const handleAddToCart = () => {
    if (!isAvailable || !product.id) return;
    addToCart(product, 1);
  };

  const handleIncreaseQuantity = () => {
    if (!isAvailable || !cartItem) return;
    updateQuantity(cartItem.id, quantity + 1);
  };

  const handleDecreaseQuantity = () => {
    if (!isAvailable || !cartItem) return;
    if (quantity === 1) {
      removeFromCart(cartItem.id);
    } else {
      updateQuantity(cartItem.id, quantity - 1);
    }
  };
  
  const handleRemoveFromCart = () => {
    if (!cartItem) return;
    removeFromCart(cartItem.id);
  };

  const handleShowDetailModal = () => setShowDetailModal(true);
  const handleCloseDetailModal = () => setShowDetailModal(false);

  return (
    <Card className="h-100 shadow-sm product-card">
      <Card.Img
        variant="top"
        src={
          product.imagenes && product.imagenes.length > 0
            ? fileUploadService.getImageUrl(product.imagenes[0].denominacion ?? '')
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

        <div className="mt-auto product-card-bottom-section">
          <Card.Text className="product-card-price-display">${product.precioVenta.toFixed(2)}</Card.Text>
          {!product.estadoActivo ? (
            <Badge bg="secondary" className="my-2">No Activo</Badge>
          ) : !isAvailable ? (
            <Badge bg="danger" className="my-2">No disponible</Badge>
          ) : null}

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
                <Button variant="outline-primary" onClick={handleIncreaseQuantity} className="product-card-control-button product-card-plus-button" disabled={!isAvailable || (typeof product.unidadesDisponiblesCalculadas === 'number' && quantity >= product.unidadesDisponiblesCalculadas)}>
                  <FontAwesomeIcon icon={faPlus} />
                </Button>
              </div>
            )}
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