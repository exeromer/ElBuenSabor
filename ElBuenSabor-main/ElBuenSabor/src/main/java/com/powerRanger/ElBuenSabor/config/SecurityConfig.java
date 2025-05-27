package com.powerRanger.ElBuenSabor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Para csrf().disable()
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity // Habilita la configuración de seguridad web de Spring
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuración de CORS para permitir peticiones del frontend
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Deshabilitar CSRF (Cross-Site Request Forgery)
                // Es común deshabilitarlo para APIs REST puras o al probar con Postman.
                .csrf(AbstractHttpConfigurer::disable)

                // Configurar las reglas de autorización de las peticiones HTTP
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest().permitAll() // Permite TODAS las peticiones a cualquier URL sin autenticación
                );

        return http.build();
    }

    /**
     * Configuración de CORS. Permite peticiones desde el frontend.
     * Aunque estemos permitiendo todo, es bueno mantener la configuración de CORS
     * por si la necesitas más adelante o si tu navegador hace comprobaciones.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Orígenes permitidos (ajusta según tu frontend en desarrollo/producción)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://localhost:4200", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Auth-Token"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esta configuración a todas las rutas
        return source;
    }
}

/*
package com.powerRanger.ElBuenSabor.config;

import com.powerRanger.ElBuenSabor.entities.Usuario;
import com.powerRanger.ElBuenSabor.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
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
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize, @PostAuthorize en métodos
public class SecurityConfig {

    @Autowired
    private UsuarioService usuarioService;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    // La propiedad 'auth0.audience' no es usada directamente por Spring Security por defecto para validación
    // pero la incluimos aquí si quisiéramos hacer una validación de audiencia manual en el JwtDecoder.
    // Spring Boot 3.x a menudo puede inferir la audiencia si se configura correctamente en Auth0
    // y si el token la incluye. Si no, necesitaríamos un validador de audiencia personalizado.
    // @Value("${auth0.audience}")
    // private String audience;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // ACA DEBEMOS IMPLENTAR QUIEN TIENE ACCESO A LAS APIS (ADMIN CLIENTE EMPLEADO ETC)
                                // ENDPOINTS PÚBLICOS (ejemplos, ajusta según tus necesidades)
                                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                                // Permitir GET para ciertas entidades de catálogo sin autenticación
                                .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/unidadesmedida/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/articulos/**").permitAll() // GET general de artículos
                                .requestMatchers(HttpMethod.GET, "/api/articulosinsumo/**").permitAll() // GET general de insumos
                                .requestMatchers(HttpMethod.GET, "/api/articulosmanufacturados/**").permitAll() // GET general de manufacturados

                                // REGLAS DE AUTORIZACIÓN ESPECÍFICAS (EJEMPLOS)
                                // Para crear, modificar o borrar se necesita un rol específico
                                .requestMatchers(HttpMethod.POST, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")

                                .requestMatchers(HttpMethod.POST, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.DELETE, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")

                                // Gestión de usuarios solo por ADMIN (el endpoint de creación directa)
                                .requestMatchers("/api/usuarios/**").hasAuthority("ROLE_ADMIN")

                                // Pedidos: Clientes pueden crear y ver sus pedidos, empleados/admin pueden ver todos/gestionar
                                .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/cliente/{clienteId}").hasAuthority("ROLE_CLIENTE") // Un cliente solo sus pedidos
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO") // Admin/Empleado ven todos
                                .requestMatchers(HttpMethod.PUT, "/api/pedidos/estado/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO") // Cambiar estado

                                // Clientes: Un cliente puede ver/modificar su propio perfil. Admin puede gestionar todos.
                                .requestMatchers(HttpMethod.GET, "/api/clientes/perfil").hasAuthority("ROLE_CLIENTE") // Endpoint para que el cliente obtenga su perfil
                                .requestMatchers(HttpMethod.PUT, "/api/clientes/perfil").hasAuthority("ROLE_CLIENTE") // Endpoint para que el cliente actualice su perfil
                                .requestMatchers("/api/clientes/**").hasAuthority("ROLE_ADMIN")


                                // Cualquier otra petición requiere autenticación
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
        // Configura el decodificador de JWT para validar tokens contra tu Auth0 issuer URI.
        // Esto verificará la firma y el issuer.
        // La validación de la audiencia (audience) es importante. Spring Boot 3+
        // intenta validar la audiencia del token contra lo que espera el resource server.
        // Asegúrate que el token de Auth0 contenga la 'audience' correcta (`auth0.audience` de tus properties).
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Este convertidor se ejecuta después de que el token JWT es decodificado y validado.
        // Aquí es donde extraemos el auth0Id (subject del token) y llamamos a nuestro UsuarioService
        // para buscar o crear el usuario local y obtener sus roles de NUESTRA base de datos.
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String auth0Id = jwt.getSubject(); // 'sub' claim es el auth0Id

            // Extraer información adicional del token si está disponible y la necesitas
            // El claim 'email' es estándar, 'nickname' puede o no estar. Ajusta según los claims de tu token de Auth0.
            String username = jwt.getClaimAsString("nickname");
            String email = jwt.getClaimAsString("email");

            if (username == null && email != null) { // Usar email como username si nickname no está
                username = email;
            }

            Collection<GrantedAuthority> authorities = new HashSet<>();
            try {
                // Lógica de Just-In-Time Provisioning: Busca o crea el usuario en nuestra BD
                Usuario usuario = usuarioService.findOrCreateUsuario(auth0Id, username, email);

                if (usuario != null && usuario.getRol() != null && Boolean.TRUE.equals(usuario.getEstadoActivo())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
                    System.out.println("Usuario '" + (usuario.getUsername() != null ? usuario.getUsername() : auth0Id) + "' autenticado con rol: " + usuario.getRol().name());
                } else if (usuario != null && !Boolean.TRUE.equals(usuario.getEstadoActivo())) {
                    System.out.println("Intento de login de usuario inactivo/dado de baja: " + (usuario.getUsername() != null ? usuario.getUsername() : auth0Id));
                    // No se asignan roles. La autorización fallará.
                } else {
                    System.out.println("No se pudo encontrar/crear usuario o no tiene rol para auth0Id: " + auth0Id);
                }
            } catch (Exception e) {
                System.err.println("Error en findOrCreateUsuario para auth0Id '" + auth0Id + "': " + e.getMessage());
                // Esto podría significar que el usuario no puede ser autenticado correctamente en tu sistema.
                // Se podría lanzar una AuthenticationServiceException aquí.
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Orígenes permitidos para tu frontend (ej. localhost:3000, localhost:5173 para desarrollo)
        // En producción, reemplaza esto con el dominio real de tu frontend.
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080")); //ACA VA LA URL DEL FRONTEND (OSEA LIVESERVER)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type", "X-Auth-Token"));
        configuration.setAllowCredentials(true); // Importante si tu frontend necesita enviar cookies o cabeceras de autorización
        configuration.setExposedHeaders(List.of("Authorization")); // Cabeceras que el cliente puede leer

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esta configuración a todas las rutas "/api/**"
        return source;
    }
}

*/