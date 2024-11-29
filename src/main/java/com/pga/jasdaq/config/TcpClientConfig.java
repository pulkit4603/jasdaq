package com.pga.jasdaq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class TcpClientConfig {

  // Gateway for sending data
  @MessagingGateway
  public interface TcpNotificationGateway {
    void notifyServer(String message, String host, int port);
  }

  // Channel for sending messages
  @Bean
  public MessageChannel toTcpChannel() {
    return new DirectChannel();
  }

  // Message handler that dynamically sets the connection factory
  @Bean
  @ServiceActivator(inputChannel = "toTcpChannel")
  public MessageHandler tcpMessageHandler() {
    return message -> {
      // Extract host and port from message headers
      String host = (String) message.getHeaders().get("host");
      int port = (int) message.getHeaders().get("port");

      // Create a connection factory dynamically
      TcpNetClientConnectionFactory connectionFactory = new TcpNetClientConnectionFactory(host, port);
      connectionFactory.start();

      // Send the message
      TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
      handler.setConnectionFactory(connectionFactory);
      handler.handleMessage(message);
    };
  }
}
