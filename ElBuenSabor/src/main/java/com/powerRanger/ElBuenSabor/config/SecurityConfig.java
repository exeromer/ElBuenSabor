package com.powerRanger.ElBuenSabor.config;

import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!dev")
public class SecurityConfig {


    @Autowired
    private UsuarioService usuarioService;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // =================================================================================
                                // 1. ENDPOINTS PÚBLICOS - Accesibles por CUALQUIERA (autenticado o no)
                                // =================================================================================
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Pre-flight requests de CORS
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/sucursales", "/api/sucursales/**").permitAll()
                                .requestMatchers("/api/ws/**").permitAll()
                                .requestMatchers(
                                        "/api/mercado-pago/notificaciones",
                                        "/payment/**", // Permite /payment/success, /payment/failure, etc.
                                        "/favicon.ico"
                                ).permitAll()
                                .requestMatchers("/ws/**").permitAll() // Websockets

                                // Endpoints públicos de la API
                                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/files/view/{filename}").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/files/view/**").permitAll()


                                // Endpoints de LECTURA de catálogos (Artículos, Categorías, etc.)
                                .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/unidadesmedida/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/articulos/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/articulosinsumo/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/articulosmanufacturados/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/promociones/**").permitAll()

                                // =================================================================================
                                // 2. ENDPOINTS PARA USUARIOS AUTENTICADOS - Requieren login, sin importar el rol
                                // =================================================================================
                                .requestMatchers(HttpMethod.GET, "/api/usuarios/auth0/{auth0Id}").authenticated()

                                // =================================================================================
                                // 3. REGLAS ESPECÍFICAS POR ROL
                                // =================================================================================
                                // ---- CLIENTE ----
                                .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST, "/api/pedidos/cliente/{clienteId}/desde-carrito").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST, "/api/pedidos/crearDesdeCarrito").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.PUT, "/api/clientes/perfil").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.GET, "/api/clientes/{clienteId}/carrito").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST, "/api/clientes/{clienteId}/carrito/items").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.PUT, "/api/clientes/{clienteId}/carrito/items/{carritoItemId}").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.DELETE, "/api/clientes/{clienteId}/carrito/items/**").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers("/api/carrito/**").hasAuthority("ROLE_CLIENTE") // Rutas del carrito
                                .requestMatchers("/api/clientes/perfil").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers("/api/domicilios/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")

                                // ---- CLIENTE y ADMIN ----
                                .requestMatchers("/api/clientes/perfil").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers("/api/domicilios/**").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST, "/api/pedidos/cliente/{clienteId}/desde-carrito").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers("/api/carrito/**").hasAuthority("ROLE_CLIENTE")

                                // ---- EMPLEADOS (Cualquier tipo) y ADMIN ----
                                .requestMatchers(HttpMethod.POST, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.POST, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.DELETE, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/pedidos/estado/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/pedidos/{id}/estado").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT,"/api/facturas/anular/{id}").hasAnyAuthority("ROLE_ADMIN","ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.POST,"/api/facturas/generar-desde-pedido").hasAnyAuthority("ROLE_ADMIN","ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.POST, "/api/files/upload").hasAnyAuthority( "ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.POST, "/api/promociones/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/promociones/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.DELETE, "/api/promociones/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")

                                // Nuevos endpoints de pedidos para roles específicos de empleado
                                .requestMatchers("/api/empleados/perfil").hasAuthority("ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/empleados/perfil").hasAuthority("ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/pedidos/{pedidoId}/estado-empleado/{sucursalId}").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/pedidos/{pedidoId}/tiempo-cocina/{sucursalId}").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/cajero/{sucursalId}").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/cocina/{sucursalId}").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/delivery/{sucursalId}").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.GET, "/api/empleados/usuario/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")

                                // ---- CUALQUIER USUARIO LOGUEADO PUEDE VER SU FACTURA ----
                                .requestMatchers(HttpMethod.GET,"/api/facturas/{id}").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN", "ROLE_EMPLEADO")

                                // ---- SOLO ADMIN ----
                                .requestMatchers("/api/empleados/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/usuarios/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/estadisticas/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/clientes/**").hasAuthority("ROLE_ADMIN")

                                // =================================================================================
                                // 4. CATCH-ALL - Cualquier otra petición no definida arriba, requiere autenticación
                                // =================================================================================
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt ->
                                jwt.decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String auth0Id = jwt.getSubject();
            String username = jwt.getClaimAsString("nickname");
            String email = jwt.getClaimAsString("email");

            if (username == null && email != null) {
                username = email.split("@")[0];
            }
            if (username == null || username.trim().isEmpty()) {
                username = "user_" + auth0Id.replaceAll("[^a-zA-Z0-9]", "").substring(0, Math.min(10, auth0Id.length()));
            }

            final String rolesClaim = "https://api.elbuensabor.com/roles";
            List<String> rolesFromToken = jwt.getClaimAsStringList(rolesClaim);
            System.out.println("JWT_CONVERTER: Roles leídos desde el token: " + rolesFromToken);

            Usuario usuario = null;
            try {
                usuario = usuarioService.findOrCreateUsuario(auth0Id, username, email, rolesFromToken);
            } catch (Exception e) {
                System.err.println("JWT_CONVERTER_ERROR: Excepción al buscar/crear usuario para auth0Id '" + auth0Id + "': " + e.getMessage());
                return Collections.emptyList();
            }

            Collection<GrantedAuthority> authorities = new HashSet<>();
            if (usuario != null && Boolean.TRUE.equals(usuario.getEstadoActivo())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
                System.out.println("JWT_CONVERTER: Usuario '" + (usuario.getUsername() != null ? usuario.getUsername() : auth0Id) + "' autenticado con rol final: " + usuario.getRol().name());
            } else if (usuario != null) {
                System.out.println("JWT_CONVERTER: Intento de login de usuario inactivo/dado de baja: " + (usuario.getUsername() != null ? usuario.getUsername() : auth0Id));
            } else {
                System.err.println("JWT_CONVERTER_ERROR: El servicio de usuario devolvió null para auth0Id: " + auth0Id);
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Auth-Token"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}