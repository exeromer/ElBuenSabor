// Nueva carpeta/ElBuenSabor-frontend/src/components/auth/PrivateRoute.tsx
import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import Loading from './Loading';
import { Alert, Container, Button } from 'react-bootstrap';
import { UsuarioService } from '../../services/usuarioService';
import type { Usuario } from '../../types/types'; // Importación de tipo

interface PrivateRouteProps {
  component: React.ComponentType;
  requiredRoles?: string[];
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ component: Component, requiredRoles }) => {
  const { isAuthenticated, isLoading, getAccessTokenSilently, user } = useAuth0();
  const navigate = useNavigate();

  const [hasAccess, setHasAccess] = useState<boolean | null>(null); // Declaración correcta
  const [, setUserRole] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const usuarioService = new UsuarioService();

  useEffect(() => {
    const checkUserRoleAndAccess = async () => {
      if (isLoading) {
        setHasAccess(null);
        return;
      }

      if (!isAuthenticated) {
        console.warn('Usuario no autenticado. Redirigiendo al inicio.');
        navigate('/');
        setHasAccess(false);
        return;
      }

      try {
        const token = await getAccessTokenSilently({
          authorizationParams: {
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            scope: import.meta.env.VITE_AUTH0_SCOPE,
          },
        });

        const auth0Id = user?.sub;
        if (!auth0Id) {
          setError('No se pudo obtener el ID de Auth0 del usuario. Acceso denegado.');
          setHasAccess(false);
          console.error('Auth0 ID no disponible en el objeto user.');
          navigate('/');
          return;
        }

        const backendUser: Usuario = await usuarioService.getUsuarioByAuth0Id(auth0Id, token);
        
        const roleFromBackend = backendUser.rol;
        setUserRole(roleFromBackend);

        if (requiredRoles && requiredRoles.length > 0) {
          if (requiredRoles.includes(roleFromBackend)) {
            setHasAccess(true);
          } else {
            const errorMessage = `Acceso denegado. Rol requerido: ${requiredRoles.join(', ')}. Tu rol: ${roleFromBackend}.`;
            setError(errorMessage);
            setHasAccess(false);
            console.warn(errorMessage);
            navigate('/');
          }
        } else {
          setHasAccess(true); // <-- CORRECCIÓN AQUÍ
        }

      } catch (err) {
        console.error("Error al verificar el rol del usuario:", err);
        setError('Error al verificar tus permisos. Intenta de nuevo o contacta al soporte.');
        setHasAccess(false); // <-- CORRECCIÓN AQUÍ
        navigate('/');
      }
    };

    checkUserRoleAndAccess();
  }, [isAuthenticated, isLoading, getAccessTokenSilently, user, navigate, requiredRoles]);

  if (isLoading || hasAccess === null) {
    return <Loading />;
  }

  if (error) {
    return (
      <Container className="my-5">
        <Alert variant="danger" className="text-center">
          <Alert.Heading>¡Acceso Denegado!</Alert.Heading>
          <p>{error}</p>
          <hr />
          <p>
            No tienes los permisos necesarios para ver esta página, o hubo un problema al verificar tu identidad.
          </p>
          <Button variant="danger" onClick={() => navigate('/')}>Volver al inicio</Button>
        </Alert>
      </Container>
    );
  }

  return hasAccess ? <Component /> : null;
};

export default PrivateRoute;