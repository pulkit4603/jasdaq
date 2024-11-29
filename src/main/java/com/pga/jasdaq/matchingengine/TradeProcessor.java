package com.pga.jasdaq.matchingengine;

import com.pga.jasdaq.config.TcpClientConfig.TcpNotificationGateway;
import com.pga.jasdaq.orderbook.Trade;
import com.pga.jasdaq.utils.TradeNotifier;

import java.util.List;

public class TradeProcessor {

  private final TradeNotifier tradeNotifier;

  public TradeProcessor(TcpNotificationGateway notificationGateway) {
    this.tradeNotifier = new TradeNotifier(notificationGateway);
  }

  /**
   * Updates the order-to-client mapping in the TradeNotifier.
   *
   * @param orderId  The ID of the order.
   * @param clientId The client associated with the order.
   */
  public void registerOrder(int orderId, String clientId) {
    tradeNotifier.registerOrder(orderId, clientId);
  }

  /**
   * Removes the mapping of an order from the TradeNotifier.
   *
   * @param orderId The ID of the order to remove.
   */
  public void unregisterOrder(int orderId) {
    tradeNotifier.unregisterOrder(orderId);
  }

  /**
   * Notifies clients about executed trades.
   *
   * @param trades The list of trades to notify.
   */
  public void processTrades(List<Trade> trades) {
    tradeNotifier.notifyTrades(trades);
  }
}

// DEAD BUT USEFUL CODE
// import com.pga.jasdaq.config.*;
// import com.pga.jasdaq.orderbook.*;
//
// import java.util.HashMap;
// import java.util.Map;
//
// import
// org.springframework.context.annotation.AnnotationConfigApplicationContext;
//
// public class TradeProcessor {
// Map<Integer, String> orderClientMap = new HashMap<Integer, String>();
//
// public TradeProcessor(Trade trade) {
//
// }
//
// public void notifyTrade(String[] args) {
// try (AnnotationConfigApplicationContext context = new
// AnnotationConfigApplicationContext(
// TcpClientConfig.class)) {
// TcpClientConfig.TcpNotificationGateway gateway = context
// .getBean(TcpClientConfig.TcpNotificationGateway.class);
//
// // Example: Notify different servers
// gateway.notifyServer("Order #1234 has been dispatched.", "192.168.1.10",
// 9090);
// gateway.notifyServer("Order #5678 has been dispatched.", "example.com",
// 8080);
// }
// }
// }
