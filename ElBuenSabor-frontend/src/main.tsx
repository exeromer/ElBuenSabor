
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/main.sass';

import { BrowserRouter as Router } from 'react-router-dom';
import { Auth0Provider } from '@auth0/auth0-react';
import { UserProvider } from './context/UserContext.tsx';
import { CartProvider } from './context/CartContext.tsx';
import { SucursalProvider } from './context/SucursalContext.tsx';

const AUTH0_DOMAIN = import.meta.env.VITE_AUTH0_DOMAIN;
const AUTH0_CLIENT_ID = import.meta.env.VITE_AUTH0_CLIENT_ID;
const AUTH0_AUDIENCE = import.meta.env.VITE_AUTH0_AUDIENCE;

if (!AUTH0_DOMAIN || !AUTH0_CLIENT_ID || !AUTH0_AUDIENCE) {
  console.error("Auth0 environment variables are not set. Please check your .env file.");
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {AUTH0_DOMAIN && AUTH0_CLIENT_ID && AUTH0_AUDIENCE ? (
      <Auth0Provider
        domain={AUTH0_DOMAIN}
        clientId={AUTH0_CLIENT_ID}
        authorizationParams={{
          redirect_uri: window.location.origin,
          audience: AUTH0_AUDIENCE,
          scope: import.meta.env.VITE_AUTH0_SCOPE || 'openid profile email',
        }}
        useRefreshTokens={true}
        cacheLocation="localstorage"
      >
        <Router>
          <UserProvider>
            <SucursalProvider>
              <CartProvider>
                <App />
              </CartProvider>
            </SucursalProvider>
          </UserProvider>
        </Router>
      </Auth0Provider>
    ) : (
      <div>Error: Configuración de autenticación faltante. Por favor, revisa tus variables de entorno.</div>
    )}
  </React.StrictMode>,
);