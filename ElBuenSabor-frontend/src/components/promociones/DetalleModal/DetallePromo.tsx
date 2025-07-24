import React from 'react';
import { Modal, Button, Row, Col, Image, ListGroup, Badge, Card } from 'react-bootstrap';
import type { PromocionResponse } from '../../../types/types';
import './DetallePromo.sass'

interface PromocionDetailModalProps {
  show: boolean;
  handleClose: () => void;
  promocion: PromocionResponse | null;
}

const PromocionDetailModal: React.FC<PromocionDetailModalProps> = ({ show, handleClose, promocion }) => {
  const defaultImage = '/placeholder-promo.png';

  if (!promocion) {
    return null;
  }

  const imageUrl = promocion.imagenes?.[0]?.denominacion || defaultImage;

  return (
    <Modal show={show} onHide={handleClose} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title>Detalle de Promoción: {promocion.denominacion}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Row>
          <Col md={4}>
            <Image
              src={imageUrl}
              alt={`Imagen de ${promocion.denominacion}`}
              fluid
              rounded
              className="mb-3 shadow-sm"
            />
            <h5><strong>ID:</strong> {promocion.id}</h5>
            <p><strong>Estado:</strong> {promocion.estadoActivo ? <Badge bg="success">Activa</Badge> : <Badge bg="danger">Inactiva</Badge>}</p>
            <p><strong>Vigencia:</strong> {promocion.fechaDesde} al {promocion.fechaHasta}</p>
            <p><strong>Horario:</strong> {promocion.horaDesde} a {promocion.horaHasta}</p>
          </Col>
          <Col md={8}>
            <Card>
              <Card.Header as="h5">Información del Descuento</Card.Header>
              <Card.Body>
                <p><strong>Tipo:</strong> <Badge bg="info">{promocion.tipoPromocion}</Badge></p>
                {promocion.tipoPromocion === 'PORCENTAJE' && <p><strong>Descuento:</strong> {promocion.porcentajeDescuento}%</p>}
                {promocion.tipoPromocion !== 'PORCENTAJE' && <p><strong>Precio Promocional:</strong> ${promocion.precioPromocional?.toFixed(2)}</p>}
                <p><strong>Descripción:</strong></p>
                <p className="text-muted">{promocion.descripcionDescuento || 'Sin descripción.'}</p>
              </Card.Body>
            </Card>

            <h5 className="mt-3">Artículos Incluidos</h5>
            <ListGroup variant="flush">
              {promocion.detallesPromocion.map(detalle => (
                <ListGroup.Item key={detalle.id}>
                  {detalle.cantidad} x {detalle.articulo.denominacion}
                </ListGroup.Item>
              ))}
            </ListGroup>

            <h5 className="mt-3">Sucursales Aplicables</h5>
            <div className="d-flex flex-wrap">
                {promocion.sucursales.map(sucursal => (
                    <Badge key={sucursal.id} pill bg="secondary" className="me-2 mb-2">{sucursal.nombre}</Badge>
                ))}
            </div>
          </Col>
        </Row>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Cerrar
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default PromocionDetailModal;