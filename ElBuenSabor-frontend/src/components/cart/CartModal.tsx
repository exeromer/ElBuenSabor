/**
 * @file CartModal.tsx
 * @description Componente de modal que muestra el contenido del carrito de compras del usuario.
 * Permite ver los artículos añadidos, ajustar sus cantidades, eliminar ítems, vaciar el carrito
 * y proceder al checkout. Interactúa con el `CartContext` para gestionar la lógica del carrito.
 *
 * @context `CartContext`: Proporciona acceso a las funciones y el estado del carrito de compras.
 * @hook `useCart`: Hook personalizado para consumir el `CartContext`.
 * @hook `Link` de `react-router-dom`: Permite la navegación a la página de checkout.
 * @function `getImageUrl`: Ayuda a construir las URLs completas de las imágenes de los artículos.
 */
import React from 'react';
import { Modal, Button, ListGroup, Image, Col, Form } from 'react-bootstrap';
import { useCart } from '../../context/CartContext'; // Importa el hook personalizado para el carrito
import { getImageUrl } from '../../services/fileUploadService'; // Función para obtener la URL completa de la imagen
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTrashAlt } from '@fortawesome/free-solid-svg-icons'; // Icono para eliminar
import { Link } from 'react-router-dom'; // Componente Link para navegación

/**
 * @interface CartModalProps
 * @description Propiedades que el componente `CartModal` espera recibir.
 * @property {boolean} show - Controla la visibilidad del modal.
 * @property {() => void} handleClose - Función para cerrar el modal.
 */
interface CartModalProps {
  show: boolean;
  handleClose: () => void;
}

const CartModal: React.FC<CartModalProps> = ({ show, handleClose }) => {
  /**
   * @hook useCart
   * @description Accede al estado y funciones del carrito de compras desde el `CartContext`.
   * @returns {object} Un objeto con `cart` (los ítems del carrito), `updateQuantity` (para cambiar la cantidad de un ítem),
   * `removeFromCart` (para eliminar un ítem), `clearCart` (para vaciar el carrito), y `getCartTotal` (para calcular el total).
   */
  const { cart, updateQuantity, removeFromCart, clearCart, getCartTotal } = useCart();

  /**
   * @constant defaultImage
   * @description Ruta a una imagen de marcador de posición (placeholder) si un artículo no tiene imágenes.
   */
  const defaultImage = '/placeholder-food.png'; // Asegúrate de que esta ruta sea accesible desde `public/`

  return (
    <Modal show={show} onHide={handleClose} size="lg" centered> {/* Añadido 'centered' para centrar el modal */}
      <Modal.Header closeButton>
        <Modal.Title>Tu Carrito de Compras</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {/* Renderizado condicional: si el carrito está vacío o no */}
        {cart.length === 0 ? (
          <p className="text-center text-muted">El carrito está vacío. ¡Añade algunos productos para empezar a comprar!</p>
        ) : (
          <ListGroup variant="flush">
            {/* Mapea cada ítem del carrito para mostrarlo en una lista */}
            {cart.map((item) => (
              <ListGroup.Item key={item.articulo.id} className="d-flex align-items-center py-3">
                <Col xs={2} className="d-flex justify-content-center"> {/* Centrar imagen en su columna */}
                  <Image
                    src={
                      // Si el artículo tiene imágenes, usa la primera; de lo contrario, usa la imagen por defecto.
                      // Se utiliza getImageUrl para asegurarse de que la URL sea correcta.
                      item.articulo.imagenes && item.articulo.imagenes.length > 0
                        ? getImageUrl(item.articulo.imagenes[0].denominacion)
                        : defaultImage
                    }
                    thumbnail // Proporciona un borde y un padding alrededor de la imagen
                    style={{ width: '80px', height: '80px', objectFit: 'cover' }}
                    alt={`Imagen de ${item.articulo.denominacion}`} // Texto alternativo para accesibilidad
                  />
                </Col>
                <Col xs={5} className="ps-3"> {/* Padding a la izquierda para el texto */}
                  <h5 className="mb-1">{item.articulo.denominacion}</h5>
                  <p className="text-muted mb-0">${item.articulo.precioVenta.toFixed(2)} c/u</p>
                </Col>
                <Col xs={3}>
                  <Form.Control
                    type="number"
                    min="1" // Cantidad mínima de 1
                    value={item.quantity}
                    // Actualiza la cantidad en el carrito, parseando el valor a un entero
                    onChange={(e) => updateQuantity(item.articulo.id, parseInt(e.target.value))}
                    className="w-75" // Ancho reducido para el control de cantidad
                  />
                </Col>
                <Col xs={2} className="text-end">
                  {/* Muestra el subtotal del ítem */}
                  <span className="fw-bold me-3">${(item.articulo.precioVenta * item.quantity).toFixed(2)}</span>
                  {/* Botón para eliminar el ítem del carrito */}
                  <Button variant="danger" size="sm" onClick={() => removeFromCart(item.articulo.id)}>
                    <FontAwesomeIcon icon={faTrashAlt} />
                  </Button>
                </Col>
              </ListGroup.Item>
            ))}
          </ListGroup>
        )}
      </Modal.Body>
      <Modal.Footer className="justify-content-between">
        {/* Sección del total del carrito */}
        <div className="total-section">
          <h5 className="mb-0">Total: <span className="text-success">${getCartTotal().toFixed(2)}</span></h5>
        </div>
        {/* Botones de acción del carrito */}
        <div>
          <Button variant="outline-danger" onClick={clearCart} disabled={cart.length === 0} className="me-2">
            Vaciar Carrito
          </Button>
          <Button variant="secondary" onClick={handleClose} className="me-2">
            Seguir Comprando
          </Button>
          {/* Botón para finalizar compra, envuelto en un Link para navegar al checkout */}
          <Link
            to="/checkout" // Ruta a la página de checkout
            onClick={handleClose} // Cierra el modal al hacer clic en el botón de checkout
            style={{ textDecoration: 'none' }} // Elimina el subrayado predeterminado del enlace
          >
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