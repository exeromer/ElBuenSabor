import React, { useState, useCallback } from 'react';
import { Container, Card, Button, Spinner, Alert, Row, Col, ListGroup } from 'react-bootstrap';
import { useSucursal } from '../context/SucursalContext';
import { PedidoService } from '../services/PedidoService';
import { useWebSocket } from '../hooks/useWebSocket';
import { useSearchableData } from '../hooks/useSearchableData';
import type { PedidoResponse } from '../types/types';
import type { Estado } from '../types/enums';
import toast from 'react-hot-toast';
import Titulo from '../components/utils/Titulo/Titulo';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMotorcycle, faMapLocationDot, faCheckCircle, faPhone } from '@fortawesome/free-solid-svg-icons';

const DeliveryPage: React.FC = () => {
    const { selectedSucursal } = useSucursal();

    const fetchPedidosDelivery = useCallback(async () => {
        if (!selectedSucursal) return [];
        return PedidoService.getPedidosDelivery(selectedSucursal.id);
    }, [selectedSucursal]);

    const {
        items: pedidos,
        isLoading,
        error,
        reload,
    } = useSearchableData({ fetchData: fetchPedidosDelivery });

    // WebSocket para recibir actualizaciones en tiempo real
    const deliveryTopic = selectedSucursal ? `/topic/pedidos/sucursal/${selectedSucursal.id}/delivery` : '';
    useWebSocket(deliveryTopic, reload);

    const handleMarcarEntregado = async (pedidoId: number) => {
        if (!selectedSucursal) return;
        
        const promise = PedidoService.updateEstadoEmpleado(pedidoId, selectedSucursal.id, 'ENTREGADO');

        toast.promise(promise, {
            loading: 'Confirmando entrega...',
            success: `¡Pedido #${pedidoId} marcado como ENTREGADO!`,
            error: (err) => `Error: ${err.message || 'No se pudo confirmar la entrega.'}`,
        });

        try {
            await promise;
            reload(); // Recarga la lista para que el pedido desaparezca
        } catch (err) {
            console.error(err);
        }
    };

    const handleOpenGoogleMaps = (domicilio: PedidoResponse['domicilio']) => {
        const address = `${domicilio.calle} ${domicilio.numero}, ${domicilio.localidad.nombre}, ${domicilio.localidad.provincia.nombre}`;
        const url = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(address)}`;
        window.open(url, '_blank');
    };

    if (!selectedSucursal) {
        return <Container className="my-4"><Alert variant="warning">Por favor, selecciona una sucursal para ver los repartos.</Alert></Container>;
    }
    
    return (
        <Container className="my-4">
            <Titulo texto="Gestión de Delivery" nivel="titulo" />
            <Card>
                <Card.Header as="h5">
                    <FontAwesomeIcon icon={faMotorcycle} className="me-2" />
                    Pedidos en Reparto
                </Card.Header>
                <Card.Body>
                    {isLoading && <div className="text-center"><Spinner animation="border" /></div>}
                    {error && <Alert variant="danger">{error}</Alert>}
                    {!isLoading && !error && pedidos.length === 0 && (
                        <Alert variant="info" className="text-center">No hay pedidos para repartir por el momento.</Alert>
                    )}
                    {!isLoading && !error && (
                        <Row xs={1} md={1} lg={2} xl={3} className="g-4">
                            {pedidos.map((pedido) => (
                                <Col key={pedido.id}>
                                    <Card className="h-100 shadow-sm">
                                        <Card.Header className="d-flex justify-content-between">
                                            <strong>Pedido #{pedido.id}</strong>
                                            <span>{pedido.formaPago}</span>
                                        </Card.Header>
                                        <Card.Body>
                                            <Card.Title>{pedido.cliente.nombre} {pedido.cliente.apellido}</Card.Title>
                                            <Card.Subtitle className="mb-2 text-muted">
                                                <FontAwesomeIcon icon={faPhone} /> {pedido.cliente.telefono || 'No disponible'}
                                            </Card.Subtitle>
                                            
                                            <ListGroup variant="flush" className="my-3">
                                                {pedido.detalles.map(detalle => (
                                                    <ListGroup.Item key={detalle.id} className="px-0 py-1">{detalle.cantidad}x {detalle.articulo.denominacion}</ListGroup.Item>
                                                ))}
                                            </ListGroup>

                                            <p><strong>Total a cobrar:</strong> ${pedido.total.toFixed(2)}</p>

                                            <div className="d-grid gap-2">
                                                <Button variant="outline-primary" onClick={() => handleOpenGoogleMaps(pedido.domicilio)}>
                                                    <FontAwesomeIcon icon={faMapLocationDot} className="me-2" />
                                                    Ver en Mapa: {pedido.domicilio.calle} {pedido.domicilio.numero}
                                                </Button>
                                                <Button variant="success" onClick={() => handleMarcarEntregado(pedido.id)}>
                                                    <FontAwesomeIcon icon={faCheckCircle} className="me-2" />
                                                    Marcar como Entregado
                                                </Button>
                                            </div>
                                        </Card.Body>
                                    </Card>
                                </Col>
                            ))}
                        </Row>
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};

export default DeliveryPage;