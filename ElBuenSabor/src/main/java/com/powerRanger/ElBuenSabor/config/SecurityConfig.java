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
import java.util.HashSet;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize, @PostAuthorize en métodos
@Profile("!dev") // No se activará si el perfil "dev" está activo
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
                                // ENDPOINTS PÚBLICOS
                                .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                                // LÍNEA AÑADIDA PARA WEBSOCKETS
                                .requestMatchers("/ws/**").permitAll()

                                // Permitir a cualquier usuario AUTENTICADO intentar acceder a su propio detalle vía auth0Id.
                                .requestMatchers(HttpMethod.GET, "/api/usuarios/auth0/{auth0Id}").authenticated()

                                // Permitir GET para ciertas entidades de catálogo sin autenticación
                                .requestMatchers(HttpMethod.GET, "/api/categorias/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/unidadesmedida/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/articulos/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/articulosinsumo/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/articulosmanufacturados/**").permitAll()

                                // REGLAS DE AUTORIZACIÓN ESPECÍFICAS
                                .requestMatchers(HttpMethod.POST, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**", "/api/unidadesmedida/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")

                                .requestMatchers(HttpMethod.POST, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.DELETE, "/api/articulos/**", "/api/articulosinsumo/**", "/api/articulosmanufacturados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")

                                // Gestión de usuarios solo por ADMIN
                                .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/usuarios").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/usuarios/{id}").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/{id}").hasAuthority("ROLE_ADMIN")

                                // Pedidos
                                .requestMatchers(HttpMethod.POST, "/api/pedidos").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST, "/api/pedidos/cliente/{clienteId}/desde-carrito").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/mis-pedidos").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/cliente/{clienteId}").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/pedidos/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/pedidos/estado/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT, "/api/pedidos/{id}/estado").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLEADO")

                                // Carrito
                                .requestMatchers(HttpMethod.GET, "/api/clientes/{clienteId}/carrito").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST, "/api/clientes/{clienteId}/carrito/items").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.PUT, "/api/clientes/{clienteId}/carrito/items/{carritoItemId}").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.DELETE, "/api/clientes/{clienteId}/carrito/items/{carritoItemId}").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.DELETE, "/api/clientes/{clienteId}/carrito/items").hasAuthority("ROLE_CLIENTE")

                                // Clientes
                                .requestMatchers(HttpMethod.GET, "/api/clientes/perfil").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.PUT, "/api/clientes/perfil").hasAuthority("ROLE_CLIENTE")
                                .requestMatchers(HttpMethod.POST,"/api/domicilios/").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                                .requestMatchers(HttpMethod.PUT,"/api/domicilios/{id}").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                                .requestMatchers(HttpMethod.DELETE,"/api/domicilios/{id}").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN")
                                .requestMatchers("/api/clientes/**").hasAuthority("ROLE_ADMIN")

                                // Facturas
                                .requestMatchers(HttpMethod.GET,"/api/facturas/{id}").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.PUT,"/api/facturas/anular/{id}").hasAnyAuthority("ROLE_ADMIN","ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.POST,"/api/facturas/generar-desde-pedido").hasAnyAuthority("ROLE_ADMIN","ROLE_EMPLEADO")

                                // Imagenes
                                .requestMatchers(HttpMethod.POST, "/api/files/upload").hasAnyAuthority( "ROLE_ADMIN", "ROLE_EMPLEADO")
                                .requestMatchers(HttpMethod.GET, "/api/files/view/{filename}").permitAll()

                                // Estadisticas
                                .requestMatchers(HttpMethod.GET, "/api/estadisticas/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/estadisticas/**").hasAuthority("ROLE_ADMIN")

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

            Collection<GrantedAuthority> authorities = new HashSet<>();
            try {
                System.out.println("JWT_CONVERTER: Llamando a findOrCreateUsuario con auth0Id: " + auth0Id + ", username: " + username + ", email: " + email);
                Usuario usuario = usuarioService.findOrCreateUsuario(auth0Id, username, email);

                if (usuario != null && usuario.getRol() != null && Boolean.TRUE.equals(usuario.getEstadoActivo())) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
                    System.out.println("JWT_CONVERTER: Usuario '" + (usuario.getUsername() != null ? usuario.getUsername() : auth0Id) + "' autenticado con rol: " + usuario.getRol().name());
                } else if (usuario != null && !Boolean.TRUE.equals(usuario.getEstadoActivo())) {
                    System.out.println("JWT_CONVERTER: Intento de login de usuario inactivo/dado de baja: " + (usuario.getUsername() != null ? usuario.getUsername() : auth0Id));
                } else {
                    System.out.println("JWT_CONVERTER: No se pudo encontrar/crear usuario, o no tiene rol/estado activo para auth0Id: " + auth0Id + ". Usuario devuelto por servicio: " + usuario);
                }
            } catch (Exception e) {
                System.err.println("JWT_CONVERTER_ERROR: Error en findOrCreateUsuario para auth0Id '" + auth0Id + "': " + e.getMessage());
                e.printStackTrace();
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