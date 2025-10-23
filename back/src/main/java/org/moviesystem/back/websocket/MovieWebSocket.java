package org.moviesystem.back.websocket;

import jakarta.enterprise.event.Observes;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.moviesystem.back.model.Movie;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/websocket/movies")
public class MovieWebSocket {
    
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    
    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("WebSocket connection opened: " + session.getId());
    }
    
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
        sessions.remove(session);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        // Эхо сообщения обратно клиенту
        try {
            session.getBasicRemote().sendText("Echo: " + message);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    public void onMovieChange(@Observes Movie movie) {
        broadcastUpdate();
    }
    
    private void broadcastUpdate() {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText("{\"type\":\"update\",\"message\":\"Movies updated\"}");
                    } catch (IOException e) {
                        System.err.println("Error broadcasting update: " + e.getMessage());
                        sessions.remove(session);
                    }
                }
            }
        }
    }
    
    public static void broadcastToAll(String message) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        System.err.println("Error broadcasting message: " + e.getMessage());
                        sessions.remove(session);
                    }
                }
            }
        }
    }
}
