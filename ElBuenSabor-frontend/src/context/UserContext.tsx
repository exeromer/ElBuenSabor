import React, { createContext, useContext, useState, useEffect, type ReactNode, useCallback } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { setAuthToken } from '../services/apiClient';
import { UsuarioService } from '../services/usuarioService';
import { ClienteService } from '../services/ClienteService';
import { EmpleadoService } from '../services/EmpleadoService';
import type { ClienteResponse, EmpleadoResponse, UsuarioResponse } from '../types/types';
import type { Rol, RolEmpleado } from '../types/enums';

type UserRole = Rol | null;
type EmployeeRole = RolEmpleado | null;

interface UserContextType {
    cliente: ClienteResponse | null;
    empleado: EmpleadoResponse | null;
    userRole: UserRole;
    employeeRole: EmployeeRole;
    isLoading: boolean;
    refreshUserData: () => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const { isAuthenticated, user, getAccessTokenSilently, isLoading: authLoading } = useAuth0();

    const [cliente, setCliente] = useState<ClienteResponse | null>(null);
    const [empleado, setEmpleado] = useState<EmpleadoResponse | null>(null);
    const [userRole, setUserRole] = useState<UserRole>(null);
    const [employeeRole, setEmployeeRole] = useState<EmployeeRole | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const loadUserData = useCallback(async () => {
        if (isAuthenticated && user?.sub) {
            setIsLoading(true);
            try {
                const token = await getAccessTokenSilently();
                setAuthToken(token);
                const backendUser: UsuarioResponse = await UsuarioService.getByAuth0Id(user.sub);
                setCliente(null);
                setEmpleado(null);
                setEmployeeRole(null);
                setUserRole(backendUser.rol); 
              
                if (backendUser.rol === 'EMPLEADO') {
                    const empleadoData = await EmpleadoService.getMiPerfil();
                    setEmpleado(empleadoData);
                    setEmployeeRole(empleadoData.rolEmpleado);
                } else if (backendUser.rol === 'CLIENTE') {
                    const clienteData = await ClienteService.getMiPerfil();
                    setCliente(clienteData);
                }

            } catch (error) {
                console.error("Error al cargar el perfil del usuario desde el backend:", error);
                setCliente(null); setEmpleado(null); setUserRole(null); setEmployeeRole(null);
            } finally {
                setIsLoading(false);
            }
        } else if (!authLoading) {
            setIsLoading(false);
            setCliente(null); setEmpleado(null); setUserRole(null); setEmployeeRole(null);
        }
    }, [isAuthenticated, user, authLoading, getAccessTokenSilently]);

    useEffect(() => {
        loadUserData();
    }, [loadUserData]);

    return (
        <UserContext.Provider value={{ cliente, empleado, userRole, employeeRole, isLoading, refreshUserData: loadUserData }}>
            {children}
        </UserContext.Provider>
    );
};

export const useUser = () => {
    const context = useContext(UserContext);
    if (context === undefined) {
        throw new Error('useUser debe ser usado dentro de un UserProvider');
    }
    return context;
};