import React, { useEffect } from 'react';
import { useLocation, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useUser } from '../../context/UserContext';
const ProfileCompletionToast = () => (
  <span>
    ¡Bienvenido! Notamos que eres nuevo.
    <Link
      to="/profile"
      style={{ color: '#fff', textDecoration: 'underline', marginLeft: '5px' }}
      onClick={() => toast.dismiss('profile-completion-toast')}
    >
      <b>Completa tu perfil aquí</b>
    </Link>
    {' '}para una mejor experiencia.
  </span>
);

const ProfileCompletionGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { cliente, empleado, userRole, isLoading: isUserContextLoading } = useUser();
  const location = useLocation();

  useEffect(() => {
    const checkProfile = () => {
      const hasShownProfileToast = sessionStorage.getItem('profileToastShown');
      
      // Si el contexto está cargando, o ya estamos en /profile, o ya se mostró el toast, no hacemos nada.
      if (isUserContextLoading || location.pathname === '/profile' || hasShownProfileToast) {
        return;
      }

      // Si el rol es ADMIN, no hacemos nada.
      if (userRole === 'ADMIN') {
        return;
      }
      
      let profileIsIncomplete = false;

      // Verificamos los datos que nos proveyó el contexto.
      if (userRole === 'CLIENTE' && cliente) {
        if (cliente.nombre === 'Nuevo' && cliente.apellido === 'Cliente') {
          profileIsIncomplete = true;
        }
      } else if (userRole === 'EMPLEADO' && empleado) {
        if (empleado.nombre === 'Nuevo' && empleado.apellido === 'Empleado') {
          profileIsIncomplete = true;
        }
      }
      
      if (profileIsIncomplete) {
        toast.custom(
          (t) => (
            <div
              style={{
                opacity: t.visible ? 1 : 0,
                transition: 'all 0.25s ease-in-out',
                backgroundColor: '#0d6efd',
                color: 'white',
                padding: '16px',
                borderRadius: '8px',
                boxShadow: '0 4px 15px rgba(0,0,0,0.1)',
                maxWidth: '450px',
                textAlign: 'center',
                zIndex: 9999,
              }}
            >
              <ProfileCompletionToast />
            </div>
          ),
          { id: 'profile-completion-toast', duration: 10000, position: 'top-center' }
        );
        sessionStorage.setItem('profileToastShown', 'true');
      }
    };

    // La lógica se ejecuta solo cuando el contexto ha terminado de cargar.
    if (!isUserContextLoading) {
      checkProfile();
    }
    
  }, [isUserContextLoading, cliente, empleado, userRole, location.pathname]); // Las dependencias ahora son los datos del contexto.

  // El guardián simplemente renderiza a sus hijos.
  return <>{children}</>;
};

export default ProfileCompletionGuard;