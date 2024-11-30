package com.pga.jasdaq.engine;

import com.pga.jasdaq.orderbook.Trade;
import com.pga.jasdaq.orderbook.Order;

import java.util.List;

public interface IStockMarketEngine {

  /**
   * Places an order in the stock market.
   *
   * @param order       The order to place (limit or market).
   * @param stockSymbol The symbol of the stock for which the order is placed.
   * @param clientId    The ID of the client placing the order.
   * @return A list of executed trades as a result of the order placement.
   */
  List<Trade> placeOrder(Order order, String stockSymbol, String clientId);

  /**
   * Cancels an existing order.
   *
   * @param orderId     The ID of the order to cancel.
   * @param stockSymbol The symbol of the stock for which the order is canceled.
   */
  void cancelOrder(int orderId, String stockSymbol);

  /**
   * Gets a snapshot of the current state of the order book.
   *
   * @param stockSymbol The symbol of the stock for which the order book snapshot
   *                    is requested.
   * @return A string representation of the order book.
   */
  String getOrderBookSnapshot(String stockSymbol);

  /**
   * Retrieves the current price of the last executed trade for a given stock symbol.
   *
   * @param stockSymbol The symbol of the stock.
   * @return The current price, or -1 if no trades have been executed.
   */
  int getCurrentPrice(String stockSymbol);
}
