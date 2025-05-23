/**
 * @file PrivateRoute.tsx
 * @description Componente de ruta privada que controla el acceso a ciertas páginas de la aplicación
 * basándose en el estado de autenticación de Auth0 y los roles de usuario obtenidos del backend.
 * Redirige a los usuarios no autenticados o sin los roles requeridos, y muestra un componente de carga
 * mientras se verifica el acceso.
 *
 * @hook `useAuth0`: Para gestionar el estado de autenticación (isAuthenticated, isLoading, user)
 * y obtener el token de acceso (`getAccessTokenSilently`).
 * @hook `useNavigate`: Para realizar redirecciones programáticas dentro de la aplicación.
 * @hook `useState`: Para gestionar el estado de acceso (`hasAccess`), el rol del usuario (`userRole`),
 * y mensajes de error (`error`).
 * @hook `useEffect`: Contiene la lógica principal para verificar la autenticación y el rol del usuario,
 * y determinar si se permite el acceso a la ruta.
 */
import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate } from 'react-router-dom';
import Loading from './Loading'; // Tu componente de carga
import { Alert, Container, Button } from 'react-bootstrap';
// Se utiliza el apiClient ya configurado para la consistencia
import apiClient from '../../services/apiClient';
import type { Usuario } from '../../types/types'; 
/**
 * @interface PrivateRouteProps
 * @description Propiedades que el componente `PrivateRoute` espera recibir.
 * @property {React.ComponentType} component - El componente que se renderizará si el usuario tiene acceso.
 * @property {string[]} [requiredRoles] - Un array de roles permitidos. Si está vacío o `undefined`,
 * cualquier usuario autenticado tendrá acceso. Los roles deben coincidir con los definidos en tu backend (ej. 'ADMIN', 'EMPLEADO', 'CLIENTE').
 */
interface PrivateRouteProps {
  component: React.ComponentType;
  requiredRoles?: string[]; // Roles que pueden acceder a esta ruta
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ component: Component, requiredRoles }) => {
  /**
   * @hook useAuth0
   * @description Hook para acceder al estado de autenticación y funciones de Auth0.
   */
  const { isAuthenticated, isLoading, getAccessTokenSilently, user } = useAuth0();

  /**
   * @hook useNavigate
   * @description Hook para la navegación programática.
   */
  const navigate = useNavigate();

  /**
   * @state hasAccess
   * @description Estado que indica si el usuario tiene permiso para acceder a la ruta.
   * `null` mientras se verifica, `true` si tiene acceso, `false` si no.
   */
  const [hasAccess, setHasAccess] = useState<boolean | null>(null);

  /**
   * @state userRole
   * @description Almacena el rol del usuario obtenido del backend.
   */
  const [, setUserRole] = useState<string | null>(null);

  /**
   * @state error
   * @description Almacena un mensaje de error si ocurre algún problema durante la verificación de acceso.
   */
  const [error, setError] = useState<string | null>(null);

  /**
   * @hook useEffect
   * @description Lógica principal para verificar el rol del usuario y los permisos de acceso.
   * Se ejecuta cuando cambian el estado de autenticación, carga de Auth0, el objeto `user` de Auth0,
   * o los roles requeridos de la ruta.
   */
  useEffect(() => {
    const checkUserRoleAndAccess = async () => {
      // 1. Esperar a que Auth0 termine de cargar el estado de autenticación
      if (isLoading) {
        setHasAccess(null); // Asegura que el estado sea nulo mientras Auth0 carga
        return;
      }

      // 2. Verificar autenticación
      if (!isAuthenticated) {
        // Si no está autenticado, redirigir a la página principal (o a una ruta de login)
        console.warn('Usuario no autenticado. Redirigiendo al inicio.');
        navigate('/'); // O puedes usar `loginWithRedirect()` si deseas forzar el inicio de sesión
        setHasAccess(false);
        return;
      }

      try {
        // 3. Obtener el token de acceso de Auth0
        const token = await getAccessTokenSilently({
          authorizationParams: {
            audience: import.meta.env.VITE_AUTH0_AUDIENCE,
            scope: import.meta.env.VITE_AUTH0_SCOPE,
          },
        });

        // 4. Obtener el Auth0 ID del usuario
        // El 'sub' (subject) del token de Auth0 es el ID único del usuario en Auth0.
        const auth0Id = user?.sub;
        if (!auth0Id) {
          setError('No se pudo obtener el ID de Auth0 del usuario. Acceso denegado.');
          setHasAccess(false);
          console.error('Auth0 ID no disponible en el objeto user.');
          navigate('/');
          return;
        }

        // 5. Llamar a tu backend para obtener el rol del usuario desde tu base de datos
        // Se utiliza el `apiClient` configurado para consistencia.
        const response = await apiClient.get<Usuario>(`/usuarios/auth0/${auth0Id}`, {
          headers: {
            Authorization: `Bearer ${token}`, // Asegura que el token se envíe
          },
        });

        const backendUser = response.data;
        const roleFromBackend = backendUser.rol; // El rol del usuario según tu DB ('ADMIN', 'EMPLEADO', 'CLIENTE')
        setUserRole(roleFromBackend); // Almacena el rol para posible uso en el UI

        // 6. Verificar si el rol del usuario tiene los permisos requeridos
        if (requiredRoles && requiredRoles.length > 0) {
          // Si se especifican roles requeridos, se verifica si el rol del usuario está incluido
          if (requiredRoles.includes(roleFromBackend)) {
            setHasAccess(true);
          } else {
            // Acceso denegado por rol no permitido
            const errorMessage = `Acceso denegado. Rol requerido: ${requiredRoles.join(', ')}. Tu rol: ${roleFromBackend}.`;
            setError(errorMessage);
            setHasAccess(false);
            console.warn(errorMessage);
            navigate('/'); // Redirigir a una página de no autorizado o a la home
          }
        } else {
          // Si `requiredRoles` no está definido o está vacío, cualquier usuario autenticado tiene acceso
          setHasAccess(true);
        }

      } catch (err) {
        // 7. Manejo de errores durante la verificación de acceso
        console.error("Error al verificar el rol del usuario:", err);
        setError('Error al verificar tus permisos. Intenta de nuevo o contacta al soporte.');
        setHasAccess(false);
        navigate('/'); // Redirigir a la home o a una página de error
      }
    };

    // Llama a la función de verificación cuando el efecto se dispara
    checkUserRoleAndAccess();
  }, [isAuthenticated, isLoading, getAccessTokenSilently, user, navigate, requiredRoles]);
  // Dependencias: el efecto se re-ejecuta si alguna de estas cambia.

  // 8. Mostrar componente de carga o error según el estado
  // Muestra el componente de carga mientras Auth0 está cargando o mientras se verifica el acceso
  if (isLoading || hasAccess === null) {
    return <Loading />;
  }

  // Si hay un error de acceso, muestra una alerta al usuario
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

  // 9. Renderizar el componente si el acceso está concedido, de lo contrario, nada (ya se redirigió)
  return hasAccess ? <Component /> : null;
};

export default PrivateRoute;