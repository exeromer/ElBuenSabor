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
├── assets/                  # Activos estáticos (imágenes, etc.)
├── components/              # Componentes React reutilizables
│   ├── admin/               # Formularios y UI para el panel de administración
│   ├── auth/                # Componentes relacionados con la autenticación (ej. PrivateRoute)
│   ├── common/              # Componentes de UI comunes (Header, Footer)
│   ├── products/            # Componentes específicos de productos (ej. ProductCard)
│   └── (otros componentes...)
├── context/                 # Contextos de React para gestión de estado global (ej. CartContext)
├── pages/                   # Componentes de página (vistas principales de la aplicación)
│   └── admin/               # Páginas específicas del panel de administración
├── services/                # Módulos para interactuar con la API RESTful (Axios configurado)
│   ├── apiClient.ts         # Configuración base de Axios y token auth
│   ├── articuloInsumoService.ts
│   ├── articuloManufacturadoService.ts
│   ├── categoriaService.ts
│   ├── clienteUsuarioService.ts
│   ├── domicilioService.ts
│   ├── fileUploadService.ts # Gestión de subida/bajada de archivos e URLs de imagen
│   ├── imagenService.ts     # CRUD de entidades de imagen en DB
│   ├── pedidoService.ts
│   ├── sucursalService.ts
│   ├── ubicacionService.ts  # Países, Provincias, Localidades
│   └── unidadMedidaService.ts
├── types/                   # Definiciones de tipos e interfaces TypeScript globales (types.ts)
└── App.tsx                  # Componente raíz de la aplicación y configuración de rutas
└── main.tsx                 # Punto de entrada de la aplicación

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
Clona el repositorio:

git clone [URL_DEL_REPOSITORIO]
cd el-buen-sabor-frontend
Instala las dependencias:

npm install

Crea un archivo .env en la raíz del proyecto y añade las siguientes variables:

VITE_API_URL=http://localhost:8080/api/v1/buensabor # URL de tu backend
VITE_AUTH0_DOMAIN=your_auth0_domain.auth0.com       # Tu dominio de Auth0
VITE_AUTH0_CLIENT_ID=your_auth0_client_id            # Tu Client ID de Auth0 (SPA Application)
VITE_AUTH0_AUDIENCE=your_auth0_audience              # Tu Audience de Auth0 (identificador de tu API)
VITE_AUTH0_SCOPE=openid profile email                # Scopes requeridos
(Asegúrate de reemplazar los valores your_auth0_... con los de tu configuración real de Auth0 y backend)

Inicia la aplicación en modo desarrollo:

npm run dev

La aplicación debería abrirse en http://localhost:5173 
