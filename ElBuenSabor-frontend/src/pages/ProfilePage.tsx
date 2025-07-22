import React, { useState, useEffect } from 'react';
import { Container, Alert, Card, Button } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';

import { useUser } from '../context/UserContext';
import Titulo from '../components/utils/Titulo/Titulo';
import ClientForm from '../components/admin/ClientForm';
import EmpleadoForm from '../components/admin/EmpleadoForm';
import FullScreenSpinner from '../components/utils/Spinner/FullScreenSpinner';

const ProfilePage: React.FC = () => {
    const { cliente, empleado, userRole, isLoading: userLoading, refreshUserData } = useUser();
    const { isLoading: authLoading } = useAuth0();
    const navigate = useNavigate();

    const [showForm, setShowForm] = useState(false);
    const [isNewUser, setIsNewUser] = useState(false);
    
    useEffect(() => {
        if (!userLoading) {
            let newUserDetected = false;
            if (userRole === 'CLIENTE' && cliente?.nombre === 'Nuevo' && cliente?.apellido === 'Cliente') {
                newUserDetected = true;
            } else if (userRole === 'EMPLEADO' && empleado?.nombre === 'Nuevo' && empleado?.apellido === 'Empleado') {
                newUserDetected = true;
            }
            setIsNewUser(newUserDetected);
            setShowForm(newUserDetected);
        }
    }, [userLoading, userRole, cliente, empleado]);

    const handleFormSaved = () => {
        alert('¡Perfil actualizado con éxito!');
        setShowForm(false);
        refreshUserData();
        if (isNewUser) {
            navigate('/');
        }
    };

    if (userLoading || authLoading) {
        return <FullScreenSpinner />;
    }

    return (
        <>
            <Container className="my-4">
                <Titulo texto={isNewUser ? "¡Bienvenido! Completa tu Perfil" : "Mi Perfil"} nivel="titulo" />
                {isNewUser && (
                    <Alert variant="info" className="mt-3">
                        Por favor, completa tus datos para una mejor experiencia en El Buen Sabor.
                    </Alert>
                )}

                {/* --- VISTA PARA CLIENTES --- */}
                {userRole === 'CLIENTE' && cliente && !isNewUser && (
                    <Card className="mt-4">
                        <Card.Header as="h5">Información Personal (Cliente)</Card.Header>
                        <Card.Body>
                            <Card.Text><strong>Nombre:</strong> {cliente.nombre} {cliente.apellido}</Card.Text>
                            <Card.Text><strong>Email:</strong> {cliente.email}</Card.Text>
                            <Card.Text><strong>Teléfono:</strong> {cliente.telefono || 'No especificado'}</Card.Text>
                            <Card.Text><strong>Dirección:</strong> {cliente.domicilios.length > 0 ? cliente.domicilios.map(d => `${d.calle} ${d.numero}`).join('; ') : 'No especificada'}</Card.Text>
                            <Button variant="primary" onClick={() => setShowForm(true)}>Editar Perfil y Domicilios</Button>
                        </Card.Body>
                    </Card>
                )}

                {/* --- VISTA PARA EMPLEADOS --- */}
                {userRole === 'EMPLEADO' && empleado && !isNewUser && (
                    <Card className="mt-4">
                        <Card.Header as="h5">Información de Empleado</Card.Header>
                        <Card.Body>
                            <Card.Text><strong>Nombre:</strong> {empleado.nombre} {empleado.apellido}</Card.Text>
                            <Card.Text><strong>Teléfono:</strong> {empleado.telefono}</Card.Text>
                            <Card.Text><strong>Rol:</strong> {empleado.rolEmpleado}</Card.Text>
                            <Card.Text><strong>Usuario:</strong> {empleado.usernameUsuario}</Card.Text>
                            <Button variant="primary" onClick={() => setShowForm(true)}>Editar Perfil</Button>
                        </Card.Body>
                    </Card>
                )}
            </Container>

            {/* --- RENDERIZADO CONDICIONAL DE FORMULARIOS --- */}
            {userRole === 'CLIENTE' && cliente && showForm && (
                <ClientForm
                    show={showForm}
                    handleClose={() => setShowForm(false)}
                    onSave={handleFormSaved}
                    clientToEdit={cliente}
                    isProfileMode={true}
                />
            )}

            {userRole === 'EMPLEADO' && empleado && showForm && (
                <EmpleadoForm
                    show={showForm}
                    handleClose={() => setShowForm(false)}
                    onSave={handleFormSaved}
                    empleadoToEdit={empleado}
                    isProfileMode={true}
                />
            )}
        </>
    );
};

export default ProfilePage;