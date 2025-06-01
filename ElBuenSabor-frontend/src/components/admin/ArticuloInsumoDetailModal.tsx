// src/components/admin/ArticuloInsumoDetailModal.tsx
import React from 'react';
import { Modal, Button, Row, Col, Image, Badge, Card } from 'react-bootstrap';
import type { ArticuloInsumo } from '../../types/types';
import { getImageUrl } from '../../services/fileUploadService';

interface ArticuloInsumoDetailModalProps {
  show: boolean;
  handleClose: () => void;
  articulo: ArticuloInsumo | null;
}

const ArticuloInsumoDetailModal: React.FC<ArticuloInsumoDetailModalProps> = ({ show, handleClose, articulo }) => {
  const defaultImage = '/placeholder-food.png'; // Imagen por defecto

  if (!articulo) {
    return null;
  }

  return (
    <Modal show={show} onHide={handleClose} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title>Detalle de Insumo: {articulo.denominacion}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Row>
          <Col md={4}>
            <Image
              src={
                articulo.imagenes && articulo.imagenes.length > 0
                  ? getImageUrl(articulo.imagenes[0].denominacion)
                  : defaultImage
              }
              alt={`Imagen de ${articulo.denominacion}`}
              fluid
              rounded
              className="mb-3 shadow-sm"
              style={{ maxHeight: '250px', objectFit: 'cover', width: '100%' }}
            />
            <h5>ID: {articulo.id}</h5>
            <p>
              <strong>Estado: </strong>
              {articulo.estadoActivo ? <Badge bg="success">Activo</Badge> : <Badge bg="danger">Inactivo</Badge>}
            </p>
             <p>
              <strong>Es para Elaborar: </strong>
              {articulo.esParaElaborar ? <Badge bg="info">Sí</Badge> : <Badge bg="secondary">No</Badge>}
            </p>
          </Col>
          <Col md={8}>
            <Card>
              <Card.Header as="h5">Información General</Card.Header>
              <Card.Body>
                <Row>
                  <Col md={6}>
                    <p><strong>Denominación:</strong> {articulo.denominacion}</p>
                    <p><strong>Categoría:</strong> {articulo.categoria.denominacion}</p>
                    <p><strong>Unidad de Medida:</strong> {articulo.unidadMedida.denominacion}</p>
                  </Col>
                  <Col md={6}>
                     <p><strong>Precio Venta:</strong> ${articulo.precioVenta.toFixed(2)}</p>
                    <p><strong>Precio Compra:</strong> ${typeof articulo.precioCompra === 'number' ? articulo.precioCompra.toFixed(2) : 'N/A'}</p>
                  </Col>
                </Row>
              </Card.Body>
            </Card>
            <Card className="mt-3">
              <Card.Header as="h5">Stock</Card.Header>
              <Card.Body>
                 <Row>
                    <Col md={6}>
                        <p><strong>Stock Actual:</strong> {articulo.stockActual}</p>
                    </Col>
                    <Col md={6}>
                        {/* Asegurarse que stockMinimo exista en el tipo ArticuloInsumo */}
                        <p><strong>Stock Mínimo:</strong> {typeof articulo.stockMinimo === 'number' ? articulo.stockMinimo : 'No definido'}</p>
                    </Col>
                 </Row>
              </Card.Body>
            </Card>
          </Col>
        </Row>
        
        {/* Galería de imágenes adicionales si existen */}
        {articulo.imagenes && articulo.imagenes.length > 1 && (
          <>
            <hr className="my-4" />
            <h5>Otras Imágenes:</h5>
            <Row xs={2} sm={3} md={4} className="g-3">
              {articulo.imagenes.slice(1).map(img => (
                <Col key={img.id}>
                  <Image src={getImageUrl(img.denominacion)} alt="Imagen adicional del insumo" thumbnail fluid />
                </Col>
              ))}
            </Row>
          </>
        )}

      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose}>
          Cerrar
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default ArticuloInsumoDetailModal;