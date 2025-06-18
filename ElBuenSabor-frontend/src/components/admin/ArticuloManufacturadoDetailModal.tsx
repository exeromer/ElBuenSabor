// ArticuloManufacturadoDetailModal.tsx
/**
 * @description Componente modal para mostrar los detalles completos de un Artículo Manufacturado.
 * Incluye información básica, descripción, preparación, imágenes y la lista de insumos.
 */

import React from 'react';
import { Modal, Button, Row, Col, Image, ListGroup, Badge } from 'react-bootstrap';
import type { ArticuloManufacturado, ArticuloManufacturadoDetalle } from '../../types/types'; 
import { FileUploadService } from '../../services/fileUploadService';

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
  articulo: ArticuloManufacturado | null;
}

const fileUploadService = new FileUploadService(); // Instanciamos el servicio

const ArticuloManufacturadoDetailModal: React.FC<ArticuloManufacturadoDetailModalProps> = ({ show, handleClose, articulo }) => {
  const defaultImage = '/placeholder-food.png'; // Asegúrate que esta ruta sea accesible desde `public/`

  if (!articulo) {
    return null; // O un spinner/mensaje si prefieres mientras carga o si es null inesperadamente
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
                  ? fileUploadService.getImageUrl(articulo.imagenes[0].denominacion) // Usamos la instancia del servicio
                  : defaultImage
              }
              alt={`Imagen de ${articulo.denominacion}`}
              fluid
              rounded
              className="mb-3 shadow-sm"
              style={{ maxHeight: '300px', objectFit: 'cover', width: '100%' }}
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
                {articulo.manufacturadoDetalles.map((detalle: ArticuloManufacturadoDetalle, index: number) => (
                  <ListGroup.Item key={detalle.id || index} className="d-flex justify-content-between align-items-start">
                    <div>
                      {/* Asumimos que 'detalle.articuloInsumo' tiene al menos 'id' y necesitamos obtener su denominación.
                          En un escenario ideal, 'ArticuloManufacturado' vendría con los nombres de los insumos
                          o tendrías que buscarlos si solo tienes IDs.
                          
                          Revisando tu `types.ts`, `ArticuloManufacturadoDetalle` tiene:
                          articuloInsumo: { id: number };
                          Esto significa que solo tienes el ID. Para mostrar el nombre, necesitarías
                          cargar la lista de todos los ArticuloInsumo en la página padre (ManageProductsPage)
                          y pasarla a este modal, o hacer que el `ArticuloManufacturado` que llega aquí
                          ya tenga el nombre del insumo.

                          Dado que este modal es solo de VISTA, la opción más simple es que el objeto `articulo`
                          que llega como prop ya tenga los nombres de los insumos en sus `manufacturadoDetalles`.
                          Si tu `getArticuloManufacturadoById` del servicio ya devuelve esto, perfecto.
                          Si no, tendrías que enriquecerlo.

                          Para el ejemplo, asumiré que `detalle.articuloInsumo` puede no tener `denominacion` directamente si es solo ID.
                          En `ArticuloManufacturadoResponseDTO.java`, el `ArticuloManufacturadoDetalleResponseDTO` tiene `ArticuloSimpleResponseDTO articuloInsumo;`
                          y `ArticuloSimpleResponseDTO` sí tiene `denominacion`. Así que debería funcionar.
                      */}
                      <strong>{(detalle.articuloInsumo as any)?.denominacion || `Insumo ID: ${detalle.articuloInsumo.id}`}</strong>
                    </div>
                    <Badge bg="info" pill>
                      {detalle.cantidad} {(detalle.articuloInsumo as any)?.unidadMedida?.denominacion || ''}
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
                  <Image src={fileUploadService.getImageUrl(img.denominacion)} alt="Imagen adicional" thumbnail fluid /> {/* Usamos la instancia del servicio */}
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