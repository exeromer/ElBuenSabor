// src/routes/AppRoutes.tsx (ruta recomendada) o src/AppRoutes.tsx
import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import ProductsPage from './pages/ProductsPage';
import ProfilePage from './pages/ProfilePage';
import CheckoutPage from './pages/CheckoutPage';
import MyOrdersPage from './pages/MyOrdersPage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import ManageProductsPage from './pages/ManageProductsPage';
import ManageUsersPage from './pages/ManageUsersPage';
import PrivateRoute from './components/auth/PrivateRoute';
import { Button } from 'react-bootstrap';


/**
 * @function AppRoutes
 * @description Componente que define todas las rutas de la aplicación,
 * incluyendo rutas públicas y protegidas por roles.
 * @returns {JSX.Element} El árbol de componentes de las rutas.
 */
function AppRoutes() {
  return (
    <Routes>
      {/* Rutas Públicas */}
      <Route path="/" element={<HomePage />} />
      <Route path="/products" element={<ProductsPage />} />

      {/* Rutas Protegidas por Rol de Cliente */}
      {/* Checkout: requiere rol de CLIENTE, ADMIN, EMPLEADO */}
      <Route path="/checkout" element={<PrivateRoute component={CheckoutPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
      {/* Perfil: accesible por CLIENTE, ADMIN, EMPLEADO */}
      <Route path="/profile" element={<PrivateRoute component={ProfilePage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
      {/* Mis Pedidos: requiere rol de CLIENTE, ADMIN, EMPLEADO */}
      <Route path="/mis-pedidos" element={<PrivateRoute component={MyOrdersPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />

      {/* Rutas Protegidas por Rol de Administración/Empleado */}
      {/* Dashboard de Administración: requiere rol de ADMIN o EMPLEADO */}
      <Route path="/admin-dashboard" element={<PrivateRoute component={AdminDashboardPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
      {/* Gestión de Productos: requiere rol de ADMIN o EMPLEADO */}
      <Route path="/manage-products" element={<PrivateRoute component={ManageProductsPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
      {/* Gestión de Usuarios y Clientes: requiere rol de ADMIN */}
      <Route path="/manage-users" element={<PrivateRoute component={ManageUsersPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
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
  );
}

export default AppRoutes;