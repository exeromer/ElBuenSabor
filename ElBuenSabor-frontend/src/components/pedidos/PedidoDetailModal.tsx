import React from 'react';
import { Modal, Button, ListGroup, Badge, Row, Col } from 'react-bootstrap';
import type { PedidoResponse } from '../../types/types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUser, faTruck, faStore, faMoneyBillWave, faCreditCard, faReceipt } from '@fortawesome/free-solid-svg-icons';

interface CajeroPedidoDetailModalProps {
  show: boolean;
  onHide: () => void;
  pedido: PedidoResponse | null;
}

const CajeroPedidoDetailModal: React.FC<CajeroPedidoDetailModalProps> = ({ show, onHide, pedido }) => {
  if (!pedido) {
    return null;
  }

  return (
    <Modal show={show} onHide={onHide} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title>Detalle del Pedido #{pedido.id}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Row>
            {/* Columna de Cliente y Entrega */}
            <Col md={6}>
                <h5><FontAwesomeIcon icon={faUser} className="me-2" />Cliente</h5>
                <p className="ms-4">
                    <strong>Nombre:</strong> {pedido.cliente.nombre} {pedido.cliente.apellido}<br/>
                    <strong>Teléfono:</strong> {pedido.cliente.telefono}
                </p>

                <h5><FontAwesomeIcon icon={pedido.tipoEnvio === 'DELIVERY' ? faTruck : faStore} className="me-2" />Entrega</h5>
                <p className="ms-4">
                    <strong>Tipo:</strong> {pedido.tipoEnvio}<br/>
                    {pedido.tipoEnvio === 'DELIVERY' && (
                        <span><strong>Dirección:</strong> {pedido.domicilio.calle} {pedido.domicilio.numero}</span>
                    )}
                </p>

                <h5><FontAwesomeIcon icon={pedido.formaPago === 'EFECTIVO' ? faMoneyBillWave : faCreditCard} className="me-2" />Pago</h5>
                 <p className="ms-4">
                    <strong>Método:</strong> {pedido.formaPago}
                    {pedido.formaPago === 'MERCADO_PAGO' && <Badge bg="success" className="ms-2">Pagado</Badge>}
                    {pedido.formaPago === 'EFECTIVO' && <Badge bg="warning" className="ms-2">Pendiente</Badge>}
                 </p>
            </Col>

            {/* Columna del Detalle de Artículos */}
            <Col md={6}>
                <h5><FontAwesomeIcon icon={faReceipt} className="me-2" />Artículos Pedidos</h5>
                <ListGroup variant="flush">
                {pedido.detalles.map(detalle => (
                    <ListGroup.Item key={detalle.id} className="d-flex justify-content-between align-items-center ps-0">
                    <span>{detalle.cantidad} x {detalle.articulo.denominacion}</span>
                    <Badge pill bg="secondary">${detalle.subTotal.toFixed(2)}</Badge>
                    </ListGroup.Item>
                ))}
                </ListGroup>
                <hr/>
                <div className="text-end">
                    <h4>Total: ${pedido.total.toFixed(2)}</h4>
                </div>
            </Col>
        </Row>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>
          Cerrar
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default CajeroPedidoDetailModal;