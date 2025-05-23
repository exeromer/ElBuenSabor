/**
 * @file Loading.tsx
 * @description Componente de presentación simple que muestra un indicador de carga (spinner)
 * y un mensaje mientras la aplicación o una sección específica está cargando.
 * Proporciona una experiencia de usuario básica para indicar que se está realizando una operación.
 * Utiliza componentes de `react-bootstrap` para el diseño del spinner y el layout.
 */
import React from 'react';
import { Spinner, Container, Row, Col } from 'react-bootstrap';

/**
 * @interface LoadingProps
 * @description No se requieren propiedades (`props`) para este componente simple de carga,
 * por lo que se define una interfaz vacía para claridad.
 */
interface LoadingProps {} // No se necesitan props para este componente simple, pero se define para claridad.

const Loading: React.FC<LoadingProps> = () => {
  return (
    // Contenedor principal que centra el contenido vertical y horizontalmente en la pantalla
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
      <Row className="text-center">
        <Col>
          {/* Spinner de Bootstrap para indicar actividad */}
          <Spinner animation="border" role="status" className="mb-3" />
          {/* Mensaje de carga */}
          <p>Cargando aplicación...</p>
        </Col>
      </Row>
    </Container>
  );
};

export default Loading;