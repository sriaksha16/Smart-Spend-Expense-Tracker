package com.example.smartexpense.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;



@Configuration 
@EnableWebSocketMessageBroker

/*
 * @EnableWebSocketMessageBroker → turns on WebSocket + STOMP messaging support
 * in Spring use @MessageMapping, brokers (/topic, /queue), and send/receive
 * messages between client and server. ✅
 */

public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
    		config.enableSimpleBroker("/topic", "/queue"); // for messages
   
   		//1.enableSimpleBroker- internal simple broker -is for app testing only for 50 users
   		//2.(for external broker enableStompBrokerRelay)for realtime more 50,00000 handling Messages can be persisted.
   
   
   		   //Simple broker = good for small apps, quick start, no setup.
		   //External broker (RabbitMQ, ActiveMQ, etc.) = needed for production-grade, large scale, persistent messaging.
   
			/*
			 * Simple broker = good for small apps, quick start, no setup.
			 * 
			 * External broker (RabbitMQ, ActiveMQ, etc.) = needed for production-grade,
			 * large scale, persistent messaging.
			 */
   		
   
		/* STOMP - simple text oriented messaging protocol - like http protocol
		 * topic → for publish/subscribe (broadcast) messages (all subscribers get it).
		 * queue → for point-to-point (1-to-1) messages. Example:
		 * 
		 * If client subscribes to /topic/news, RabbitMQ delivers broadcast messages to
		 * all subscribers.
		 * 
		 * If client subscribes to /queue/chat, only one consumer gets each message.
		 */
       
        config.setApplicationDestinationPrefixes("/app"); // for sending from client
		/*
		 * setApplicationDestinationPrefixes("/app") → messages sent from the client
		 * starting with /app go to your Spring controller methods (@MessageMapping).
		 
		 * @MessageMapping("/chat.sendMessage")
					@SendTo("/topic/public")
					public ChatMessage sendMessage(ChatMessage message) {
					    return message;
					}
				If a client sends a message like this (from JS STOMP client)?:
				stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({text: "Hello"}));
				
				/app is the entry prefix for client → server communication.
				Without it, Spring wouldn’t know which messages should go to your controllers.


		 */
        
       config.setUserDestinationPrefix("/user"); // user-specific3
        
		/*
		 * This is for user-specific (private) messaging.
		 * 
		 * It allows sending messages to a single, authenticated user instead of
		 * broadcasting to everyone.
		 * 
		 * backend: @Autowired private SimpMessagingTemplate messagingTemplate;
		 * 
		 * public void sendPrivateMessage(String username, String msg) {
		 * messagingTemplate.convertAndSendToUser(username, "/queue/messages", msg); }
		 * 
		 * Frontend: stompClient.subscribe("/user/queue/messages", function(message) {
		 * console.log("Private message: " + message.body); });
		 */
        
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS(); // endpoint for frontend
    }
    

}
