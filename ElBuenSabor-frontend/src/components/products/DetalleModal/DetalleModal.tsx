import React from 'react';
import { Modal, Button, Image, Container, Row, Col, Badge, ListGroup } from 'react-bootstrap';
import type { ArticuloManufacturadoResponse } from '../../../types/types';
import { useCart } from '../../../context/CartContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus, faMinus, faTrash } from '@fortawesome/free-solid-svg-icons';
import { useUser } from '../../../context/UserContext';
import './DetalleModal.sass';

interface DetalleModalProps {
  product: ArticuloManufacturadoResponse;
  show: boolean;
  onHide: () => void;
  isDisponible: boolean;
}

const DetalleModal: React.FC<DetalleModalProps> = ({ product, show, onHide, isDisponible }) => {
  const { cart, addToCart, updateQuantity, removeFromCart } = useCart();
  const { userRole } = useUser();
  const cartItem = cart.find(item => item.articulo.id === product.id);
  const quantity = cartItem ? cartItem.quantity : 0;
  const defaultImage = '/placeholder-food.png';
  const imageUrl = product.imagenes?.[0]?.denominacion || defaultImage;

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
              {(userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
                <>
                  <h5 className="detalle-modal-section-title">Tiempo Estimado de Cocina:</h5>
                  <p className="detalle-modal-time">{product.tiempoEstimadoMinutos} minutos</p>

                  <h5 className="detalle-modal-section-title">Preparación:</h5>
                  <p className="detalle-modal-preparacion">{product.preparacion}</p>
                </>
              )}
            </Col>
          </Row>
          {product.manufacturadoDetalles && product.manufacturadoDetalles.length > 0 && (
            <Row className="mt-4">
              <Col>
                <h5 className="detalle-modal-section-title">Ingredientes:</h5>
                <ListGroup variant="flush">
                  {product.manufacturadoDetalles.map(detalle => (
                    <ListGroup.Item key={detalle.id} className="bg-transparent px-0">
                      - {detalle.articuloInsumo.denominacion}
                      {(userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
                        <span className="text-muted fst-italic ms-2">
                          ({detalle.cantidad})
                        </span>
                      )}
                    </ListGroup.Item>
                  ))}
                </ListGroup>
              </Col>
            </Row>
          )}
        </Container>
      </Modal.Body>
      <Modal.Footer className="detalle-modal-footer">
        <div className="me-auto">
          {!isDisponible && <Badge bg="danger">No disponible</Badge>}
        </div>

        {quantity === 0 ? (
          <Button variant="success" onClick={handleAddToCart} disabled={!isDisponible} className="detalle-modal-add-button">
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
      </Modal.Footer>
    </Modal>
  );
};

export default DetalleModal;