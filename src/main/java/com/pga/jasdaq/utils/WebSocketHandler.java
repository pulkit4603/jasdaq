package com.pga.jasdaq.utils;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.List;

public class WebSocketHandler extends TextWebSocketHandler {
  // Thread-safe list to store active WebSocket sessions
  private final List<WebSocketSession> sessions = new ArrayList<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // Add the session to the list
    System.out.println("sessions list 1: " + sessions);
    sessions.add(session);
    System.out.println("sessions list 2: " + sessions);
    // Send a welcome message with the current count
    session.sendMessage(new TextMessage("WELCOME"));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status)
      throws Exception {
    // Remove the session when it's closed
    sessions.remove(session);
  }

  public void sendToBroadcast(String stockSymbol, int price) {
    broadcastToClients(stockSymbol + " " + price);
  }

  private void broadcastToClients(String message) {
    System.out.println("Sessions: " + sessions);
    for (WebSocketSession session : sessions) {
      try {
        if (session.isOpen()) { // Check if the session is still open
          session.sendMessage(new TextMessage(message));
        }
      } catch (Exception e) {
        System.out.println("Exception in broadcastToClients");
        e.printStackTrace();
      }
    }
  }
}
