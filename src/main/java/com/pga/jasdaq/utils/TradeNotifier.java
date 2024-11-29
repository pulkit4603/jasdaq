package com.pga.jasdaq.utils;

import com.pga.jasdaq.orderbook.Trade;
import com.pga.jasdaq.config.TcpClientConfig.TcpNotificationGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeNotifier {

  // Map to track order IDs and their corresponding client IDs
  private final Map<Integer, String> orderToClientMap = new HashMap<>();
  private final TcpNotificationGateway notificationGateway;

  public TradeNotifier(TcpNotificationGateway notificationGateway) {
    this.notificationGateway = notificationGateway;
  }

  /**
   * Registers a new order with its client ID.
   *
   * @param orderId  The ID of the order.
   * @param clientId The ID of the client associated with the order.
   */
  public synchronized void registerOrder(int orderId, String clientId) {
    orderToClientMap.put(orderId, clientId);
  }

  /**
   * Removes an order from the mapping.
   *
   * @param orderId The ID of the order to remove.
   */
  public synchronized void unregisterOrder(int orderId) {
    orderToClientMap.remove(orderId);
  }

  /**
   * Notifies clients about the executed trades.
   *
   * @param trades The list of trades to notify.
   */
  public void notifyTrades(List<Trade> trades) {
    for (Trade trade : trades) {
      String buyerClientId = orderToClientMap.get(trade.getBuyOrderId());
      String sellerClientId = orderToClientMap.get(trade.getSellOrderId());

      // Create trade notification messages
      String buyerMessage = createTradeMessage(trade, "BUYER");
      String sellerMessage = createTradeMessage(trade, "SELLER");

      // Notify the respective clients using the TCP gateway
      if (buyerClientId != null) {
        notifyClient(buyerClientId, buyerMessage);
      }

      if (sellerClientId != null) {
        notifyClient(sellerClientId, sellerMessage);
      }
    }
  }

  /**
   * Constructs a trade notification message.
   *
   * @param trade The trade object.
   * @param role  The role of the client (e.g., "BUYER" or "SELLER").
   * @return The constructed notification message.
   */
  private String createTradeMessage(Trade trade, String role) {
    return String.format(
        "Trade Notification - Role: %s, Shares: %d, Price: %d, Counterparty Order ID: %d",
        role,
        trade.getSharesTraded(),
        trade.getTradePrice(),
        role.equals("BUYER") ? trade.getSellOrderId() : trade.getBuyOrderId());
  }

  /**
   * Sends a notification to the specified client using the TCP Notification
   * Gateway.
   *
   * @param clientId The client ID.
   * @param message  The message to send.
   */
  private void notifyClient(String clientId, String message) {
    notificationGateway.notifyServer(message, clientId, 8080); // Assuming port 8080 for now
  }
}
