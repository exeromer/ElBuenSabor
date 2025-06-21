import React from 'react';
import { Modal, Button, ListGroup, Image, Col, Form, Spinner, Alert } from 'react-bootstrap';
import { useCart } from '../../context/CartContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrashAlt } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';

const CartModal: React.FC = () => {
  const {
    cart,
    updateQuantity,
    removeFromCart,
    clearCart,
    totalPrice,
    isCartOpen,
    closeCart,
    isLoading,
    error,
  } = useCart();

  const defaultImage = '/placeholder-food.png';

  return (
    <Modal show={isCartOpen} onHide={closeCart} size="lg" centered backdrop="static">
      <Modal.Header closeButton>
        <Modal.Title>Tu Carrito de Compras</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {isLoading && <div className="text-center"><Spinner animation="border" /> <p>Actualizando carrito...</p></div>}
        {error && <Alert variant="danger">{error}</Alert>}

        {!isLoading && cart.length === 0 ? (
          <p className="text-center text-muted">El carrito está vacío. ¡Añade algunos productos!</p>
        ) : (
          <ListGroup variant="flush">
            {cart.map((item) => (
              <ListGroup.Item key={item.id} className="d-flex align-items-center py-3">
                <Col xs={2} className="d-flex justify-content-center">
                  <Image
                    src={
                      item.articulo.imagenes && item.articulo.imagenes.length > 0
                        ? item.articulo.imagenes[0].denominacion
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
                    disabled={isLoading}
                    className="w-75"
                  />
                </Col>
                <Col xs={2} className="text-end">
                  <span className="fw-bold me-3">${item.subtotal.toFixed(2)}</span>
                  <Button variant="outline-danger" size="sm" onClick={() => removeFromCart(item.id)} disabled={isLoading}>
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
          <h5 className="mb-0">Total: <span className="text-success">${totalPrice.toFixed(2)}</span></h5>
        </div>
        <div>
          <Button variant="outline-danger" onClick={clearCart} disabled={cart.length === 0 || isLoading} className="me-2">
            Vaciar Carrito
          </Button>
          <Button variant="secondary" onClick={closeCart} disabled={isLoading} className="me-2">
            Seguir Comprando
          </Button>
          <Link to="/checkout" onClick={closeCart} style={{ textDecoration: 'none' }}>
            <Button variant="primary" disabled={cart.length === 0 || isLoading}>
              Finalizar Compra
            </Button>
          </Link>
        </div>
      </Modal.Footer>
    </Modal>
  );
};

export default CartModal;