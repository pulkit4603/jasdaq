package com.pga.jasdaq.engine;

import com.pga.jasdaq.db.service.TradeService;
import com.pga.jasdaq.matchingengine.IMatchingEngine;
import com.pga.jasdaq.matchingengine.MatchingEngine;
import com.pga.jasdaq.orderbook.IBook;
import com.pga.jasdaq.orderbook.Order;
import com.pga.jasdaq.orderbook.Trade;
import com.pga.jasdaq.utils.WebSocketHandler;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StockMarketEngine implements IStockMarketEngine {

  private static final Logger logger = LoggerFactory.getLogger(StockMarketEngine.class);
  
  private final Map<String, IMatchingEngine> matchingEngines;
  private final IBook orderBook;
  private final WebSocketHandler webSocketHandler;
  private final TradeService tradeService;

  public StockMarketEngine(Map<String, IMatchingEngine> matchingEngines, IBook orderBook,
      WebSocketHandler webSocketHandler, TradeService tradeService) {
    this.matchingEngines = matchingEngines;
    this.orderBook = orderBook;
    this.webSocketHandler = webSocketHandler;
    this.tradeService = tradeService;

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
    logger.info("In placeOrder in matchingEngine, stockSymbol: {}", stockSymbol);
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
    logger.info("Trades executed for client {} for stock {}: {}", clientId, stockSymbol, tradesExecuted);

    // Store trades in the database
    for (Trade trade : tradesExecuted) {
      // Save trade to database (isBuy reflects whether the original order was a buy)
      try {
        tradeService.saveTrade(trade, stockSymbol, order.isBuy());
      } catch (Exception e) {
        logger.error("Failed to save trade to database: {}", e.getMessage(), e);
      }
      
      // Broadcast each trade to WebSocket clients
      webSocketHandler.sendToBroadcast(stockSymbol, trade.getTradePrice());
    }

    return tradesExecuted;
  }

  @Override
  public void cancelOrder(int orderId, String stockSymbol) {
    IMatchingEngine matchingEngine = matchingEngines.get(stockSymbol);
    if (matchingEngine == null) {
      throw new IllegalArgumentException("No matching engine found for stock: " + stockSymbol);
    }

    matchingEngine.cancelOrder(orderId);
    logger.info("Order {} canceled for stock {}.", orderId, stockSymbol);
  }

  @Override
  public String getOrderBookSnapshot(String stockSymbol) {
    IMatchingEngine matchingEngine = matchingEngines.get(stockSymbol);
    if (matchingEngine == null) {
      throw new IllegalArgumentException("No matching engine found for stock: " + stockSymbol);
    }

    return matchingEngine.getOrderBookSnapshot();
  }

  @Override
  public int getCurrentPrice(String stockSymbol) {
    IMatchingEngine matchingEngine = matchingEngines.get(stockSymbol);
    if (matchingEngine == null) {
      throw new IllegalArgumentException("No matching engine found for stock: " + stockSymbol);
    }

    return matchingEngine.getCurrentPrice();
  }
}
