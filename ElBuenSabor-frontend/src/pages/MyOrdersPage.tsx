import React, { useEffect, useState, useCallback } from 'react';
import { Container, Table, Spinner, Alert, Button, Badge } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { Link } from 'react-router-dom';

import { PedidoService } from '../services/pedidoService';
import type { PedidoResponse, FacturaResponse } from '../types/types';
import type { Estado } from '../types/enums';

import Titulo from '../components/utils/Titulo/Titulo';
import PedidoDetailModal from '../components/pedidos/PedidoDetailModal'; 
import FacturaDetailModal from '../components/facturas/FacturaDetailModal'; 
import { FacturaService } from '../services/FacturaService'; 

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEye, faFileInvoiceDollar, faCheckCircle, faTimesCircle, faCog, faTruck, faClock } from '@fortawesome/free-solid-svg-icons';

const MyOrdersPage: React.FC = () => {
  const { isAuthenticated, isLoading: authLoading } = useAuth0();
  const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Estados para los modales
  const [showPedidoModal, setShowPedidoModal] = useState(false);
  const [showFacturaModal, setShowFacturaModal] = useState(false);
  const [selectedPedido, setSelectedPedido] = useState<PedidoResponse | null>(null);
  const [selectedFactura, setSelectedFactura] = useState<FacturaResponse | null>(null);

  const fetchMyOrders = useCallback(async () => {
    if (authLoading || !isAuthenticated) {
      if (!authLoading) {
        setError('Debes iniciar sesión para ver tus pedidos.');
        setLoading(false);
      }
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const fetchedPedidos = await PedidoService.getMisPedidos();
      setPedidos(fetchedPedidos);
    } catch (err) {
      console.error('Error al obtener mis pedidos:', err);
      setError('No se pudieron cargar tus pedidos.');
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, authLoading]);

  useEffect(() => {
    fetchMyOrders();
  }, [fetchMyOrders]);

  const handleShowPedido = (pedido: PedidoResponse) => {
    setSelectedPedido(pedido);
    setShowPedidoModal(true);
  };
  
  const handleShowFactura = async (facturaId: number) => {
    setShowFacturaModal(true);
    setSelectedFactura(null); 
    try {
      const facturaCompleta = await FacturaService.findByIdIncludingAnuladas(facturaId);
      setSelectedFactura(facturaCompleta);
    } catch (error) {
      console.error("Error al cargar detalle de la factura:", error);
      setError("No se pudo cargar la factura.");
    }
  };

  const getEstadoBadge = (estado: Estado) => {
    let bg = 'secondary';
    let icon = faCog;
    switch (estado) {
      case 'PENDIENTE': bg = 'warning'; icon = faClock; break;
      case 'PREPARACION': bg = 'info'; icon = faCog; break;
      case 'EN_CAMINO': bg = 'primary'; icon = faTruck; break;
      case 'ENTREGADO': bg = 'success'; icon = faCheckCircle; break;
      case 'CANCELADO':
      case 'RECHAZADO': bg = 'danger'; icon = faTimesCircle; break;
      case 'LISTO': bg = 'success'; icon = faCheckCircle; break;
    }
    return <Badge bg={bg}><FontAwesomeIcon icon={icon} className="me-1" /> {estado}</Badge>;
  };

  if (loading || authLoading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status" />
        <p className="mt-3">Cargando tus pedidos...</p>
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="danger">{error}</Alert>
      </Container>
    );
  }

  return (
    <>
      <Container className="my-4">
        <Titulo texto="Mi Historial de Pedidos" nivel="titulo" />

        {pedidos.length === 0 ? (
          <Alert variant="info" className="text-center">
            Aún no has realizado ningún pedido.
            <div className="mt-3">
              <Link to="/products"><Button variant="primary">¡Empieza a Comprar!</Button></Link>
            </div>
          </Alert>
        ) : (
          <Table striped bordered hover responsive className="text-center align-middle shadow-sm">
            <thead>
              <tr>
                <th>N° Pedido</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Total</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {pedidos.map((pedido) => (
                <tr key={pedido.id}>
                  <td>#{pedido.id}</td>
                  <td>{new Date(pedido.fechaPedido).toLocaleDateString()}</td>
                  <td>{getEstadoBadge(pedido.estado)}</td>
                  <td>${pedido.total.toFixed(2)}</td>
                  <td>
                    <Button variant="outline-info" size="sm" onClick={() => handleShowPedido(pedido)} className="me-2" title="Ver Detalle">
                      <FontAwesomeIcon icon={faEye} />
                    </Button>
                    {pedido.factura && (
                      <Button variant="outline-primary" size="sm" onClick={() => handleShowFactura(pedido.factura!.id)} title="Ver Factura">
                        <FontAwesomeIcon icon={faFileInvoiceDollar} />
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
      </Container>
      
      {/* Modales */}
      <PedidoDetailModal show={showPedidoModal} onHide={() => setShowPedidoModal(false)} pedido={selectedPedido} />
      <FacturaDetailModal show={showFacturaModal} onHide={() => setShowFacturaModal(false)} factura={selectedFactura} />
    </>
  );
};

export default MyOrdersPage;