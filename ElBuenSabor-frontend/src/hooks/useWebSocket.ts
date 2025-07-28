import { useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import apiClient from '../services/apiClient';
import toast from 'react-hot-toast';
import type { PedidoResponse } from '../types/types'; 

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

                if (pedido.estado === 'PENDIENTE' && !processedMessageIds.current.has(pedido.id)) {
                    processedMessageIds.current.add(pedido.id);
                    toast.success(`¡Nuevo pedido! #${pedido.id} de ${pedido.cliente.nombre}`, {
                        icon: '🔔',
                        duration: 5000,
                        position: 'top-right',
                    });
                }
                
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