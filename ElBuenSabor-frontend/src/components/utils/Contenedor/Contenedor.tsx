import { ReactNode } from 'react';
import './Contenedor.sass';

interface ContenedorProps {
  children: ReactNode;
  fluid?: boolean;
  className?: string;
  style?: React.CSSProperties;
}

const Contenedor = ({ children, fluid = false, className = '', style }: ContenedorProps) => {
  const containerClass = `contenedor${fluid ? ' contenedor-fluid' : ''} ${className}`;

  return (
    <div className={containerClass.trim()} style={style}>
      {children}
    </div>
  );
};

export default Contenedor;
