// src/components/promociones/PromotionCard.tsx

import React from 'react';
import { Card, Badge } from 'react-bootstrap';
import type { PromocionResponse } from '../../types/types';
import apiClient from '../../services/apiClient';


interface PromotionCardProps {
  promocion: PromocionResponse;
}

const PromotionCard: React.FC<PromotionCardProps> = ({ promocion }) => {
  const defaultImage = '/placeholder-promo.png'; // Puedes crear una imagen por defecto para promos
    const imageUrl = promocion.imagenes?.[0] 
    ? `${apiClient.defaults.baseURL}/files/download/${promocion.imagenes[0].denominacion}`
    : defaultImage;

  return (
    <Card className="h-100 shadow-sm">
      <Card.Img variant="top" src={imageUrl} style={{ height: '180px', objectFit: 'cover' }} />
      <Card.Body>
        <Card.Title>{promocion.denominacion}</Card.Title>
        <Card.Text>{promocion.descripcionDescuento}</Card.Text>
      </Card.Body>
      <Card.Footer>
        <small className="text-muted">
          Válido del {new Date(promocion.fechaDesde).toLocaleDateString()} al {new Date(promocion.fechaHasta).toLocaleDateString()}
        </small>
      </Card.Footer>
    </Card>
  );
};

export default PromotionCard;