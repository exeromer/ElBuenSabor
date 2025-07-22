import React, { useState } from 'react';
import { Container, Card, Button, Badge } from 'react-bootstrap';
import { SucursalService } from '../services/sucursalService';
import type { SucursalResponse } from '../types/types';
import { useSearchableData } from '../hooks/useSearchableData';
import { SearchableTable, type ColumnDefinition } from '../components/common/Tables/SearchableTable';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEdit, faToggleOff, faToggleOn } from '@fortawesome/free-solid-svg-icons';
import Titulo from '../components/utils/Titulo/Titulo';
import SucursalForm from '../components/admin/SucursalForm';

const ManageSucursalesPage: React.FC = () => {

    // Hook para la data y la búsqueda
    const {
        items: sucursales,
        isLoading,
        error,
        reload,
        ...searchableTableProps
    } = useSearchableData<SucursalResponse>({
        fetchData: SucursalService.getAll
    });

    // Estados para el modal del formulario
    const [showForm, setShowForm] = useState(false);
    const [editingSucursal, setEditingSucursal] = useState<SucursalResponse | null>(null);

    // Abre el formulario para editar una sucursal
    const handleEdit = (sucursal: SucursalResponse) => {
        setEditingSucursal(sucursal);
        setShowForm(true);
    };

    // Abre el formulario para crear una nueva sucursal
    const handleCreate = () => {
        setEditingSucursal(null);
        setShowForm(true);
    };

    // Cierra el formulario y recarga la tabla
    const handleSave = () => {
        setShowForm(false);
        reload();
    };

    // Maneja la baja/alta lógica de una sucursal
const handleToggleActive = async (sucursal: SucursalResponse) => {
        const action = sucursal.estadoActivo ? 'dar de baja' : 'reactivar';
        if (window.confirm(`¿Estás seguro de que quieres ${action} la sucursal "${sucursal.nombre}"?`)) {
            try {
                if (sucursal.estadoActivo) {
                    // Si está activa, usamos el servicio de DELETE para la baja lógica
                    await SucursalService.delete(sucursal.id);
                    alert(`Sucursal ${sucursal.nombre} dada de baja correctamente.`);
                } else {
                    const sucursalRequest: any = { 
                        nombre: sucursal.nombre,
                        horarioApertura: sucursal.horarioApertura.substring(0, 5),
                        horarioCierre: sucursal.horarioCierre.substring(0, 5),
                        empresaId: sucursal.empresa.id,
                        categoriaIds: sucursal.categorias.map(c => c.id),
                        promocionIds: sucursal.promociones.map(p => p.id),
                        domicilio: {
                            calle: sucursal.domicilio.calle,
                            numero: sucursal.domicilio.numero,
                            cp: sucursal.domicilio.cp,
                            localidadNombre: sucursal.domicilio.localidad.nombre,
                            provinciaId: sucursal.domicilio.localidad.provincia.id
                        },
                        estadoActivo: true 
                    };
                    await SucursalService.update(sucursal.id, sucursalRequest);
                    alert(`Sucursal ${sucursal.nombre} reactivada correctamente.`);
                }
                reload(); 
            } catch (err: any) {
                alert(`Error al ${action} la sucursal: ${err.message}`);
            }
        }
    };

    // Definición de las columnas para la tabla
    const columns: ColumnDefinition<SucursalResponse>[] = [
        { key: 'id', header: 'ID', renderCell: (s) => s.id, sortable: true },
        { key: 'nombre', header: 'Nombre', renderCell: (s) => s.nombre, sortable: true },
        {
            key: 'domicilio',
            header: 'Domicilio',
            renderCell: (s) => `${s.domicilio.calle} ${s.domicilio.numero}, ${s.domicilio.localidad.nombre}`,
        },
        {
            key: 'horarios',
            header: 'Horarios',
            renderCell: (s) => `${s.horarioApertura} - ${s.horarioCierre}`,
        },
        {
            key: 'estadoActivo',
            header: 'Estado',
            renderCell: (s) => (
                <Badge bg={s.estadoActivo ? 'success' : 'danger'}>
                    {s.estadoActivo ? 'Activa' : 'Inactiva'}
                </Badge>
            ),
        },
    ];

    // Renderiza los botones de acción para cada fila
    const renderRowActions = (sucursal: SucursalResponse) => (
        <>
            <Button variant="info" size="sm" className="me-2" onClick={() => handleEdit(sucursal)} title="Editar">
                <FontAwesomeIcon icon={faEdit} />
            </Button>
            <Button
                variant={sucursal.estadoActivo ? 'danger' : 'success'}
                size="sm"
                onClick={() => handleToggleActive(sucursal)}
                title={sucursal.estadoActivo ? 'Dar de Baja' : 'Reactivar'}
            >
                <FontAwesomeIcon icon={sucursal.estadoActivo ? faToggleOff : faToggleOn} />
            </Button>
        </>
    );

    return (
        <Container className="my-4">
            <Titulo texto="Gestión de Sucursales" nivel="titulo" />
            <Card className="shadow-sm">
                <Card.Body>
                    <SearchableTable
                        items={sucursales}
                        columns={columns}
                        renderRowActions={renderRowActions}
                        isLoading={isLoading}
                        error={error}
                        reload={reload}
                        {...searchableTableProps}
                        createButtonText="Nueva Sucursal"
                        onCreate={handleCreate}
                    />
                </Card.Body>
            </Card>

            {
            <SucursalForm
                show={showForm}
                handleClose={() => setShowForm(false)}
                onSave={handleSave}
                sucursalToEdit={editingSucursal}
            />
            }

        </Container>
    );
};

export default ManageSucursalesPage;