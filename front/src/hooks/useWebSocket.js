import { useEffect, useRef, useState } from 'react';

const useWebSocket = (url) => {
  const [socket, setSocket] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [lastMessage, setLastMessage] = useState(null);
  const [connectionError, setConnectionError] = useState(null);
  const reconnectTimeoutRef = useRef(null);
  const reconnectAttempts = useRef(0);
  const maxReconnectAttempts = 5;

  useEffect(() => {
    const connect = () => {
      try {
        setConnectionError(null);
        const ws = new WebSocket(url);
        
        ws.onopen = () => {
          console.log('WebSocket connected to:', url);
          setIsConnected(true);
          setSocket(ws);
          reconnectAttempts.current = 0;
          setConnectionError(null);
        };

        ws.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            setLastMessage(data);
          } catch (error) {
            console.error('Error parsing WebSocket message:', error);
          }
        };

        ws.onclose = (event) => {
          console.log('WebSocket disconnected:', event.code, event.reason);
          setIsConnected(false);
          setSocket(null);
          
          // Set connection error for user feedback
          if (event.code === 1006) {
            setConnectionError('Не удается подключиться к серверу. Убедитесь, что backend запущен на порту 8080.');
          }
          
          // Attempt to reconnect if not a manual close
          if (event.code !== 1000 && reconnectAttempts.current < maxReconnectAttempts) {
            const delay = Math.pow(2, reconnectAttempts.current) * 1000; // Exponential backoff
            console.log(`Attempting to reconnect in ${delay}ms (attempt ${reconnectAttempts.current + 1})`);
            
            reconnectTimeoutRef.current = setTimeout(() => {
              reconnectAttempts.current++;
              connect();
            }, delay);
          } else if (reconnectAttempts.current >= maxReconnectAttempts) {
            setConnectionError('Превышено максимальное количество попыток переподключения. Проверьте, что backend сервер запущен.');
          }
        };

        ws.onerror = (error) => {
          console.error('WebSocket error:', error);
          setConnectionError('Ошибка WebSocket соединения. Проверьте, что backend сервер запущен на порту 8080.');
        };

      } catch (error) {
        console.error('Error creating WebSocket:', error);
        setIsConnected(false);
        setConnectionError('Ошибка создания WebSocket соединения.');
      }
    };

    connect();

    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (socket) {
        socket.close(1000, 'Component unmounting');
      }
    };
  }, [url]);

  const sendMessage = (message) => {
    if (socket && isConnected) {
      socket.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket is not connected');
    }
  };

  return {
    socket,
    isConnected,
    lastMessage,
    connectionError,
    sendMessage
  };
};

export default useWebSocket;
