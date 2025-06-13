import React from 'react';
import { Modal, Button, ListGroup, Image, Col, Form } from 'react-bootstrap';
import { useCart } from '../../context/CartContext';
import { FileUploadService } from '../../services/fileUploadService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrashAlt } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';

interface CartModalProps {
  show: boolean;
  handleClose: () => void;
}

const CartModal: React.FC<CartModalProps> = ({ show, handleClose }) => {
  const { cart, updateQuantity, removeFromCart, clearCart, getCartTotal } = useCart();
  const defaultImage = '/placeholder-food.png';
  const fileUploadService = new FileUploadService();

  return (
    <Modal show={show} onHide={handleClose} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title>Tu Carrito de Compras</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {cart.length === 0 ? (
          <p className="text-center text-muted">El carrito está vacío. ¡Añade algunos productos para empezar a comprar!</p>
        ) : (
          <ListGroup variant="flush">
            {cart.map((item) => (
              <ListGroup.Item key={item.id} className="d-flex align-items-center py-3">
                <Col xs={2} className="d-flex justify-content-center">
                  <Image
                    src={
                      item.articulo.imagenes && item.articulo.imagenes.length > 0
                        ? fileUploadService.getImageUrl(item.articulo.imagenes[0].denominacion)
                        : defaultImage
                    }
                    thumbnail
                    style={{ width: '80px', height: '80px', objectFit: 'cover' }}
                    alt={`Imagen de ${item.articulo.denominacion}`}
                  />
                </Col>
                <Col xs={5} className="ps-3">
                  <h5 className="mb-1">{item.articulo.denominacion}</h5>
                  <p className="text-muted mb-0">${item.articulo.precioVenta.toFixed(2)} c/u</p>
                </Col>
                <Col xs={3}>
                  <Form.Control
                    type="number"
                    min="1"
                    value={item.quantity}
                    onChange={(e) => updateQuantity(item.id, parseInt(e.target.value))}
                    className="w-75"
                  />
                </Col>
                <Col xs={2} className="text-end">
                  <span className="fw-bold me-3">${(item.articulo.precioVenta * item.quantity).toFixed(2)}</span>
                  <Button variant="danger" size="sm" onClick={() => removeFromCart(item.id)}>
                    <FontAwesomeIcon icon={faTrashAlt} />
                  </Button>
                </Col>
              </ListGroup.Item>
            ))}
          </ListGroup>
        )}
      </Modal.Body>
      <Modal.Footer className="justify-content-between">
        <div className="total-section">
          <h5 className="mb-0">Total: <span className="text-success">${getCartTotal().toFixed(2)}</span></h5>
        </div>
        <div>
          <Button variant="outline-danger" onClick={clearCart} disabled={cart.length === 0} className="me-2">
            Vaciar Carrito
          </Button>
          <Button variant="secondary" onClick={handleClose} className="me-2">
            Seguir Comprando
          </Button>
          <Link to="/checkout" onClick={handleClose} style={{ textDecoration: 'none' }}>
            <Button variant="primary" disabled={cart.length === 0}>
              Finalizar Compra
            </Button>
          </Link>
        </div>
      </Modal.Footer>
    </Modal>
  );
};

export default CartModal;