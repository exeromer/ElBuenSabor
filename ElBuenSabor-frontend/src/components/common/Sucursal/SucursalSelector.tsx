import React from 'react';
import { Form } from 'react-bootstrap';
import { useSucursal } from '../../../context/SucursalContext';
import { useCart } from '../../../context/CartContext';
import './SucursalSelector.sass';

const SucursalSelector: React.FC = () => {
  // Obtenemos todo lo que necesitamos de nuestros contextos
  const { sucursales, selectedSucursal, selectSucursal, loading } = useSucursal();
  const { clearCart, isCartOpen } = useCart(); // <-- FIX: Obtenemos clearCart

  // El manejador para el evento onChange del select
  const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const sucursalId = Number(event.target.value);

        if (selectedSucursal && sucursalId !== selectedSucursal.id) {
        const userConfirmation = window.confirm(
            "Al cambiar de sucursal, tu carrito de compras se vaciará. ¿Deseas continuar?"
        );

        if (userConfirmation) {
            selectSucursal(sucursalId);
            clearCart(); // Limpiamos el carrito desde el componente que dispara la acción
        } else {
            // Si el usuario cancela, reseteamos el valor del select al anterior
            event.target.value = String(selectedSucursal.id);
        }
    } else if (!selectedSucursal) {
        selectSucursal(sucursalId);
    }
  };

  return (
    <div className="sucursal-selector-container">
      <Form.Label className="sucursal-selector-label">Sucursal:</Form.Label>
      <Form.Select
        value={selectedSucursal?.id || ''}
        onChange={handleChange}
        disabled={loading || isCartOpen} // Se deshabilita si está cargando O si el carrito está abierto
        aria-label="Selector de sucursal"
        className="sucursal-selector-select"
      >
        {loading ? (
          <option>Cargando sucursales...</option>
        ) : (
          sucursales.map(sucursal => (
            <option key={sucursal.id} value={sucursal.id}>
              {sucursal.nombre}
            </option>
          ))
        )}
      </Form.Select>
      {isCartOpen && (
        <div className="cart-open-overlay" title="Cierra el carrito para cambiar de sucursal"></div>
      )}
    </div>
  );
};

export default SucursalSelector;