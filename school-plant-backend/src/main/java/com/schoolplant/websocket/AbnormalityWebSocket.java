package com.schoolplant.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/ws/abnormality/{userId}")
@Component
public class AbnormalityWebSocket {

    private static final ConcurrentHashMap<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        SESSIONS.put(userId, session);
        log.info("WebSocket Connected: User {}", userId);
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        SESSIONS.remove(userId);
        log.info("WebSocket Disconnected: User {}", userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket Error", error);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Handle client messages if needed
    }

    public static void sendMessage(Long userId, String message) {
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("Failed to send WebSocket message to User {}", userId, e);
            }
        }
    }
}
