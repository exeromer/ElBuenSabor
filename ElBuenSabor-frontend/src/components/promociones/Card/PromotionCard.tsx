// src/components/promociones/Card/PromotionCard.tsx
import React from 'react';
import { Card, Button } from 'react-bootstrap';
import type { PromocionResponse } from '../../../types/types';
import { useCart } from '../../../context/CartContext';
import { useUser } from '../../../context/UserContext'; // Para saber si el usuario puede comprar
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faShoppingCart } from '@fortawesome/free-solid-svg-icons';
import './PromotionCard.sass';
import toast from 'react-hot-toast';

interface PromotionCardProps {
  promocion: PromocionResponse;
}

const PromotionCard: React.FC<PromotionCardProps> = ({ promocion }) => {
  const { addToCart } = useCart();
  const { userRole } = useUser(); // Obtenemos el rol del usuario

  const defaultImage = '/placeholder-image.png';
  const imageUrl = promocion.imagenes?.[0]?.denominacion || defaultImage;

  const handleComprar = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation(); // Evita que el click se propague a otros elementos

    // Solo los clientes pueden comprar
    if (userRole !== 'CLIENTE') {
      toast.error('Debes iniciar sesión como cliente para comprar.');
      return;
    }

    // Recorremos los artículos de la promoción y los añadimos al carrito
    const promesas = promocion.detallesPromocion.map(detalle => {
      // Necesitamos un objeto `ArticuloResponse` completo para `addToCart`
      // Lo creamos a partir de la info que tenemos en `detalle.articulo`
      const articuloParaAgregar = {
        id: detalle.articulo.id,
        denominacion: detalle.articulo.denominacion,
        precioVenta: detalle.articulo.precioVenta,
        // Añadimos propiedades dummy para cumplir con el tipo `ArticuloResponse`
        type: 'manufacturado' as const, // O determinar el tipo real si es posible
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

    // Usamos Promise.all para esperar que todos los artículos se añadan
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
      <Card.Img variant="top" src={imageUrl} style={{ height: '180px', objectFit: 'cover' }} />
      <Card.Body>
        <Card.Title>{promocion.denominacion}</Card.Title>
        <Card.Text className="text-muted">{promocion.descripcionDescuento}</Card.Text>
        
        {/* Solo mostramos el botón si el usuario es un cliente */}
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