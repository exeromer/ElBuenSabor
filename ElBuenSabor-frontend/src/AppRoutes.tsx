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
import CajeroPage from './pages/CajeroPage';
import CocinaPage from './pages/CocinaPage';
import DeliveryPage from './pages/DeliveryPage';
import { Button, Container} from 'react-bootstrap';

// Placeholders para las paginas
const EstadisticasPage = () => <Container className="my-4"><h1>Vista de Estadísticas</h1></Container>;


/**
 * @function AppRoutes
 * @description Componente que define todas las rutas de la aplicación,
 * incluyendo rutas públicas y protegidas por roles.
 * @returns {JSX.Element} El árbol de componentes de las rutas.
 */
function AppRoutes() {
  return (
    <Routes>
      {/* --- Rutas Públicas y de Cliente (sin cambios) --- */}
      <Route path="/" element={<HomePage />} />
      <Route path="/products" element={<ProductsPage />} />
      <Route path="/checkout" element={<PrivateRoute component={CheckoutPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
      <Route path="/profile" element={<PrivateRoute component={ProfilePage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
      <Route path="/mis-pedidos" element={<PrivateRoute component={MyOrdersPage} requiredRoles={['CLIENTE', 'ADMIN', 'EMPLEADO']} />} />
      
      {/* --- Rutas de Administración y Empleados --- */}
      <Route path="/admin-dashboard" element={<PrivateRoute component={AdminDashboardPage} requiredRoles={['ADMIN', 'EMPLEADO']} />} />
      <Route path="/manage-products" element={<PrivateRoute component={ManageProductsPage} requiredRoles={['ADMIN', 'EMPLEADO']} />} />
      <Route path="/manage-users" element={<PrivateRoute component={ManageUsersPage} requiredRoles={['ADMIN']} />} />
      
      {/* --- NUEVAS RUTAS POR ROL --- */}
      <Route path="/cajero" element={<PrivateRoute component={CajeroPage} requiredRoles={['ADMIN', 'EMPLEADO']} />} />
      <Route path="/cocina" element={<PrivateRoute component={CocinaPage} requiredRoles={['ADMIN', 'EMPLEADO']} />} />
      <Route path="/delivery" element={<PrivateRoute component={DeliveryPage} requiredRoles={['ADMIN', 'EMPLEADO']} />} />
      <Route path="/estadisticas" element={<PrivateRoute component={EstadisticasPage} requiredRoles={['ADMIN']} />} />

      {/* Ruta comodín para 404 */}
      <Route path="*" element={
        <div className="d-flex justify-content-center align-items-center text-center my-5" style={{ minHeight: '60vh' }}>
          <div>
            <h1 className="display-4">404</h1>
            <p className="lead">¡Oops! Página no encontrada.</p>
            <Button variant="primary" onClick={() => window.location.href = '/'}>Volver al Inicio</Button>
          </div>
        </div>
      } />
    </Routes>
  );
}

export default AppRoutes;