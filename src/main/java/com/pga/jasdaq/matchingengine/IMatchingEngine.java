package com.pga.jasdaq.matchingengine;

import com.pga.jasdaq.orderbook.*;
import java.util.List;

public interface IMatchingEngine {

  /**
   * Places a limit order.
   *
   * @param order The limit order to place.
   * @return List of trades executed as a result.
   */
  List<Trade> placeLimitOrder(Order order);

  /**
   * Places a market order.
   *
   * @param order The market order to place.
   * @return List of trades executed as a result.
   */
  List<Trade> placeMarketOrder(Order order);

  /**
   * Cancels an order.
   *
   * @param orderId The ID of the order to cancel.
   */
  void cancelOrder(int orderId);

  /**
   * Retrieves the current best bid price.
   *
   * @return The best bid price.
   */
  int getBestBid();

  /**
   * Retrieves the current best offer price.
   *
   * @return The best offer price.
   */
  int getBestOffer();

  /**
   * Retrieves the spread (difference between best bid and best offer).
   *
   * @return The spread.
   */
  int getSpread();

  /**
   * Retrieves the last traded price.
   *
   * @return The last traded price.
   */
  int getLastTradedPrice();

  /**
   * Retrieves a snapshot of the order book.
   *
   * @return String representation of the order book.
   */
  String getOrderBookSnapshot();
}
