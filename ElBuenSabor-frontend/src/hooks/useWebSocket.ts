import { useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import apiClient from '../services/apiClient';

// El callback recibirá el cuerpo del mensaje, que esperamos sea un objeto (ej. PedidoResponse)
type MessageCallback = (message: any) => void;

export const useWebSocket = (topic: string, onMessageReceived: MessageCallback) => {
    // Usamos useRef para mantener una única instancia del cliente Stomp durante el ciclo de vida del componente
    const stompClientRef = useRef<Stomp.Client | null>(null);

    useEffect(() => {
        // Solo intentamos conectar si el topic es válido
        if (!topic) return;

        // URL del endpoint de WebSocket de tu backend
        const socket = new SockJS(`${apiClient.defaults.baseURL}/ws`);
        const stompClient = Stomp.over(socket);
        stompClientRef.current = stompClient;
        
        // Desactivamos los logs de debug de Stomp en la consola para no saturarla
        stompClient.debug = () => {};

        stompClient.connect({}, () => {
            console.log(`Conectado al WebSocket y suscrito al tema: ${topic}`);
            
            // Suscripción al tema específico
            stompClient.subscribe(topic, (message) => {
                // Cuando llega un mensaje, parseamos su contenido y llamamos al callback
                const messageBody = JSON.parse(message.body);
                onMessageReceived(messageBody);
            });
        }, (error) => {
            console.error('Error de conexión con WebSocket:', error);
        });

        // Función de limpieza que se ejecuta cuando el componente se "desmonta"
        return () => {
            if (stompClient.connected) {
                console.log(`Desconectando del tema: ${topic}`);
                stompClient.disconnect(() => {
                    console.log('Desconectado del WebSocket.');
                });
            }
        };
    }, [topic, onMessageReceived]); // El efecto se vuelve a ejecutar si el tema o el callback cambian
};