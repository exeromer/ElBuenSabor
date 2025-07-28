import React, { useState } from 'react';
import { Modal, Button, ListGroup, Badge, Row, Col, Spinner } from 'react-bootstrap';
import type { PedidoResponse, ArticuloManufacturadoResponse} from '../../types/types';
import { useUser } from '../../context/UserContext';
import ArticuloManufacturadoDetailModal from '../admin/ArticuloManufacturadoDetailModal';
import { ArticuloManufacturadoService } from '../../services/ArticuloManufacturadoService';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUser, faTruck, faStore, faMoneyBillWave, faCreditCard, faReceipt, faEye } from '@fortawesome/free-solid-svg-icons';

interface PedidoDetailModalProps  {
  show: boolean;
  onHide: () => void;
  pedido: PedidoResponse | null;
}

const PedidoDetailModal: React.FC<PedidoDetailModalProps> = ({ show, onHide, pedido }) => {
  const { userRole } = useUser();
  const [showRecipeModal, setShowRecipeModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState<ArticuloManufacturadoResponse | null>(null);
  const [loadingRecipe, setLoadingRecipe] = useState(false);

  const handleViewRecipe = async (articuloId: number) => {
    setLoadingRecipe(true);
    try {
      const fullProductData = await ArticuloManufacturadoService.getById(articuloId);
      setSelectedProduct(fullProductData);
      setShowRecipeModal(true);
    } catch (error) {
      console.error("Error al cargar la receta:", error);
      alert("No se pudo cargar el detalle del producto.");
    } finally {
      setLoadingRecipe(false);
    }
  };

   if (!pedido) {
    return null;
  }

   return (
    <>
      <Modal show={show} onHide={onHide} size="lg" centered>
        <Modal.Header closeButton>
          <Modal.Title>Detalle del Pedido #{pedido.id}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Row>
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

              <Col md={6}>
                  <h5><FontAwesomeIcon icon={faReceipt} className="me-2" />Artículos Pedidos</h5>
                  {loadingRecipe && <div className="text-center"><Spinner size="sm" /> Cargando receta...</div>}
                  <ListGroup variant="flush">
                  {pedido.detalles.map(detalle => (
                      <ListGroup.Item key={detalle.id} className="d-flex justify-content-between align-items-center ps-0">
                        <div>
                          <span>{detalle.cantidad} x {detalle.articulo.denominacion}</span>
                        </div>
                        <div className="d-flex align-items-center">
                          <Badge pill bg="secondary" className="me-3">${detalle.subTotal.toFixed(2)}</Badge>
                          
                          {(userRole === 'ADMIN' || userRole === 'EMPLEADO') && (
                            <Button
                              variant="outline-info"
                              size="sm"
                              onClick={() => handleViewRecipe(detalle.articulo.id)}
                              title="Ver Receta"
                              disabled={loadingRecipe}
                            >
                              <FontAwesomeIcon icon={faEye} />
                            </Button>
                          )}
                        </div>
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

      {selectedProduct && (
        <ArticuloManufacturadoDetailModal
            show={showRecipeModal}
            handleClose={() => setShowRecipeModal(false)}
            articulo={selectedProduct}
        />
      )}
    </>
  );
};

export default PedidoDetailModal;