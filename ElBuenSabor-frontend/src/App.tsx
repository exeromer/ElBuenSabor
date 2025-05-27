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
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/common/Header'; 
import Footer from './components/common/Footer'; 
import HomePage from './pages/HomePage';
import ProfilePage from './pages/ProfilePage';
import AdminDashboardPage from './pages/AdminDashboardPage'; 
import PrivateRoute from './components/auth/PrivateRoute'; 
import Loading from './components/auth/Loading'; 
import { useAuth0 } from '@auth0/auth0-react';
import ProductsPage from './pages/ProductsPage';
import CheckoutPage from './pages/CheckoutPage';
import MyOrdersPage from './pages/MyOrdersPage';
import ManageProductsPage from './pages/ManageProductsPage'; 
import ManageUsersPage from './pages/ManageUsersPage';
import { Button } from 'react-bootstrap';

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
    <Router>
      {/* Contenedor principal para la aplicación, con layout flexbox */}
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <Header /> {/* Componente de la barra de navegación */}
        {/* Contenido principal de la aplicación. `flex-grow-1` asegura que ocupe todo el espacio disponible */}
        <main className="flex-grow-1">
          <Routes>
            {/* Rutas Públicas */}
            <Route path="/" element={<HomePage />} />
            <Route path="/products" element={<ProductsPage />} />

            {/* Rutas Protegidas por Rol de Cliente */}
            {/* Checkout: requiere rol de CLIENTE */}
            <Route path="/checkout" element={<PrivateRoute component={CheckoutPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
            {/* Perfil: accesible por CLIENTE, ADMIN, EMPLEADO */}
            <Route path="/profile" element={<PrivateRoute component={ProfilePage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
            {/* Mis Pedidos: requiere rol de CLIENTE */}
            <Route path="/mis-pedidos" element={<PrivateRoute component={MyOrdersPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />

            {/* Rutas Protegidas por Rol de Administración/Empleado */}
            {/* Dashboard de Administración: requiere rol de ADMIN o EMPLEADO */}
            <Route path="/admin-dashboard" element={<PrivateRoute component={AdminDashboardPage} requiredRoles={['ADMIN', 'EMPLEADO', 'CLIENTE']} />} />
            {/* Gestión de Productos: requiere rol de ADMIN o EMPLEADO */}
            <Route path="/manage-products" element={<PrivateRoute component={ManageProductsPage} requiredRoles={['ADMIN', 'EMPLEADO', 'CLIENTE']} />} />
            {/* Gestión de Usuarios y Clientes: requiere rol de ADMIN (los empleados no deberían gestionar usuarios y clientes) */}
            <Route path="/manage-users" element={<PrivateRoute component={ManageUsersPage} requiredRoles={['ADMIN', 'EMPLEADO', 'CLIENTE']} />} />

            {/* Ruta comodín para páginas no encontradas (404) */}
            <Route path="*" element={
              <div className="d-flex justify-content-center align-items-center text-center my-5" style={{ minHeight: '60vh' }}>
                <div>
                  <h1 className="display-4">404</h1>
                  <p className="lead">¡Oops! Página no encontrada.</p>
                  <p>La dirección a la que intentas acceder no existe.</p>
                  <Button variant="primary" onClick={() => window.location.href = '/'}>Volver a la Página Principal</Button>
                </div>
              </div>
            } />
          </Routes>
        </main>
        <Footer /> {/* Componente del pie de página */}
      </div>
    </Router>
  );
}

export default App;