// ArticuloManufacturadoDetailModal.tsx
/**
 * @description Componente modal para mostrar los detalles completos de un Artículo Manufacturado.
 * Incluye información básica, descripción, preparación, imágenes y la lista de insumos.
 */

import React from 'react';
import { Modal, Button, Row, Col, Image, ListGroup, Badge } from 'react-bootstrap';
import type { ArticuloManufacturadoResponse, ArticuloManufacturadoDetalleResponse } from '../../types/types';
import { FileUploadService } from '../../services/FileUploadService';
import apiClient from '../../services/apiClient';

/**
 * @interface ArticuloManufacturadoDetailModalProps
 * @description Propiedades que el componente ArticuloManufacturadoDetailModal espera recibir.
 * @property {boolean} show - Controla la visibilidad del modal.
 * @property {() => void} handleClose - Función para cerrar el modal.
 * @property {ArticuloManufacturado | null} articulo - El objeto ArticuloManufacturado a mostrar. Puede ser null si no hay ninguno seleccionado.
 */
interface ArticuloManufacturadoDetailModalProps {
  show: boolean;
  handleClose: () => void;
  articulo: ArticuloManufacturadoResponse | null;
}

const ArticuloManufacturadoDetailModal: React.FC<ArticuloManufacturadoDetailModalProps> = ({ show, handleClose, articulo }) => {
  const defaultImage = '/placeholder-food.png';

  if (!articulo) {
    return null;
  }
  return (
    <Modal show={show} onHide={handleClose} size="lg" centered>
      <Modal.Header closeButton>
        <Modal.Title>Detalle de: {articulo.denominacion}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Row>
          {/* Columna para Imagen Principal y Detalles Básicos */}
          <Col md={5}>
            <Image
                          src={
                articulo.imagenes && articulo.imagenes.length > 0
                  ? articulo.imagenes[0].denominacion
                  : defaultImage
              }
              alt={`Imagen de ${articulo.denominacion}`}
              fluid
              rounded
              className="mb-3 shadow-sm"
            />
            <h5><strong>ID:</strong> {articulo.id}</h5>
            <p><strong>Precio Venta:</strong> <Badge bg="success">${articulo.precioVenta.toFixed(2)}</Badge></p>
            <p><strong>Categoría:</strong> {articulo.categoria.denominacion}</p>
            <p><strong>Unidad de Medida:</strong> {articulo.unidadMedida.denominacion}</p>
            <p><strong>Tiempo Estimado:</strong> {articulo.tiempoEstimadoMinutos} minutos</p>
            <p><strong>Estado:</strong> {articulo.estadoActivo ? <Badge bg="primary">Activo</Badge> : <Badge bg="secondary">Inactivo</Badge>}</p>
          </Col>
          {/* Columna para Descripción, Preparación e Insumos */}
          <Col md={7}>
            <h4>Descripción:</h4>
            <p className="text-muted">{articulo.descripcion || 'No disponible'}</p>
            <hr />
            <h4>Preparación:</h4>
            <p style={{ whiteSpace: 'pre-wrap' }}>{articulo.preparacion || 'No disponible'}</p>
            <hr />
            <h4>Ingredientes (Insumos):</h4>
            {articulo.manufacturadoDetalles && articulo.manufacturadoDetalles.length > 0 ? (
              <ListGroup variant="flush">
                {/* FIX: Se usa la estructura del DTO de respuesta, que sí contiene toda la info */}
                {articulo.manufacturadoDetalles.map((detalle: ArticuloManufacturadoDetalleResponse) => (
                  <ListGroup.Item key={detalle.id} className="d-flex justify-content-between align-items-center">
                    <span>{detalle.articuloInsumo.denominacion}</span>
                    <Badge bg="info" pill>
                      {detalle.cantidad}
                    </Badge>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            ) : (
              <p className="text-muted">No hay ingredientes especificados para este artículo.</p>
            )}
          </Col>
        </Row>
        {/* Si tienes más imágenes, podrías mostrarlas aquí en una galería simple */}
        {articulo.imagenes && articulo.imagenes.length > 1 && (
          <>
            <hr className="my-4" />
            <h5>Otras Imágenes:</h5>
            <Row xs={2} md={4} className="g-3">
              {articulo.imagenes.slice(1).map(img => (
                <Col key={img.id}>
                  <Image
                    src={
                      articulo.imagenes && articulo.imagenes.length > 0
                        ? `${apiClient.defaults.baseURL}/files/view/${articulo.imagenes[0].denominacion}`
                        : defaultImage
                    }
                    alt={`Imagen de ${articulo.denominacion}`}
                    fluid
                    rounded
                    className="mb-3 shadow-sm"
                    style={{ maxHeight: '300px', objectFit: 'cover', width: '100%' }}
                  />
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

export default ArticuloManufacturadoDetailModal;