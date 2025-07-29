import React from 'react';
import { Card, Button } from 'react-bootstrap';
import type { PromocionResponse } from '../../../types/types';
import { useCart } from '../../../context/CartContext';
import { useUser } from '../../../context/UserContext';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShoppingCart } from '@fortawesome/free-solid-svg-icons';
import './PromotionCard.sass';
import toast from 'react-hot-toast';
import { ArticuloService } from '../../../services/ArticuloService'; // Importa el servicio de Artículo

interface PromotionCardProps {
  promocion: PromocionResponse;
}

const PromotionCard: React.FC<PromotionCardProps> = ({ promocion }) => {
  const { addToCart } = useCart();
  const { userRole } = useUser();

  const defaultImage = '/placeholder-image.png';
  const imageUrl = promocion.imagenes?.[0]?.denominacion || defaultImage;

  // ▼▼▼ FUNCIÓN 'handleComprar' CORREGIDA ▼▼▼
  const handleComprar = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();

    if (userRole !== 'CLIENTE') {
      toast.error('Debes iniciar sesión como cliente para comprar.');
      return;
    }

    // Usamos toast.promise para una mejor experiencia de usuario
    const promise = (async () => {
      // Iteramos sobre cada artículo que incluye la promoción
      for (const detalle of promocion.detallesPromocion) {
        // 1. Obtenemos la información COMPLETA del artículo desde el backend
        const fullArticle = await ArticuloService.getById(detalle.articulo.id);

        // 2. Añadimos el artículo correcto y completo al carrito
        await addToCart(fullArticle, detalle.cantidad);
      }
    })();

    toast.promise(promise, {
      loading: `Añadiendo "${promocion.denominacion}"...`,
      success: `¡Promoción "${promocion.denominacion}" añadida al carrito!`,
      error: (err) => `Error: ${err.message || 'No se pudo añadir la promoción.'}`,
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