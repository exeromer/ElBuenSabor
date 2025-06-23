import React, { useState, useEffect } from 'react';
import { Card, Button, Badge, Spinner } from 'react-bootstrap';
import type { ArticuloManufacturadoResponse } from '../../../types/types'; 
import { useCart } from '../../../context/CartContext';
import { useUser } from '../../../context/UserContext';
import { useSucursal } from '../../../context/SucursalContext';
import { StockInsumoSucursalService } from '../../../services/StockInsumoSucursalService';
import apiClient from '../../../services/apiClient'; // Importamos apiClient para la URL de la imagen
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faMinus, faTrash, faEye } from '@fortawesome/free-solid-svg-icons';
import DetalleModal from '../../../components/products/DetalleModal/DetalleModal';
import './ProductCard.sass';

interface ProductCardProps {
  product: ArticuloManufacturadoResponse;
}

const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const { cart, addToCart, updateQuantity, removeFromCart } = useCart();
  const { selectedSucursal } = useSucursal();
  const { cliente } = useUser(); 

  const [isDisponible, setIsDisponible] = useState<boolean>(true);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [showDetailModal, setShowDetailModal] = useState(false);

  useEffect(() => {
    // Solo verificar si hay una sucursal y un cliente logueado.
    if (selectedSucursal && product.estadoActivo && cliente) {
      setIsLoading(true);
      StockInsumoSucursalService.checkDisponibilidadManufacturado(product, selectedSucursal.id)
        .then(disponible => {
          setIsDisponible(disponible);
        })
        .catch(error => {
          console.error(`Error al verificar stock para ${product.denominacion}:`, error);
          setIsDisponible(false);
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      setIsDisponible(false);
      setIsLoading(false);
    }
  }, [product, selectedSucursal, cliente]);

  const cartItem = cart.find(item => item.articulo.id === product.id);
  const quantity = cartItem ? cartItem.quantity : 0;

  const defaultImage = '/placeholder-food.png';
  const imageUrl = product.imagenes && product.imagenes.length > 0
    ? `${apiClient.defaults.baseURL}/files/download/${product.imagenes[0].denominacion}`
    : defaultImage;

  const handleAddToCart = () => {
    if (!isDisponible || !product.id || isLoading) return;
    addToCart(product, 1);
  };

  const handleIncreaseQuantity = () => {
    if (!isDisponible || !cartItem || isLoading) return;
    updateQuantity(cartItem.id, quantity + 1);
  };

  const handleDecreaseQuantity = () => {
    if (!isDisponible || !cartItem || isLoading) return;
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
    <Card className={`h-100 shadow-sm product-card ${!isDisponible || !product.estadoActivo ? 'unavailable' : ''}`}>
      <Card.Img variant="top" src={imageUrl} alt={`Imagen de ${product.denominacion}`} className="product-card-img" />
      <Card.Body className="d-flex flex-column product-card-body">
        <Card.Title className="mb-2 product-card-title" onClick={isDisponible ? handleShowDetailModal : undefined} style={{ cursor: isDisponible ? 'pointer' : 'default' }}>
          {product.denominacion}
        </Card.Title>
        <Card.Text className="text-muted flex-grow-1 overflow-hidden product-card-description">
          {product.descripcion}
        </Card.Text>

        <div className="mt-auto product-card-bottom-section">
          <Card.Text className="product-card-price-display">${product.precioVenta.toFixed(2)}</Card.Text>
          {isLoading ? (
             <Badge bg="secondary" className="my-2">Verificando...</Badge>
          ) : !isDisponible ? (
            <Badge bg="danger" className="my-2">No disponible</Badge>
          ) : null}

          <div className="product-card-buttons-group">
            {quantity === 0 ? (
              <Button variant="success" onClick={handleAddToCart} className="product-card-add-button" disabled={!isDisponible || isLoading}>
                {isLoading ? <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" /> : 'Agregar'}
              </Button>
            ) : (
              <div className="product-card-controls-wrapper d-flex align-items-center justify-content-center">
                <Button variant="outline-danger" onClick={handleRemoveFromCart} className="product-card-control-button product-card-remove-all" disabled={!isDisponible || isLoading}>
                  <FontAwesomeIcon icon={faTrash} />
                </Button>
                <Button variant="outline-secondary" onClick={handleDecreaseQuantity} className="product-card-control-button product-card-minus-button" disabled={!isDisponible || isLoading}>
                  <FontAwesomeIcon icon={faMinus} />
                </Button>
                <span className="mx-2 product-card-quantity-display">{quantity}</span>
                <Button variant="outline-primary" onClick={handleIncreaseQuantity} className="product-card-control-button product-card-plus-button" disabled={!isDisponible || isLoading}>
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
      <DetalleModal product={product} show={showDetailModal} onHide={handleCloseDetailModal} isDisponible={isDisponible} />
    </Card>
  );
};

export default ProductCard;