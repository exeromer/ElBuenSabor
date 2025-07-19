import React, { useEffect, useState, useCallback } from 'react';
import { Container, Alert, Card, Button } from 'react-bootstrap';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';

import { ClienteService } from '../services/ClienteService';
import type { ClienteResponse } from '../types/types';
import Titulo from '../components/utils/Titulo/Titulo';
import ClientForm from '../components/admin/ClientForm';
import FullScreenSpinner from '../components/utils/Spinner/FullScreenSpinner';

const ProfilePage: React.FC = () => {
    const { isAuthenticated, isLoading: authLoading } = useAuth0();
    const navigate = useNavigate();

    const [cliente, setCliente] = useState<ClienteResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isNewUser, setIsNewUser] = useState(false);
    const [showForm, setShowForm] = useState(false);

    const fetchProfile = useCallback(async () => {
        if (authLoading || !isAuthenticated) return;
        setLoading(true);
        try {
            const profileData = await ClienteService.getMiPerfil();
            setCliente(profileData);
            if (profileData.nombre === 'Nuevo' && profileData.apellido === 'Cliente') {
                setIsNewUser(true);
                setShowForm(true);
            }
        } catch (err: any) {
            setError(err.message || 'No se pudo cargar tu perfil.');
        } finally {
            setLoading(false);
        }
    }, [isAuthenticated, authLoading]);

    useEffect(() => {
        fetchProfile();
    }, [fetchProfile]);

    // CORRECCIÓN: Esta función ahora no necesita argumentos.
    // Solo se encarga de lo que pasa DESPUÉS de que el ClientForm se guarda.
    const handleFormSaved = () => {
        alert('¡Perfil actualizado con éxito!');
        setShowForm(false);
        fetchProfile(); // Recargamos los datos para ver los cambios.
        if (isNewUser) {
            navigate('/');
        }
    };

    if (loading || authLoading) {
        return <FullScreenSpinner />;
    }

    if (error) {
        return <Container className="my-5"><Alert variant="danger">{error}</Alert></Container>;
    }

    return (
        <>
            <Container className="my-4">
                <Titulo texto={isNewUser ? "¡Bienvenido! Completa tu Perfil" : "Mi Perfil"} nivel="titulo" />
                {isNewUser && (
                    <Alert variant="info" className="mt-3">
                        Por favor, completa tus datos para poder realizar pedidos en El Buen Sabor.
                    </Alert>
                )}
                {cliente && !isNewUser && (
                    <Card className="mt-4">
                        <Card.Header as="h5">Información Personal</Card.Header>
                        <Card.Body>
                            <Card.Text><strong>Rol:</strong> {cliente.rolUsuario}</Card.Text>
                            <Card.Text><strong>Nombre:</strong> {cliente.nombre} {cliente.apellido}</Card.Text>
                            <Card.Text><strong>Email:</strong> {cliente.email}</Card.Text>
                            <Card.Text><strong>Teléfono:</strong> {cliente.telefono || 'No especificado'}</Card.Text>
                            <Card.Text><strong>Dirección:</strong> {cliente.domicilios.length > 0 ? cliente.domicilios.map(d => `${d.calle} ${d.numero}, ${d.localidad.nombre}`).join('; ') : 'No especificada'}</Card.Text>
                            <Button variant="primary" onClick={() => setShowForm(true)}>Editar Perfil y Domicilios</Button>
                        </Card.Body>
                    </Card>
                )}
            </Container>

            {cliente && showForm && (
                <ClientForm
                    show={showForm}
                    handleClose={() => setShowForm(false)}
                    onSave={handleFormSaved}
                    clientToEdit={cliente}
                    isProfileMode={true}
                />
            )}
        </>
    );
};

export default ProfilePage;