/**
 * @file ProfilePage.tsx
 * @description Página de perfil del usuario que muestra información básica obtenida de Auth0
 * para el usuario autenticado. Incluye el nombre, email, imagen de perfil (si disponible)
 * y una representación JSON del objeto de usuario completo de Auth0.
 * Este componente es principalmente presentacional, mostrando los datos del `user` de Auth0.
 *
 * @hook `useAuth0`: Para acceder al estado de autenticación y la información del usuario (`user`, `isAuthenticated`, `isLoading`).
 */
import React from 'react';
import { Container, Card, Button, Spinner, Alert } from 'react-bootstrap'; // Se añadió Spinner y Alert para la carga
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom'; // Se añade useNavigate para posibles redirecciones

/**
 * @interface ProfilePageProps
 * @description No se requieren propiedades (`props`) para este componente de página,
 * por lo que se define una interfaz vacía para claridad.
 */
interface ProfilePageProps {}

const ProfilePage: React.FC<ProfilePageProps> = () => {
  /**
   * @hook useAuth0
   * @description Hook para acceder al estado de autenticación de Auth0 y la información del usuario.
   * @returns {object} Un objeto con `user` (datos del perfil), `isAuthenticated` (estado de autenticación),
   * y `isLoading` (estado de carga de Auth0).
   */
  const { user, isAuthenticated, isLoading, loginWithRedirect } = useAuth0(); // Se añadió loginWithRedirect

  /**
   * @hook useNavigate
   * @description Hook para la navegación programática.
   */
  const navigate = useNavigate();

  // --- Renderizado condicional basado en estados de carga o autenticación ---
  // Muestra un spinner si Auth0 está cargando la información del usuario
  if (isLoading) {
    return (
      <Container className="text-center my-5">
        <Spinner animation="border" role="status" />
        <p className="mt-3">Cargando perfil...</p>
      </Container>
    );
  }

  // Muestra un mensaje y un botón para iniciar sesión si el usuario no está autenticado
  if (!isAuthenticated || !user) {
    return (
      <Container className="my-5 text-center">
        <Alert variant="info">
          <Alert.Heading>¡Acceso Restringido!</Alert.Heading>
          <p>Por favor, inicia sesión para ver tu perfil y gestionar tu información.</p>
          <hr />
          <Button variant="primary" onClick={() => loginWithRedirect()}>
            Iniciar Sesión
          </Button>
        </Alert>
      </Container>
    );
  }

  return (
    <Container className="my-5">
      <Card className="shadow-sm"> {/* Añadido shadow-sm para una ligera elevación */}
        <Card.Header as="h5" className="bg-primary text-white">Mi Perfil</Card.Header> {/* Color de fondo para el header */}
        <Card.Body>
          {/* Título de la tarjeta, usando el nombre del usuario de Auth0 */}
          <Card.Title className="mb-3">{user.name || user.nickname || 'Usuario'}</Card.Title>
          <Card.Text>
            {/* Muestra el email del usuario */}
            <strong>Email:</strong> {user.email} <br />
            {/* Muestra la imagen de perfil del usuario si está disponible */}
            {user.picture && (
              <img
                src={user.picture}
                alt="Imagen de Perfil"
                className="img-fluid rounded-circle my-3 shadow-sm" // Añadido shadow-sm
                style={{ maxWidth: '120px', height: '120px', objectFit: 'cover' }} // Ajuste de tamaño
              />
            )}{' '}
            <br />
            {/* Sección para mostrar información adicional del backend (futura expansión) */}
            <p className="mt-3 text-muted">
              Aquí podrías integrar información adicional de tu sistema de backend,
              como domicilios, pedidos recientes, o datos de perfil extendidos.
            </p>
            {/* Muestra el objeto `user` completo de Auth0 para depuración */}
            <p className="mt-3">
              Información completa provista por Auth0 (solo para depuración):
              <pre className="bg-light p-3 rounded" style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                {JSON.stringify(user, null, 2)}
              </pre>
            </p>
          </Card.Text>
          {/* Botón para editar el perfil (funcionalidad pendiente) */}
          <Button variant="outline-primary" onClick={() => alert('Funcionalidad de edición de perfil pendiente.')}>
            Editar Perfil
          </Button>
          {/* Puedes añadir un botón para gestionar domicilios, por ejemplo */}
          <Button variant="link" onClick={() => navigate('/admin/clients')} className="ms-2">
            Gestionar mis Domicilios
          </Button>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default ProfilePage;