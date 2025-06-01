/**
 * @file DetalleModal.tsx
 * @description Modal para mostrar el detalle completo de un ArticuloManufacturado.
 * @props `product`: Objeto ArticuloManufacturado con los datos del producto a mostrar.
 * @props `show`: Booleano para controlar la visibilidad del modal.
 * @props `onHide`: Función para cerrar el modal.
 */
import React from 'react';
import { Modal, Button, Image, Container, Row, Col } from 'react-bootstrap';
import type { ArticuloManufacturado } from '../../../types/types'; // Ruta correcta
import { getImageUrl } from '../../../services/fileUploadService'; // Ruta correcta
import './DetalleModal.sass'; // Crearemos este archivo SASS

interface DetalleModalProps {
  product: ArticuloManufacturado;
  show: boolean;
  onHide: () => void;
}

const DetalleModal: React.FC<DetalleModalProps> = ({ product, show, onHide }) => {
  const defaultImage = '/placeholder-food.png'; // Ruta a tu imagen por defecto

  return (
    <Modal show={show} onHide={onHide} size="lg" centered className="detalle-modal">
      <Modal.Header closeButton className="detalle-modal-header">
        <Modal.Title className="detalle-modal-title">{product.denominacion}</Modal.Title>
      </Modal.Header>
      <Modal.Body className="detalle-modal-body">
        <Container fluid>
          <Row>
            <Col md={6} className="text-center mb-3 mb-md-0">
              <Image
                src={
                  product.imagenes && product.imagenes.length > 0
                    ? getImageUrl(product.imagenes[0].denominacion)
                    : defaultImage
                }
                alt={`Imagen de ${product.denominacion}`}
                fluid
                className="detalle-modal-image"
              />
            </Col>
            <Col md={6}>
              <h5 className="detalle-modal-section-title">Descripción:</h5>
              <p className="detalle-modal-description">{product.descripcion}</p>

              <h5 className="detalle-modal-section-title">Precio:</h5>
              <p className="detalle-modal-price">${product.precioVenta.toFixed(2)}</p>

              <h5 className="detalle-modal-section-title">Tiempo Estimado de Cocina:</h5>
              <p className="detalle-modal-time">{product.tiempoEstimadoMinutos} minutos</p>

              {/* Nueva sección para "Preparación" */}
              <h5 className="detalle-modal-section-title">Preparación:</h5>
              <p className="detalle-modal-preparacion">{product.preparacion}</p>

              {/* Si ArticuloManufacturadoDetalle te da info de ingredientes que quieres mostrar
                  tendrías que iterar sobre `product.manufacturadoDetalles`
                  y buscar los nombres de los ArticuloInsumo, quizás requiriendo
                  una llamada adicional o que ya vengan pre-cargados en la prop `product`.
                  Por ahora, me apego a los campos directos de ArticuloManufacturado.
              */}
            </Col>
          </Row>
        </Container>
      </Modal.Body>
      <Modal.Footer className="detalle-modal-footer">
        <Button variant="secondary" onClick={onHide} className="detalle-modal-close-button">
          Cerrar
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default DetalleModal;