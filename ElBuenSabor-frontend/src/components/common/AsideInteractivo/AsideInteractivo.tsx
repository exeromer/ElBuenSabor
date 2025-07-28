import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUtensils } from '@fortawesome/free-solid-svg-icons';
import './AsideInteractivo.sass';

const mensajesDivertidos = [
  "¿Sabías que una pizza redonda viene en una caja cuadrada? ¡Pura magia!",
  "Nuestras hamburguesas son tan buenas que hasta las papas fritas las aplauden.",
  "La vida es corta, ¡cómete el postre primero! Tenemos unas promos que te encantarán.",
  "Si te caes, levántate. A menos que haya una de nuestras empanadas en el suelo. ¡Prioridades!",
  "Un dato curioso: 'El Buen Sabor' es el ingrediente secreto de la felicidad."
];

const AsideInteractivo: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [mensaje, setMensaje] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const randomIndex = Math.floor(Math.random() * mensajesDivertidos.length);
    setMensaje(mensajesDivertidos[randomIndex]);
  }, []);

  const handleNavigation = () => {
    navigate('/products');
  };

  return (
    <div
      className={`aside-interactivo ${isOpen ? 'open' : ''}`}
      onMouseEnter={() => setIsOpen(true)}
      onMouseLeave={() => setIsOpen(false)}
    >
      <div className="aside-handle">
        <FontAwesomeIcon icon={faUtensils} style={{ color: 'grey' }} />
      </div>
      <div className="aside-content">
        <p className="lead">
          {mensaje}
        </p>
        <Button variant="light" size="sm" onClick={handleNavigation}>
          Ver Menú
        </Button>
      </div>
    </div>
  );
};

export default AsideInteractivo;