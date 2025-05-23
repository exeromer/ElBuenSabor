/**
 * @file Footer.tsx
 * @description Componente de pie de página (footer) de la aplicación.
 * Muestra información de derechos de autor, dirección y contacto.
 * Se adhiere a la parte inferior de la página gracias a `mt-auto` y `shadow-top` de Bootstrap.
 * Utiliza componentes de `react-bootstrap` para el layout.
 */
import React from 'react';
import { Container } from 'react-bootstrap';

/**
 * @interface FooterProps
 * @description No se requieren propiedades (`props`) para este componente de pie de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface FooterProps {} // Interfaz vacía para tipificar las props de este componente simple

const Footer: React.FC<FooterProps> = () => {
  return (
    <footer className="bg-light py-3 mt-auto shadow-top">
      {/* Contenedor de Bootstrap para centrar y limitar el ancho del contenido */}
      <Container className="text-center">
        {/* Párrafo de derechos de autor, con el año actual */}
        <p>&copy; {new Date().getFullYear()} El Buen Sabor. Todos los derechos reservados.</p>
        {/* Información de contacto ficticia */}
        <p>Dirección: Calle Falsa 123, Ciudad de Mendoza</p>
        <p>Teléfono: +54 9 261 123-4567</p>
      </Container>
    </footer>
  );
};

export default Footer;