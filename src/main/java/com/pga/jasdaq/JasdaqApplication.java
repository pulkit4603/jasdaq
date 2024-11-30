package com.pga.jasdaq;

import com.pga.jasdaq.utils.WebSocketHandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JasdaqApplication {

  public static void main(String[] args) {
    SpringApplication.run(JasdaqApplication.class, args);
  }

  @Bean
  public WebSocketHandler webSocketHandler() {
      return new WebSocketHandler();
  }
}
