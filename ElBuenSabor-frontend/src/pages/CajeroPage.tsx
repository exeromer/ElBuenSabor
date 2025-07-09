import React, { useState, useCallback} from 'react';
import { Container, Card, Button, Spinner, Alert, Tabs, Tab, Badge, Form, InputGroup} from 'react-bootstrap';
import { useSucursal } from '../context/SucursalContext';
import { PedidoService } from '../services/PedidoService';
import { useWebSocket } from '../hooks/useWebSocket';
import { useSearchableData } from '../hooks/useSearchableData';
import type { PedidoResponse } from '../types/types';
import type { Estado } from '../types/enums';
import toast from 'react-hot-toast';

// Componente de tabla reutilizable que ya tienes
import { SearchableTable, type ColumnDefinition } from '../components/common/Tables/SearchableTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSearch } from '@fortawesome/free-solid-svg-icons';

const CajeroPage: React.FC = () => {

    // =================================================================================
    // PASO 1: ESTADOS Y HOOKS PRINCIPALES
    // =================================================================================

    // --- Contexto ---
    // Obtenemos la sucursal seleccionada globalmente.
    const { selectedSucursal } = useSucursal();

    // --- Estados del Componente ---
    // Pestaña de estado activa ('PENDIENTE', 'EN_PREPARACION', etc.).
    const [activeTab, setActiveTab] = useState<Estado>('PENDIENTE');
    // Término de búsqueda para el ID del pedido.
    const [pedidoIdSearch, setPedidoIdSearch] = useState('');


    // =================================================================================
    // PASO 2: LÓGICA DE DATOS (BÚSQUEDA, ORDENAMIENTO Y CARGA)
    // =================================================================================

    // --- Carga de Datos ---
    // Definimos la función que `useSearchableData` usará para obtener los pedidos.
    // Se ejecutará cada vez que cambie la sucursal, la pestaña o el término de búsqueda.
    const fetchPedidos = useCallback(async () => {
        if (!selectedSucursal) return [];
        
        // Convertimos el string de búsqueda a número, o undefined si está vacío.
        const id = pedidoIdSearch ? parseInt(pedidoIdSearch, 10) : undefined;
        
        // Llamamos al servicio con todos los filtros.
        return PedidoService.getPedidosCajero(selectedSucursal.id, activeTab, id);
    }, [selectedSucursal, activeTab, pedidoIdSearch]);

    // --- Hook de Datos ---
    // Centralizamos toda la gestión de datos (carga, error, ordenamiento) en este hook.
    const {
        items: pedidos,
        isLoading,
        error,
        reload, // Función para recargar los datos manualmente.
        requestSort,
        sortConfig
    } = useSearchableData({ fetchData: fetchPedidos });


    // =================================================================================
    // PASO 3: LÓGICA EN TIEMPO REAL (WEBSOCKETS)
    // =================================================================================

    // --- Manejador de Mensajes ---
    // Esta función se ejecutará cada vez que llegue un mensaje del WebSocket.
    const handleWebSocketMessage = useCallback((pedido: PedidoResponse) => {
        // Si el pedido recibido pertenece a la pestaña que estamos viendo,
        // simplemente recargamos la tabla para mostrarlo.
        if (pedido.estado === activeTab) {
            reload();
        }
    }, [activeTab, reload]);

    // --- Conexión WebSocket ---
    // Definimos el "tema" (topic) al que nos suscribimos, basado en la sucursal.
    const cajeroTopic = selectedSucursal ? `/topic/pedidos/sucursal/${selectedSucursal.id}/cajero` : '';
    // Usamos el hook para conectarnos y le pasamos nuestro manejador.
    // La lógica de notificación ahora está dentro del hook, así que no se necesita más.
    useWebSocket(cajeroTopic, handleWebSocketMessage);


    // =================================================================================
    // PASO 4: MANEJADORES DE ACCIONES DEL USUARIO
    // =================================================================================

    // Se ejecuta cuando el usuario hace clic en una pestaña.
    const handleTabSelect = (k: string | null) => {
        if (k) {
            setPedidoIdSearch(''); // Limpiamos la búsqueda anterior.
            setActiveTab(k as Estado);
        }
    };

    // Se ejecuta cuando el usuario presiona el botón de buscar.
    const handleSearch = () => {
        reload(); // Forzamos una recarga con el nuevo término de búsqueda.
    };

    // Se ejecuta cuando el cajero cambia el estado de un pedido.
    const handleUpdateEstado = async (pedidoId: number, nuevoEstado: Estado) => {
        if (!selectedSucursal) return;

        const promise = PedidoService.updateEstadoEmpleado(pedidoId, selectedSucursal.id, nuevoEstado);

        toast.promise(promise, {
            loading: 'Actualizando estado...',
            success: `Pedido #${pedidoId} actualizado a ${nuevoEstado}.`,
            error: (err) => `Error: ${err.message || 'No se pudo actualizar'}`,
        });

        try {
            await promise;
            reload(); // Al tener éxito, recargamos los datos para que el pedido desaparezca de la pestaña actual.
        } catch (err) {
            console.error(err); // El toast ya muestra el error, aquí solo lo registramos en consola.
        }
    };


    // =================================================================================
    // PASO 5: DEFINICIÓN DE LA ESTRUCTURA DE LA TABLA
    // =================================================================================

    // Definimos cómo se verá cada columna en nuestra tabla.
    const columns: ColumnDefinition<PedidoResponse>[] = [
        { key: 'id', header: 'ID', renderCell: (p) => p.id, sortable: true },
        { key: 'cliente.nombre', header: 'Cliente', renderCell: (p) => `${p.cliente.nombre} ${p.cliente.apellido}` },
        { key: 'fechaPedido', header: 'Fecha', renderCell: (p) => new Date(p.fechaPedido).toLocaleDateString(), sortable: true },
        {
            key: 'formaPago', header: 'Método de Pago', renderCell: (p) => (
                <div>
                    {p.formaPago}
                    {p.formaPago === 'MERCADO_PAGO' && <Badge bg="success" className="ms-2">Pagado</Badge>}
                    {p.formaPago === 'EFECTIVO' && <Badge bg="warning" className="ms-2">Pendiente</Badge>}
                </div>
            )
        },
        { key: 'estado', header: 'Estado Actual', renderCell: (p) => <Badge bg="info">{p.estado}</Badge> },
    ];

    // Definimos qué botones de acción se mostrarán según el estado del pedido.
    const renderRowActions = (pedido: PedidoResponse) => {
        switch (pedido.estado) {
            case 'PENDIENTE':
                return (
                    <>
                        <Button variant="danger" size="sm" className="me-2" onClick={() => handleUpdateEstado(pedido.id, 'RECHAZADO')}>Rechazar</Button>
                        <Button variant="success" size="sm" onClick={() => handleUpdateEstado(pedido.id, 'PREPARACION')}>Confirmar</Button>
                    </>
                );
            case 'LISTO':
                return (
                    <>
                        {pedido.tipoEnvio === 'DELIVERY' &&
                            <Button variant="primary" size="sm" className="me-2" onClick={() => handleUpdateEstado(pedido.id, 'EN_CAMINO')}>A Delivery</Button>
                        }
                        {pedido.tipoEnvio === 'TAKEAWAY' &&
                            <Button variant="success" size="sm" onClick={() => handleUpdateEstado(pedido.id, 'ENTREGADO')}>Entregar</Button>
                        }
                    </>
                );
            default:
                return <span className="text-muted">Sin acciones</span>;
        }
    };

        return (
        <Container className="my-4">
            <h1 className="text-center mb-4">Gestión de Pedidos (Cajero)</h1>
            <Tabs activeKey={activeTab} onSelect={handleTabSelect} id="pedidos-tabs" className="mb-3" fill>
                <Tab eventKey="PENDIENTE" title="A Confirmar" />
                <Tab eventKey="PREPARACION" title="En Cocina" />
                <Tab eventKey="LISTO" title="Listos" />
                <Tab eventKey="EN_CAMINO" title="En Delivery" />
                <Tab eventKey="ENTREGADO" title="Entregados" />
            </Tabs>
            <Card>
                <Card.Body>
                    {isLoading ? (
                        <div className="text-center"><Spinner animation="border" /></div>
                    ) : error ? (
                        <Alert variant="danger">{error}</Alert>
                    ) : (
                        <SearchableTable
                            items={pedidos}
                            columns={columns}
                            renderRowActions={renderRowActions}
                            isLoading={isLoading}
                            error={error}
                            reload={reload} // Usamos reload del hook
                            // Pasamos las propiedades para búsqueda y ordenamiento
                            searchTerm={pedidoIdSearch}
                            setSearchTerm={setPedidoIdSearch}
                            sortConfig={sortConfig}
                            requestSort={requestSort}
                            searchPlaceholder="Buscar por ID..." // Placeholder actualizado
                        />
                    )}
                </Card.Body>
            </Card>
        </Container>
    );
};
export default CajeroPage;