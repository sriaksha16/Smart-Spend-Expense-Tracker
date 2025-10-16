package com.example.smartexpense.configurations;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

@Component
public class WebSocketEventLogger {

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal p = sha.getUser();
        System.out.println("🌐 WS CONNECT: principal=" + (p==null?"null":p.getName()));
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal p = sha.getUser();
        System.out.println("📩 WS SUBSCRIBE: principal=" + (p==null?"null":p.getName()) + 
                           " dest=" + sha.getDestination());
    }
}
