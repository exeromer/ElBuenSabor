/**
 * @file App.tsx
 * @description Componente raíz de la aplicación React.
 * Configura el enrutamiento de la aplicación utilizando `react-router-dom`,
 * definiendo las rutas públicas y protegidas por roles.
 * Integra los componentes principales de layout (Header y Footer) y gestiona
 * los estados globales de carga y error de Auth0 para una experiencia de usuario fluida.
 *
 * @hook `useAuth0`: Para acceder al estado global de carga y errores de la biblioteca Auth0.
 * @component `BrowserRouter`, `Routes`, `Route`: Componentes para el manejo de rutas.
 * @component `Header`: Barra de navegación superior de la aplicación.
 * @component `Footer`: Pie de página de la aplicación.
 * @component `HomePage`, `ProductsPage`, `ProfilePage`, `CheckoutPage`, `MyOrdersPage`,
 * `AdminDashboardPage`, `ManageProductsPage`, `ManageUsersPage`: Las diferentes
 * páginas y vistas de la aplicación.
 * @component `PrivateRoute`: Componente de enrutamiento que aplica lógica de autenticación y autorización por roles.
 * @component `Loading`: Componente de carga que se muestra mientras Auth0 inicializa.
 */
import Header from './components/common/Header/Header'; 
import Footer from './components/common/Footer/Footer'; 
import Loading from './components/auth/Loading'; 
import { useAuth0 } from '@auth0/auth0-react';
import AppRoutes from './AppRoutes';
import {Toaster} from 'react-hot-toast';

/**
 * @function App
 * @description Componente principal de la aplicación.
 * Envuelve toda la aplicación en un `Router` y define la estructura de las rutas.
 * Muestra componentes de carga o error basados en el estado de Auth0.
 * @returns {JSX.Element} El árbol de componentes de la aplicación.
 */
function App() {
  /**
   * @hook useAuth0
   * @description Accede al estado de carga y error global de Auth0.
   */
  const { isLoading, error } = useAuth0();

  // Muestra un componente de carga si Auth0 está inicializando
  if (isLoading) {
    return <Loading />;
  }

  // Muestra un mensaje de error si ocurre un problema con Auth0 (ej. configuración incorrecta)
  if (error) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <div className="text-center">
          <h1>¡Oops! Ha ocurrido un error.</h1>
          <p>Hubo un problema al iniciar sesión o configurar la autenticación.</p>
          <p>Mensaje: {error.message}</p>
          <p>Por favor, intenta de nuevo más tarde o contacta al soporte.</p>
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Toaster/>
      <Header />
      <main className="flex-grow-1">
        <AppRoutes />
      </main>
      <Footer />
    </div>
  );
}

export default App;