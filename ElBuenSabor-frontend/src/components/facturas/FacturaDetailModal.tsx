import React from 'react';
import { Modal, Button, Badge, ListGroup, Row, Col } from 'react-bootstrap';
import type { FacturaResponse } from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faFileInvoiceDollar, faTimesCircle } from '@fortawesome/free-solid-svg-icons';

interface FacturaDetailModalProps {
  show: boolean;
  onHide: () => void;
  factura: FacturaResponse | null;
}

const FacturaDetailModal: React.FC<FacturaDetailModalProps> = ({ show, onHide, factura }) => {
  if (!factura) {
    return null;
  }

  const isAnulada = factura.estadoFactura === 'ANULADA';

  return (
    <Modal show={show} onHide={onHide} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title>
          <FontAwesomeIcon icon={faFileInvoiceDollar} className="me-2" />
          Detalle de la Factura #{factura.id}
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Row className="mb-3">
          <Col>
            <strong>Fecha de Facturación:</strong> {new Date(factura.fechaFacturacion).toLocaleDateString()}
          </Col>
          <Col className="text-end">
            <strong>Estado:</strong>
            <Badge bg={isAnulada ? 'danger' : 'success'} className="ms-2">
              {factura.estadoFactura}
            </Badge>
          </Col>
        </Row>

        {isAnulada && (
          <Row className="mb-3 text-danger">
            <Col>
              <FontAwesomeIcon icon={faTimesCircle} className="me-2" />
              <strong>Factura Anulada el:</strong> {new Date(factura.fechaAnulacion!).toLocaleDateString()}
            </Col>
          </Row>
        )}

        <hr />
        <h6>Artículos Facturados</h6>
        <ListGroup variant="flush">
          {factura.detallesFactura?.map((detalle) => (
            <ListGroup.Item key={detalle.id} className="d-flex justify-content-between align-items-center">
              <span>{detalle.cantidad} x {detalle.denominacionArticulo}</span>
              <span className="fw-bold">${detalle.subTotal.toFixed(2)}</span>
            </ListGroup.Item>
          ))}
        </ListGroup>
                <hr />
        <div className="text-end">
            {factura.subtotal !== undefined && factura.totalDescuentos !== undefined && factura.subtotal > factura.totalVenta && (
                <>
                    <p className="mb-1">
                        <strong>Subtotal:</strong> ${factura.subtotal.toFixed(2)}
                    </p>
                    <p className="mb-1 text-danger">
                        <strong>Descuentos:</strong> -${factura.totalDescuentos.toFixed(2)}
                    </p>
                    <hr className="my-1" />
                </>
            )}
          <h4>Total: <span className="text-success">${factura.totalVenta.toFixed(2)}</span></h4>
        </div>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Cerrar
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default FacturaDetailModal;