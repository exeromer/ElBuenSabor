/**
 * @file HomePage.tsx
 * @description Página de inicio de la aplicación "El Buen Sabor".
 * Sirve como la primera impresión para los usuarios, presentando un mensaje de bienvenida,
 * destacando los valores de la empresa (calidad, variedad, rapidez) y proporcionando
 * un enlace directo a la página de productos.
 * Utiliza componentes y clases de utilidad de `react-bootstrap` para el diseño responsivo y la presentación.
 *
 * @component `Link` de `react-router-dom`: Utilizado para la navegación al menú de productos.
 */
import React from 'react';
import { Container, Button } from 'react-bootstrap';

/**
 * @interface HomePageProps
 * @description No se requieren propiedades (`props`) para este componente de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface HomePageProps {}

const HomePage: React.FC<HomePageProps> = () => {
  return (
    // Contenedor principal de Bootstrap para centrar el contenido y aplicar márgenes verticales.
    <Container className="my-5 text-center">
      {/* Sección principal de bienvenida, estilizada como un "Jumbotron" moderno de Bootstrap */}
      {/* Clases de Bootstrap: */}
      {/* - `p-5`: Padding en todas las direcciones. */}
      {/* - `mb-4`: Margen inferior. */}
      {/* - `bg-light`: Fondo de color claro. */}
      {/* - `rounded-3`: Esquinas redondeadas. */}
      <div className="p-5 mb-4 bg-light rounded-3">
        {/* Título principal con estilo de display */}
        <h1 className="display-4">¡Bienvenido a El Buen Sabor!</h1>
        {/* Párrafo principal con estilo de texto guía */}
        <p className="lead">Tu destino para las mejores comidas con entrega rápida.</p>
        {/* Línea horizontal divisoria */}
        <hr className="my-4" />
        {/* Párrafo con llamado a la acción */}
        <p>Explora nuestro delicioso menú y haz tu pedido ahora mismo.</p>
        {/* Botón grande que enlaza a la página de productos (Menú) */}
        <Button variant="primary" size="lg" href="/products" className="shadow-sm"> {/* Añadido shadow-sm para un toque visual */}
          Ver Menú
        </Button>
      </div>

      {/* Sección de características destacadas (Calidad, Variedad, Rapidez) */}
      <div className="row">
        {/* Columna para "Calidad" */}
        <div className="col-md-4 mb-4"> {/* Añadido mb-4 para margen inferior en móviles */}
          <h2 className="text-primary mb-3">Calidad</h2> {/* Color de texto y margen */}
          <p className="text-muted">Solo usamos los ingredientes más frescos y de alta calidad para garantizar el mejor sabor en cada plato, preparados con dedicación.</p>
        </div>
        {/* Columna para "Variedad" */}
        <div className="col-md-4 mb-4">
          <h2 className="text-success mb-3">Variedad</h2> {/* Color de texto y margen */}
          <p className="text-muted">Desde pizzas artesanales hasta hamburguesas gourmet, tenemos una amplia selección para satisfacer todos los gustos y antojos.</p>
        </div>
        {/* Columna para "Rapidez" */}
        <div className="col-md-4 mb-4">
          <h2 className="text-info mb-3">Rapidez</h2> {/* Color de texto y margen */}
          <p className="text-muted">Nuestro eficiente equipo de delivery se asegura de que tu comida llegue caliente y a tiempo, directamente a la comodidad de tu puerta.</p>
        </div>
      </div>
    </Container>
  );
};

export default HomePage;