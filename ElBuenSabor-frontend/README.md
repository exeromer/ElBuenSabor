🚀 Frontend de El Buen Sabor
Este repositorio contiene el código fuente del frontend de la aplicación web "El Buen Sabor". Desarrollado con React y TypeScript, este proyecto interactúa con una API RESTful de backend (probablemente en Spring Boot) para ofrecer una experiencia completa de gestión de pedidos de comida.

✨ Características Principales
Menú Dinámico: Explora una amplia variedad de Artículos Manufacturados (productos) y filtra por categorías.
Carrito de Compras: Añade productos al carrito, ajusta cantidades y gestiona tu pedido antes de finalizar la compra.
Proceso de Checkout: Flujo de compra guiado para seleccionar sucursal, tipo de envío (Delivery/Take Away), domicilio y forma de pago.
Autenticación y Autorización (Auth0):
Inicio/cierre de sesión seguro mediante Auth0.
Control de acceso a rutas basado en roles de usuario (CLIENTE, EMPLEADO, ADMIN) obtenidos del backend.
Panel de Administración (roles ADMIN/EMPLEADO):
Gestión de Artículos: CRUD completo para Artículos Manufacturados y Artículos Insumo. Incluye carga y eliminación de imágenes.
Gestión de Usuarios y Clientes: CRUD para usuarios y clientes, incluyendo la modificación de roles y el cambio de estado (activar/desactivar).
(Próximamente: Gestión de Pedidos en el dashboard de administración)
Página "Mis Pedidos": Los clientes pueden visualizar su historial de pedidos y el estado actual de cada uno.
Persistencia Local: El carrito de compras se guarda automáticamente en el navegador.

📦 Estructura del Proyecto
El frontend sigue una estructura modular para facilitar la mantenibilidad y escalabilidad:

src/
├── assets/                      # Activos estáticos (imágenes, etc.)
├── components/                  # Componentes React reutilizables
│   ├── admin/                   # Formularios y UI para el panel de administración
│   ├── auth/                    # Componentes relacionados con la autenticación (ej. PrivateRoute)
│   ├── common/                  # Componentes de UI comunes (Header, Footer)
│   ├── products/                # Componentes específicos de productos (ej. ProductCard)
│   └── (otros componentes...)
├── context/                     # Contextos de React para gestión de estado global (ej. CartContext)
├── pages/                       # Componentes de página (vistas principales de la aplicación)
│   └── admin/                   # Páginas específicas del panel de administración
├── services/                    # Módulos para interactuar con la API RESTful (Axios configurado)
│   ├── apiClient.ts             # Configuración base de Axios y token auth
│   ├── articuloInsumoService.ts
│   ├── articuloManufacturadoService.ts
│   ├── categoriaService.ts
│   ├── clienteUsuarioService.ts
│   ├── domicilioService.ts
│   ├── fileUploadService.ts     # Gestión de subida/bajada de archivos e URLs de imagen
│   ├── imagenService.ts         # CRUD de entidades de imagen en DB
│   ├── pedidoService.ts
│   ├── sucursalService.ts
│   ├── ubicacionService.ts      # Países, Provincias, Localidades
│   └── unidadMedidaService.ts
├── types/                       # Definiciones de tipos e interfaces TypeScript globales (types.ts)
└── App.tsx                      # Componente raíz de la aplicación y configuración de rutas
└── main.tsx                     # Punto de entrada de la aplicación

🛠️ Tecnologías Utilizadas
React: Biblioteca de JavaScript para construir interfaces de usuario.
TypeScript: Superset de JavaScript que añade tipado estático.
React Router DOM: Para la navegación y el enrutamiento declarativo.
Axios: Cliente HTTP basado en promesas para las interacciones con la API.
React Bootstrap: Componentes de interfaz de usuario de Bootstrap reescritos para React.
Auth0 (SPA SDK): Plataforma de autenticación y autorización para la gestión de usuarios.
Font Awesome: Biblioteca de iconos escalables.
date-fns: Librería para manipular y formatear fechas.

🚀 Puesta en Marcha (Desarrollo)

1.  **Clona el repositorio:**
    ```bash
    git clone [URL_DEL_REPOSITORIO_FRONTEND]
    cd el-buen-sabor-frontend
    ```

2.  **Instala las dependencias:**
    ```bash
    npm install
    ```

3.  **Variables de Entorno (`.env`):**
    Crea un archivo `.env` en la raíz del proyecto (`el-buen-sabor-frontend/.env`) y añade las siguientes variables con tu configuración específica:
    ```env
    VITE_API_URL=http://localhost:8080/api
    VITE_AUTH0_DOMAIN=dev-e7rix4gh8kwqcqy1.us.auth0.com
    VITE_AUTH0_CLIENT_ID=vL0XaMQlQkj7DrZbFAKXEfAJ6Jtoqj8n
    VITE_AUTH0_AUDIENCE=[https://api.elbuensabor.com]
    VITE_AUTH0_SCOPE=openid profile email
    ```
    * **`VITE_API_URL`**: URL base de tu API backend.
    * **`VITE_AUTH0_DOMAIN`**: Tu dominio de Auth0 (ej. `tu-tenant.us.auth0.com`).
    * **`VITE_AUTH0_CLIENT_ID`**: El Client ID de tu aplicación SPA en Auth0.
    * **`VITE_AUTH0_AUDIENCE`**: El Identifier (Audience) de tu API registrada en Auth0.
    * **`VITE_AUTH0_SCOPE`**: Scopes OAuth solicitados.

4.  **Configuración de Auth0 (Dashboard):**
    * **Aplicación SPA:**
        * **Allowed Callback URLs:** `http://localhost:5173`
        * **Allowed Logout URLs:** `http://localhost:5173`
        * **Allowed Web Origins:** `http://localhost:5173`
    * **API:**
        * Asegúrate de que el **Identifier** de tu API coincida con `VITE_AUTH0_AUDIENCE`.

5.  **Usuarios de Prueba (Crear en Auth0 Dashboard):**
    Para probar la aplicación, crea los siguientes usuarios en tu dashboard de Auth0 (User Management > Users > "+ Create User"):

    * **Contraseña para ambos usuarios:** `#hola1234`

    * **Usuario Cliente:**
        * **Email:** `ana.garcia@example.com`
        * **Nota:** Al iniciar sesión por primera vez, el backend creará una entrada para este usuario en la base de datos interna con el rol `CLIENTE` por defecto.

    * **Usuario Administrador (para acceder al CRUD):**
        * **Email:** `luisluis@admin.com`
        * **Nota:**
            1.  Inicia sesión en el frontend con este usuario. El backend creará una entrada en la tabla `usuario` con rol `CLIENTE`.
            2.  **Accede a tu base de datos MySQL** y modifica la columna `rol` de este usuario a `'ADMIN'`.
            3.  Cierra sesión y vuelve a iniciar sesión para tener permisos de administrador.

6.  **Iniciar la Aplicación:**
    * Asegúrate de que tu servidor backend Spring Boot ("ElBuenSabor") esté corriendo.
    * En la terminal, dentro de la carpeta `el-buen-sabor-frontend`:
        ```bash
        npm run dev
        ```
    La aplicación frontend debería abrirse en `http://localhost:5173`.

