package com.pga.jasdaq.orderbook;

import java.util.List;

public interface IBook {

  /**
   * removes an order in the book.
   *
   * @param orderId The ID of the order to cancel.
   */
  void removeOrder(int orderId);

  /**
   * Places an incoming limit order after matching against existing orders in the
   * book.
   *
   * @param incomingOrder The incoming order to match.
   */
  List<Trade> placeLimitOrder(Order incomingOrder); // returns trades list

  /**
   * Places a market order and matches it against the book.
   *
   * @param marketOrder The market order to place.
   */
  List<Trade> placeMarketOrder(Order marketOrder); // returns trades list

  /**
   * Retrieves the current best bid price in the book.
   *
   * @return The best bid price, or -1 if no bids are available.
   */
  int getBestBid();

  /**
   * Retrieves the current best offer price in the book.
   *
   * @return The best offer price, or -1 if no offers are available.
   */
  int getBestOffer();

  /**
   * Computes the spread (difference between best offer and best bid).
   *
   * @return The spread, or -1 if the book is empty or incomplete.
   */
  int getSpread();

  /**
   * Validates an order for correctness.
   *
   * @param order The order to validate.
   * @throws IllegalArgumentException if the order is invalid.
   */
  void validateOrder(Order order);

  /**
   * Generates a snapshot of the current state of the order book.
   *
   * @return A string representation of the order book.
   */
  String getOrderBookSnapshot();
}