import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ClienteUsuarioService } from '../../services/clienteUsuarioService';
import FullScreenSpinner from '../utils/Spinner/FullScreenSpinner';

const ProfileCompletionGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { user, isAuthenticated, isLoading, getAccessTokenSilently } = useAuth0();
    const navigate = useNavigate();
    const location = useLocation();
    const [isProfileChecked, setIsProfileChecked] = useState(false);

    useEffect(() => {
        const checkProfile = async () => {
            const roles = user?.['https://buensabor.com/roles'] as string[] || [];
            if (isAuthenticated && !roles.includes('ADMIN') && !roles.includes('EMPLEADO') && location.pathname !== '/profile') {
                try {
                    const token = await getAccessTokenSilently();
                    // CORRECCIÓN: Se crea una instancia del servicio para usar sus métodos.
                    const clienteService = new ClienteUsuarioService();
                    const profile = await clienteService.getMyProfile(token);
                    
                    if (profile.nombre === 'Nuevo' && profile.apellido === 'Cliente') {
                        navigate('/profile', { replace: true });
                    } else {
                        setIsProfileChecked(true);
                    }
                } catch (error) {
                    console.error("Error al verificar el perfil del cliente:", error);
                    setIsProfileChecked(true); // Se deja continuar para no bloquear al usuario en caso de error.
                }
            } else {
                setIsProfileChecked(true);
            }
        };

        if (!isLoading) {
            checkProfile();
        }
    }, [isLoading, isAuthenticated, user, navigate, location.pathname, getAccessTokenSilently]);

    if (!isProfileChecked || isLoading) {
        return <FullScreenSpinner />;
    }

    return <>{children}</>;
};

export default ProfileCompletionGuard;