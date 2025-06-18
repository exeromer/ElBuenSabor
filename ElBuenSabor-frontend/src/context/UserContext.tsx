// Nueva carpeta/ElBuenSabor-frontend/src/context/UserContext.tsx
import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { ClienteUsuarioService } from '../services/clienteUsuarioService';
import type { Cliente } from '../types/types';

interface UserContextType {
    cliente: Cliente | null;
    isLoading: boolean;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { isAuthenticated, user, getAccessTokenSilently, isLoading: authLoading } = useAuth0();
    const [cliente, setCliente] = useState<Cliente | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const loadUserData = async () => {
            if (isAuthenticated && user?.sub) {
                setIsLoading(true);
                try {
                    const token = await getAccessTokenSilently();
                    const service = new ClienteUsuarioService();
                    const clienteData = await service.getMyProfile(token);
                    setCliente(clienteData);
                } catch (error) {
                    setCliente(null);
                } finally {
                    setIsLoading(false);
                }
            } else if (!authLoading) {
                setIsLoading(false);
                setCliente(null);
            }
        };
        loadUserData();
    }, [isAuthenticated, user, authLoading]);

    return (
        <UserContext.Provider value={{ cliente, isLoading }}>
            {children}
        </UserContext.Provider>
    );
};

export const useUser = () => {
    const context = useContext(UserContext);
    if (context === undefined) {
        throw new Error('useUser must be used within a UserProvider');
    }
    return context;
};