package com.pga.jasdaq.engine;

import com.pga.jasdaq.matchingengine.IMatchingEngine;
import com.pga.jasdaq.matchingengine.MatchingEngine;
import com.pga.jasdaq.orderbook.IBook;
import com.pga.jasdaq.orderbook.Order;
import com.pga.jasdaq.orderbook.Trade;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;

@Service
public class StockMarketEngine implements IStockMarketEngine {

  private final Map<String, IMatchingEngine> matchingEngines;
  private final IBook orderBook; // Assuming you have a way to create an IBook instance

  public StockMarketEngine(Map<String, IMatchingEngine> matchingEngines, IBook orderBook) {
    this.matchingEngines = matchingEngines;
    this.orderBook = orderBook;

    // Initialize the map with empty engines for the specified stock symbols
    initializeMatchingEngines();
  }

  private void initializeMatchingEngines() {
    matchingEngines.put("TSLA", new MatchingEngine(orderBook));
    matchingEngines.put("HIND", new MatchingEngine(orderBook));
    matchingEngines.put("RELI", new MatchingEngine(orderBook));
    matchingEngines.put("ADNI", new MatchingEngine(orderBook));
  }

  @Override
  public List<Trade> placeOrder(Order order, String stockSymbol, String clientId) {
    IMatchingEngine matchingEngine = matchingEngines.get(stockSymbol);
    if (matchingEngine == null) {
      throw new IllegalArgumentException("No matching engine found for stock: " + stockSymbol);
    }

    List<Trade> tradesExecuted;

    // Determine the type of order and call the appropriate matching engine method
    if (order.isMarketOrder()) {
      tradesExecuted = matchingEngine.placeMarketOrder(order);
    } else {
      tradesExecuted = matchingEngine.placeLimitOrder(order);
    }

    // Log trades executed or notify another component as necessary
    System.out.println("Trades executed for client " + clientId + " for stock " + stockSymbol + ": " + tradesExecuted);

    return tradesExecuted;
  }

  @Override
  public void cancelOrder(int orderId, String stockSymbol) {
    IMatchingEngine matchingEngine = matchingEngines.get(stockSymbol);
    if (matchingEngine == null) {
      throw new IllegalArgumentException("No matching engine found for stock: " + stockSymbol);
    }

    matchingEngine.cancelOrder(orderId);
    System.out.println("Order " + orderId + " canceled for stock " + stockSymbol + ".");
  }

  @Override
  public String getOrderBookSnapshot(String stockSymbol) {
    IMatchingEngine matchingEngine = matchingEngines.get(stockSymbol);
    if (matchingEngine == null) {
      throw new IllegalArgumentException("No matching engine found for stock: " + stockSymbol);
    }

    return matchingEngine.getOrderBookSnapshot();
  }
}