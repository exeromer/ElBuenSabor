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
import Contenedor from '../components/utils/Contenedor/Contenedor';
import Titulo from '../components/utils/Titulo/Titulo';
import Nosotros from '../components/common/Nosotros/Nosotros';

/**
 * @interface HomePageProps
 * @description No se requieren propiedades (`props`) para este componente de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface HomePageProps { }

const HomePage: React.FC<HomePageProps> = () => {
  return (
    <Container className="my-5">
      <Contenedor>
        <div className="p-5 mb-4 rounded-3">
          <Titulo texto='Bienvenido a El Buen Sabor' nivel='titulo'/>
          <p className="lead">Tu destino para las mejores comidas con entrega rápida.</p>
          <p>Explora nuestro delicioso menú y haz tu pedido ahora mismo.</p>
          <Button variant="primary" size="lg" href="/products">
            Ver Menú
          </Button>
        </div>
      </Contenedor>

      {/* Sección de características destacadas (Calidad, Variedad, Rapidez) */}
      <div className="row">
        <div className="col-md-4 mb-4">
          <Contenedor>
            <Titulo texto='Calidad' nivel='subtitulo'/>
            <p className="text-muted">Solo usamos los ingredientes más frescos y de alta calidad para garantizar el mejor sabor en cada plato, preparados con dedicación.</p>
          </Contenedor>
        </div>
        {/* Columna para "Variedad" */}
        <div className="col-md-4 mb-4">
          <Contenedor>
            <Titulo texto='Variedad' nivel='subtitulo'/>
            <p className="text-muted">Desde pizzas artesanales hasta hamburguesas gourmet, tenemos una amplia selección para satisfacer todos los gustos y antojos.</p>
          </Contenedor>
        </div>
        {/* Columna para "Rapidez" */}
        <div className="col-md-4 mb-4">
          <Contenedor>
            <Titulo texto='Rapidez' nivel='subtitulo'/>
            <p className="text-muted">Nuestro eficiente equipo de delivery se asegura de que tu comida llegue caliente y a tiempo, directamente a la comodidad de tu puerta.</p>
          </Contenedor>
        </div>
      </div>

<Nosotros />

    </Container>
  );
};

export default HomePage;