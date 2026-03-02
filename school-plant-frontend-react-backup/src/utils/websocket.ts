import { message, notification } from 'antd';

let socket: WebSocket | null = null;

export const connectWebSocket = (userId: number) => {
  if (socket) {
    socket.close();
  }

  // Assume backend WS endpoint is ws://localhost:8080/ws/abnormality/{userId}
  // If we use proxy, we might need to adjust or use full URL
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  // Use backend port 8080 directly for WS usually, or proxy via vite
  // Vite proxy for WS needs ws: true
  // Let's assume localhost:8080 for development
  const wsUrl = `ws://localhost:8080/ws/abnormality/${userId}`;

  socket = new WebSocket(wsUrl);

  socket.onopen = () => {
    console.log('WebSocket Connected');
  };

  socket.onmessage = (event) => {
    console.log('WS Message:', event.data);
    try {
        // Simple text message or JSON? Backend sends plain text in AbnormalityWebSocket.java
        // "您有新的异常工单待处理，ID: ..."
        notification.info({
            message: '系统通知',
            description: event.data,
            duration: 5,
        });
    } catch (e) {
        console.error(e);
    }
  };

  socket.onclose = () => {
    console.log('WebSocket Disconnected');
  };

  socket.onerror = (error) => {
    console.error('WebSocket Error', error);
  };
};

export const disconnectWebSocket = () => {
  if (socket) {
    socket.close();
    socket = null;
  }
};
