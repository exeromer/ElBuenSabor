import React from 'react';
import { Card, Button } from 'react-bootstrap';
import type { PromocionResponse } from '../../../types/types';
import { useCart } from '../../../context/CartContext';
import { useUser } from '../../../context/UserContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShoppingCart } from '@fortawesome/free-solid-svg-icons';
import './PromotionCard.sass';
import toast from 'react-hot-toast';

interface PromotionCardProps {
  promocion: PromocionResponse;
}

const PromotionCard: React.FC<PromotionCardProps> = ({ promocion }) => {
  const { addToCart } = useCart();
  const { userRole } = useUser(); 

  const defaultImage = '/placeholder-image.png';
  const imageUrl = promocion.imagenes?.[0]?.denominacion || defaultImage;

  const handleComprar = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();

    if (userRole !== 'CLIENTE') {
      toast.error('Debes iniciar sesión como cliente para comprar.');
      return;
    }

    const promesas = promocion.detallesPromocion.map(detalle => {
      const articuloParaAgregar = {
        id: detalle.articulo.id,
        denominacion: detalle.articulo.denominacion,
        precioVenta: detalle.articulo.precioVenta,
        type: 'manufacturado' as const,
        estadoActivo: true,
        imagenes: [],
        categoria: { id: 0, denominacion: '', estadoActivo: true },
        unidadMedida: { id: 0, denominacion: '' },
        descripcion: '',
        tiempoEstimadoMinutos: 0,
        preparacion: '',
        manufacturadoDetalles: [],
      };
      return addToCart(articuloParaAgregar, detalle.cantidad);
    });

    Promise.all(promesas)
      .then(() => {
        toast.success(`¡Promoción "${promocion.denominacion}" añadida al carrito!`);
      })
      .catch(error => {
        console.error("Error al añadir promoción al carrito:", error);
        toast.error('No se pudo añadir la promoción al carrito.');
      });
  };


  return (
    <Card className="h-100 shadow-sm promocion-card">
      <Card.Img variant="top" src={imageUrl} />
      <Card.Body>
        <Card.Title>{promocion.denominacion}</Card.Title>
        <Card.Text className="text-muted">{promocion.descripcionDescuento}</Card.Text>
        {userRole === 'CLIENTE' && (
          <Button 
            variant="primary" 
            className="mt-auto btn-comprar-promo" 
            onClick={handleComprar}
          >
            <FontAwesomeIcon icon={faShoppingCart} className="me-2" />
            Comprar
          </Button>
        )}
      </Card.Body>
      <Card.Footer>
        <small>
          Válido del {new Date(promocion.fechaDesde).toLocaleDateString()} al {new Date(promocion.fechaHasta).toLocaleDateString()}
        </small>
      </Card.Footer>
    </Card>
  );
};

export default PromotionCard;