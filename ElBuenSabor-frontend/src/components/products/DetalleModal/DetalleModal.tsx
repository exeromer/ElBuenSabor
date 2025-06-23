import React from 'react';
import { Modal, Button, Image, Container, Row, Col, Badge } from 'react-bootstrap';
// FIX 1: Usamos el DTO de respuesta correcto
import type { ArticuloManufacturadoResponse } from '../../../types/types';
import { useCart } from '../../../context/CartContext';
import apiClient from '../../../services/apiClient';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faMinus, faTrash } from '@fortawesome/free-solid-svg-icons';
import './DetalleModal.sass'; 

interface DetalleModalProps {
  product: ArticuloManufacturadoResponse;
  show: boolean;
  onHide: () => void;
  // FIX 2: Aceptamos la nueva prop para saber la disponibilidad
  isDisponible: boolean;
}

const DetalleModal: React.FC<DetalleModalProps> = ({ product, show, onHide, isDisponible }) => {
  // FIX 3: Integramos el contexto del carrito
  const { cart, addToCart, updateQuantity, removeFromCart } = useCart();
  
  const cartItem = cart.find(item => item.articulo.id === product.id);
  const quantity = cartItem ? cartItem.quantity : 0;

  const defaultImage = '/placeholder-food.png';
  const imageUrl = product.imagenes && product.imagenes.length > 0
    ? `${apiClient.defaults.baseURL}/files/download/${product.imagenes[0].denominacion}`
    : defaultImage;

  // --- Funciones para manejar el carrito (idénticas a las de ProductCard) ---
  const handleAddToCart = () => {
    if (!isDisponible || !product.id) return;
    addToCart(product, 1);
  };

  const handleIncreaseQuantity = () => {
    if (!isDisponible || !cartItem) return;
    updateQuantity(cartItem.id, quantity + 1);
  };

  const handleDecreaseQuantity = () => {
    if (!isDisponible || !cartItem) return;
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

  return (
    <Modal show={show} onHide={onHide} size="lg" centered className="detalle-modal">
      <Modal.Header closeButton className="detalle-modal-header">
        <Modal.Title className="detalle-modal-title">{product.denominacion}</Modal.Title>
      </Modal.Header>
      <Modal.Body className="detalle-modal-body">
        <Container fluid>
          <Row>
            <Col md={6} className="text-center mb-3 mb-md-0">
              <Image src={imageUrl} alt={`Imagen de ${product.denominacion}`} fluid className="detalle-modal-image" />
            </Col>
            <Col md={6}>
              <h5 className="detalle-modal-section-title">Descripción:</h5>
              <p className="detalle-modal-description">{product.descripcion}</p>
              
              <h5 className="detalle-modal-section-title">Precio:</h5>
              <p className="detalle-modal-price">${product.precioVenta.toFixed(2)}</p>

              <h5 className="detalle-modal-section-title">Tiempo Estimado de Cocina:</h5>
              <p className="detalle-modal-time">{product.tiempoEstimadoMinutos} minutos</p>

              <h5 className="detalle-modal-section-title">Preparación:</h5>
              <p className="detalle-modal-preparacion">{product.preparacion}</p>
            </Col>
          </Row>
        </Container>
      </Modal.Body>
      <Modal.Footer className="detalle-modal-footer">
        {/* FIX 3 (cont.): Añadimos los botones del carrito en el footer */}
        <div className="me-auto">
          {!isDisponible && <Badge bg="danger">No disponible</Badge>}
        </div>

        {quantity === 0 ? (
          <Button variant="success" onClick={handleAddToCart} disabled={!isDisponible}>
            Agregar al Carrito
          </Button>
        ) : (
          <div className="d-flex align-items-center">
             <Button variant="outline-danger" onClick={handleRemoveFromCart} disabled={!isDisponible}>
                <FontAwesomeIcon icon={faTrash} />
             </Button>
            <Button variant="outline-secondary" onClick={handleDecreaseQuantity} className="ms-2" disabled={!isDisponible}>
              <FontAwesomeIcon icon={faMinus} />
            </Button>
            <span className="mx-2 quantity-display">{quantity}</span>
            <Button variant="outline-primary" onClick={handleIncreaseQuantity} disabled={!isDisponible}>
              <FontAwesomeIcon icon={faPlus} />
            </Button>
          </div>
        )}
        <Button variant="secondary" onClick={onHide}>Cerrar</Button>
      </Modal.Footer>
    </Modal>
  );
};

export default DetalleModal;