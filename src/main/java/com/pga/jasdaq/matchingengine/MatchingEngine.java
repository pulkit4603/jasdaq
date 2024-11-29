package com.pga.jasdaq.matchingengine;

import com.pga.jasdaq.orderbook.*;
import java.util.List;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MatchingEngine implements IMatchingEngine {

  private final IBook orderBook;
  private final List<Trade> tradeHistory; // Keeps a local history of trades
  private int lastTradedPrice = -1; // Stores the last traded price

  public MatchingEngine(IBook orderBook) {
    this.orderBook = orderBook;
    this.tradeHistory = new ArrayList<Trade>();
  }

  /**
   * Places a limit order and processes matching.
   *
   * @param order The limit order to place.
   * @return List of trades executed as a result of the order placement.
   */
  @Override
  public List<Trade> placeLimitOrder(Order order) {
    orderBook.validateOrder(order);
    List<Trade> trades = orderBook.placeLimitOrder(order);
    handleExecutedTrades(trades);
    return trades;
  }

  /**
   * Places a market order and processes matching.
   *
   * @param order The market order to place.
   * @return List of trades executed as a result of the market order placement.
   */
  @Override
  public List<Trade> placeMarketOrder(Order order) {
    orderBook.validateOrder(order);
    List<Trade> trades = orderBook.placeMarketOrder(order);
    handleExecutedTrades(trades);
    return trades;
  }

  /**
   * Cancels an order in the order book.
   *
   * @param orderId The ID of the order to cancel.
   */
  @Override
  public void cancelOrder(int orderId) {
    orderBook.removeOrder(orderId);
  }

  /**
   * Retrieves the best bid price.
   *
   * @return The best bid price.
   */
  @Override
  public int getBestBid() {
    return orderBook.getBestBid();
  }

  /**
   * Retrieves the best offer price.
   *
   * @return The best offer price.
   */
  @Override
  public int getBestOffer() {
    return orderBook.getBestOffer();
  }

  /**
   * Retrieves the spread (difference between best bid and best offer).
   *
   * @return The spread.
   */
  @Override
  public int getSpread() {
    return orderBook.getSpread();
  }

  /**
   * Returns the last traded price.
   *
   * @return The last traded price.
   */
  @Override
  public int getLastTradedPrice() {
    return lastTradedPrice;
  }

  /**
   * Handles the trades executed during an order placement.
   * Updates the trade history and the last traded price.
   *
   * @param trades List of trades executed.
   */
  private void handleExecutedTrades(List<Trade> trades) {
    for (Trade trade : trades) {
      tradeHistory.add(trade); // Append trade to local history
      lastTradedPrice = trade.getTradePrice(); // Update last traded price
      // notify related parties using UDP multicast
      System.out.println("Trade Executed: " + trade);
    }
  }

  /**
   * Retrieves a snapshot of the order book.
   *
   * @return A string representation of the order book.
   */
  @Override
  public String getOrderBookSnapshot() {
    return orderBook.getOrderBookSnapshot();
  }

  /**
   * Returns the trade history stored locally in the matching engine.
   *
   * @return List of trades executed so far.
   */
  public List<Trade> getTradeHistory() {
    return new ArrayList<>(tradeHistory); // Return a copy of the trade history
  }
}
