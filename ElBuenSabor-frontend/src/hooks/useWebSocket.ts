import { useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import apiClient from '../services/apiClient';
import toast from 'react-hot-toast';
import type { PedidoResponse } from '../types/types'; 

// El callback recibirá el cuerpo del mensaje, que esperamos sea un objeto (ej. PedidoResponse)
type MessageCallback = (message: any) => void;

export const useWebSocket = (topic: string, onMessageReceived: MessageCallback) => {
    const stompClientRef = useRef<Stomp.Client | null>(null);
    const processedMessageIds = useRef(new Set());

    useEffect(() => {
        if (!topic) return;

        const socket = new SockJS(`${apiClient.defaults.baseURL}/ws`);
        const stompClient = Stomp.over(socket);
        stompClientRef.current = stompClient;
        
        stompClient.debug = () => {};

        stompClient.connect({}, () => {
            console.log(`Conectado al WebSocket y suscrito al tema: ${topic}`);
            
            stompClient.subscribe(topic, (message) => {
                const pedido: PedidoResponse = JSON.parse(message.body);

                // --- LÓGICA DE NOTIFICACIÓN REFINADA ---
                // Solo notificar si es un pedido nuevo (PENDIENTE) y no ha sido notificado antes.
                if (pedido.estado === 'PENDIENTE' && !processedMessageIds.current.has(pedido.id)) {
                    processedMessageIds.current.add(pedido.id);
                    toast.success(`¡Nuevo pedido! #${pedido.id} de ${pedido.cliente.nombre}`, {
                        icon: '🔔',
                        duration: 5000,
                        position: 'top-right',
                    });
                }
                
                // Siempre llamar al callback para que el componente decida si actualizar la tabla.
                onMessageReceived(pedido);
            });
        }, (error) => {
            console.error('Error de conexión con WebSocket:', error);
        });

        return () => {
            processedMessageIds.current.clear();
            if (stompClient.connected) {
                stompClient.disconnect(() => {
                    console.log('Desconectado del WebSocket.');
                });
            }
        };
    }, [topic, onMessageReceived]);
};