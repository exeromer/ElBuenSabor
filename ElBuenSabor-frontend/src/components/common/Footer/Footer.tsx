/**
 * @file Footer.tsx
 * @description Componente de pie de página (footer) de la aplicación.
 * Muestra información de derechos de autor, dirección y contacto, y enlaces de navegación.
 * Se adhiere a la parte inferior de la página gracias a `mt-auto` y `shadow-top` de Bootstrap.
 * Utiliza componentes de `react-bootstrap` para el layout.
 */
import React from 'react';
import { Container, Row, Col } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faLocationDot, faMobile } from '@fortawesome/free-solid-svg-icons';
import './Footer.sass';

/**
 * @interface FooterProps
 * @description No se requieren propiedades (`props`) para este componente de pie de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface FooterProps {}

const Footer: React.FC<FooterProps> = () => {
  return (
    <footer className="py-4 mt-auto shadow-top app-footer">
      <Container className="footer-container">
        <Row className="justify-content-center text-center text-md-start footer-row-content">
          {/* Columna de Contacto */}
          <Col md={4} className="mb-4 mb-md-0 footer-col footer-col-contact">
            <h5 className="footer-heading">Contacto</h5>
            <p className="footer-contact-item">
              <FontAwesomeIcon icon={faLocationDot} className="me-2 footer-icon" />
              Calle Falsa 123, Ciudad de Mendoza
            </p>
            <p className="footer-contact-item">
              <FontAwesomeIcon icon={faMobile} className="me-2 footer-icon" />
              +54 9 261 123-4567
            </p>
          </Col>

          {/* Columna de Enlaces Rápidos */}
          <Col md={2} className="mb-4 mb-md-0 footer-col footer-col-links">
            <h5 className="footer-heading">Enlaces Rápidos</h5>
            <ul className="list-unstyled footer-list">
              <li className="footer-list-item"><a href="/home" className="footer-link">Inicio</a></li>
              <li className="footer-list-item"><a href="/products" className="footer-link">Productos</a></li>
              <li className="footer-list-item"><a href="#about" className="footer-link">Acerca de</a></li>
            </ul>
          </Col>

          {/* Columna de Servicios */}
          <Col md={2} className="mb-4 mb-md-0 footer-col footer-col-services">
            <h5 className="footer-heading">Servicios</h5>
            <ul className="list-unstyled footer-list">
              <li className="footer-list-item"><a href="#delivery" className="footer-link">Delivery</a></li>
              <li className="footer-list-item"><a href="#catering" className="footer-link">Catering</a></li>
              <li className="footer-list-item"><a href="#reservations" className="footer-link">Reservas</a></li>
            </ul>
          </Col>

          {/* Columna de Legal */}
          <Col md={2} className="mb-4 mb-md-0 footer-col footer-col-legal">
            <h5 className="footer-heading">Legal</h5>
            <ul className="list-unstyled footer-list">
              <li className="footer-list-item"><a href="#privacy" className="footer-link">Política de Privacidad</a></li>
              <li className="footer-list-item"><a href="#terms" className="footer-link">Términos y Condiciones</a></li>
              <li className="footer-list-item"><a href="#faq" className="footer-link">Preguntas Frecuentes</a></li>
            </ul>
          </Col>
        </Row>
        <hr className="footer-divider" />
        {/* Párrafo de derechos de autor */}
        <Row className="footer-row-copyright">
          <Col className="text-center mt-3 footer-col-copyright">
            <p className="footer-copyright-text">&copy; {new Date().getFullYear()} El Buen Sabor. Todos los derechos reservados.</p>
          </Col>
        </Row>
      </Container>
    </footer>
  );
};

export default Footer;