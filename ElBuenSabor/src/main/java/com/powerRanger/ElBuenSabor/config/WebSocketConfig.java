package com.powerRanger.ElBuenSabor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un simple broker de mensajes en memoria.
        // Los mensajes cuyo destino comienza con "/topic" se enrutarán a este broker.
        config.enableSimpleBroker("/topic");

        // Configura un prefijo para los destinos a los que los clientes enviarán mensajes.
        // Los mensajes desde el cliente con destinos que comienzan con "/app" serán enrutados
        // a los métodos anotados con @MessageMapping en el backend.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra el endpoint "/ws" para la conexión WebSocket.
        // Con SockJS se habilita un fallback para navegadores que no soportan WebSockets nativos.
        // Permite orígenes específicos para CORS, lo cual es crucial en entornos de desarrollo.
        registry.addEndpoint("/api" +
                        "/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:8080", "http://localhost:4200") // Permite orígenes específicos para CORS
                .withSockJS();
    }
}