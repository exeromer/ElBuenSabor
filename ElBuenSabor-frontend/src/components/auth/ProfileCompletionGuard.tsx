import React, { useEffect, useState } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { useNavigate, useLocation } from "react-router-dom";
import { ClienteUsuarioService } from "../../services/clienteUsuarioService";
import FullScreenSpinner from "../utils/Spinner/FullScreenSpinner";

const ProfileCompletionGuard: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const { user, isAuthenticated, isLoading, getAccessTokenSilently } =
    useAuth0();
  const navigate = useNavigate();
  const location = useLocation();
  const [isProfileChecked, setIsProfileChecked] = useState(false);

  useEffect(() => {
    const checkProfile = async () => {
      const roles = (user?.["https://buensabor.com/roles"] as string[]) || [];

      // **INICIO DE CÓDIGO NUEVO:**
      // Flag para indicar que el perfil ya ha sido verificado o que el usuario ya visitó /profile
      const profileInitiallyChecked = localStorage.getItem(
        "profile_initially_checked"
      );
      // **FIN DE CÓDIGO NUEVO**

      // Si el usuario está autenticado, no es ADMIN/EMPLEADO y no está ya en la página de perfil
      if (
        isAuthenticated &&
        !roles.includes("ADMIN") &&
        !roles.includes("EMPLEADO") &&
        location.pathname !== "/profile"
      ) {
        // **INICIO DE CÓDIGO NUEVO: Comprobación del flag de localStorage**
        if (profileInitiallyChecked === "true") {
          setIsProfileChecked(true); // Ya se chequeó antes y se puede continuar
          return; // Salir de la función para no re-redirigir
        }
        // **FIN DE CÓDIGO NUEVO**

        try {
          const token = await getAccessTokenSilently();
          const clienteService = new ClienteUsuarioService();
          const profile = await clienteService.getMyProfile(token);

          // Si el perfil aún tiene los valores predeterminados, redirigir
          if (profile.nombre === "Nuevo" && profile.apellido === "Cliente") {
            console.warn(
              "Perfil incompleto detectado. Redirigiendo a /profile."
            ); // Log para depuración
            navigate("/profile", { replace: true });
            // **INICIO DE CÓDIGO NUEVO: Marcar que se ha iniciado el proceso de verificación**
            // No marcamos 'true' aquí directamente, porque la redirección significa que aún no se ha completado.
            // La bandera se marcará cuando el usuario aterrice en /profile y potencialmente lo complete.
          } else {
            // Perfil completo, marcar como verificado y permitir el acceso
            console.log("Perfil completo. Acceso permitido."); // Log para depuración
            setIsProfileChecked(true);
            // **INICIO DE CÓDIGO NUEVO: Marcar que el perfil ha sido revisado exitosamente**
            localStorage.setItem("profile_initially_checked", "true");
            // **FIN DE CÓDIGO NUEVO**
          }
        } catch (error) {
          console.error("Error al verificar el perfil del cliente:", error);
          // En caso de error, se permite continuar para no bloquear al usuario
          setIsProfileChecked(true);
          // **INICIO DE CÓDIGO NUEVO: En caso de error, también se marca para evitar bucles**
          localStorage.setItem("profile_initially_checked", "true"); // Evitar bucle si hay error de API
          // **FIN DE CÓDIGO NUEVO**
        }
      } else {
        // Si es ADMIN/EMPLEADO, ya está en /profile, o no autenticado, permitir acceso
        setIsProfileChecked(true);
        // **INICIO DE CÓDIGO NUEVO: Si llega a /profile, marcar como verificado (asume que aquí se completa)**
        if (location.pathname === "/profile") {
          localStorage.setItem("profile_initially_checked", "true");
        }
        // **FIN DE CÓDIGO NUEVO**
      }
    };

    if (!isLoading) {
      checkProfile();
    }
  }, [
    isLoading,
    isAuthenticated,
    user,
    navigate,
    location.pathname,
    getAccessTokenSilently,
  ]);

  if (!isProfileChecked || isLoading) {
    return <FullScreenSpinner />;
  }

  return <>{children}</>;
};

export default ProfileCompletionGuard;
