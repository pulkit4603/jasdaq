package com.pga.jasdaq.orderbook;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

@Service
class Book implements IBook {
  // Store all orders by ID for quick access
  Map<Integer, Order> orders = new HashMap<>();

  // Store buy and sell limits
  TreeMap<Integer, Limit> buyLimits = new TreeMap<>((a, b) -> b - a); // Descending order for buys
  TreeMap<Integer, Limit> sellLimits = new TreeMap<>(); // Ascending order for sells
  Map<Integer, Limit> limitHashMap = new HashMap<>(); // For fast price-level lookup

  Limit highestBuy;
  Limit lowestSell;
  private int currentPrice = 0; // Initialize currentPrice

  public void addOrder(Order order) {
    long startTime = System.nanoTime();

    // Add order to global map
    orders.put(order.idNumber, order);

    // Determine the correct tree and limit structure
    TreeMap<Integer, Limit> limitTree = order.isBuy ? buyLimits : sellLimits;
    Limit limit = limitTree.computeIfAbsent(order.limit, key -> {
      Limit newLimit = new Limit(key);
      limitHashMap.put(key, newLimit); // Store in HashMap for fast access
      return newLimit;
    });

    // Add the order to the limit
    limit.addOrder(order);

    // Update best prices
    updateBestPrices(order);

    long endTime = System.nanoTime();
    System.out.println(String.format("Time taken - addOrder (microseconds): %d", (endTime - startTime) / 1000));
  }

  public void removeOrder(int orderId) {
    Order order = orders.remove(orderId);
    if (order != null) {
      Limit limit = order.parentLimit;
      limit.removeOrder(order);

      // If the limit is now empty, remove it from both TreeMap and HashMap
      if (limit.size == 0) {
        TreeMap<Integer, Limit> limitTree = order.isBuy ? buyLimits : sellLimits;
        limitTree.remove(limit.limitPrice);
        limitHashMap.remove(limit.limitPrice);
        updateBestPricesAfterRemoval(order.isBuy);
      }
    }
  }

  public void executeOrder(int orderId, int sharesToExecute) {
    Order order = orders.get(orderId);
    if (order != null) {
      order.shares -= sharesToExecute;
      order.parentLimit.totalVolume -= sharesToExecute;

      if (order.shares <= 0) {
        removeOrder(orderId); // Fully executed, remove order
      }
    }
  }

  public List<Trade> placeLimitOrder(Order incomingOrder) {
    long startTime = System.nanoTime();

    List<Trade> tradesExecuted = new ArrayList<Trade>();

    if (incomingOrder.isBuy) {
      // Match buy order against sell limits
      while (incomingOrder.shares > 0 && !sellLimits.isEmpty()) {
        Limit sellLimit = sellLimits.firstEntry().getValue();
        if (incomingOrder.limit < sellLimit.limitPrice) {
          // Stop if no sell orders meet the buy price
          break;
        }
        Order sellOrder = sellLimit.headOrder;
        int sellOrderId = sellOrder.idNumber;
        int sellOrderPrice = sellOrder.limit;

        int sharesToMatch = Math.min(incomingOrder.shares, sellOrder.shares);

        // Execute trade
        executeOrder(sellOrder.idNumber, sharesToMatch);

        incomingOrder.shares -= sharesToMatch;

        tradesExecuted.add(new Trade(incomingOrder.idNumber, sellOrderId, sharesToMatch, sellOrderPrice));
        currentPrice = sellOrderPrice;
      }
    } else {
      // Match sell order against buy limits
      while (incomingOrder.shares > 0 && !buyLimits.isEmpty()) {
        Limit buyLimit = buyLimits.firstEntry().getValue();
        if (incomingOrder.limit > buyLimit.limitPrice) {
          // Stop if no buy orders meet the sell price
          break;
        }
        Order buyOrder = buyLimit.headOrder;
        int buyOrderId = buyOrder.idNumber;
        int buyOrderPrice = buyOrder.limit;

        int sharesToMatch = Math.min(incomingOrder.shares, buyOrder.shares);

        // Execute trade
        executeOrder(buyOrder.idNumber, sharesToMatch);

        incomingOrder.shares -= sharesToMatch;

        tradesExecuted.add(new Trade(buyOrderId, incomingOrder.idNumber, sharesToMatch, buyOrderPrice));
        currentPrice = buyOrderPrice;
      }
    }

    // If the incoming order still has unfilled shares, add it to the book
    if (incomingOrder.shares > 0) {
      addOrder(incomingOrder);
    }

    long endTime = System.nanoTime();
    String logMsg = String.format("Time taken - placeLimitOrder (microseconds): %d", (endTime - startTime) / 1000);
    System.out.println(logMsg);

    return tradesExecuted;
  }

  public List<Trade> placeMarketOrder(Order marketOrder) {
    long startTime = System.nanoTime();

    List<Trade> tradesExecuted = new ArrayList<Trade>();

    if (marketOrder.isBuy) {
      // Buy market order: Match against the lowest sell prices
      while (marketOrder.shares > 0 && !sellLimits.isEmpty()) {
        Limit sellLimit = sellLimits.firstEntry().getValue();
        Order sellOrder = sellLimit.headOrder;
        int sellOrderId = sellOrder.idNumber;
        int sellOrderPrice = sellOrder.limit;

        int sharesToMatch = Math.min(marketOrder.shares, sellOrder.shares);

        // Execute the trade
        executeOrder(sellOrderId, sharesToMatch);

        marketOrder.shares -= sharesToMatch;

        tradesExecuted.add(new Trade(marketOrder.idNumber, sellOrderId, sharesToMatch, sellOrderPrice));
        currentPrice = sellOrderPrice;
      }
    } else {
      // Sell market order: Match against the highest buy prices
      while (marketOrder.shares > 0 && !buyLimits.isEmpty()) {
        Limit buyLimit = buyLimits.firstEntry().getValue();
        Order buyOrder = buyLimit.headOrder;
        int buyOrderId = buyOrder.idNumber;
        int buyOrderPrice = buyOrder.limit;

        int sharesToMatch = Math.min(marketOrder.shares, buyOrder.shares);

        // Execute the trade
        executeOrder(buyOrderId, sharesToMatch);

        marketOrder.shares -= sharesToMatch;

        tradesExecuted.add(new Trade(buyOrderId, marketOrder.idNumber, sharesToMatch, buyOrderPrice));
        currentPrice = buyOrderPrice;
      }
    }

    // If the market order cannot be fully filled, log the remaining shares
    if (marketOrder.shares > 0) {
      System.out.println("Market order partially filled. Unfilled shares: " + marketOrder.shares);
    }
    long endTime = System.nanoTime();
    String logMsg = String.format("Time taken - placeMarketOrder (microseconds): %d", (endTime - startTime) / 1000);
    System.out.println(logMsg);

    return tradesExecuted;
  }

  public int getBestBid() {
    return buyLimits.isEmpty() ? -1 : buyLimits.firstKey();
  }

  public int getBestOffer() {
    return sellLimits.isEmpty() ? -1 : sellLimits.firstKey();
  }

  public int getSpread() {
    if (buyLimits.isEmpty() || sellLimits.isEmpty()) {
      return -1; // no spread
    }
    return sellLimits.firstKey() - buyLimits.firstKey();
  }

  private void updateBestPrices(Order order) {
    if (order.isBuy) {
      highestBuy = buyLimits.isEmpty() ? null : buyLimits.firstEntry().getValue();
    } else {
      lowestSell = sellLimits.isEmpty() ? null : sellLimits.firstEntry().getValue();
    }
  }

  private void updateBestPricesAfterRemoval(boolean isBuyOrder) {
    if (isBuyOrder) {
      highestBuy = buyLimits.isEmpty() ? null : buyLimits.firstEntry().getValue();
    } else {
      lowestSell = sellLimits.isEmpty() ? null : sellLimits.firstEntry().getValue();
    }
  }

  public void validateOrder(Order order) {
    if (order == null || order.shares <= 0) {
      throw new IllegalArgumentException("Invalid order: Order is null or shares are <= 0.");
    }
    // Limit price should only be validated for limit orders
    if (!order.isMarketOrder && order.limit <= 0) {
      throw new IllegalArgumentException("Invalid limit price for order.");
    }
  }

  public String getOrderBookSnapshot() {
    StringBuilder snapshot = new StringBuilder("Order Book Snapshot:\n");
    snapshot.append("BUY:\n");
    buyLimits
        .forEach((price, limit) -> snapshot.append(String.format("Price: %d, Volume: %d\n", price, limit.totalVolume)));
    snapshot.append("SELL:\n");
    sellLimits
        .forEach((price, limit) -> snapshot.append(String.format("Price: %d, Volume: %d\n", price, limit.totalVolume)));
    return snapshot.toString();
  }

  public int getCurrentPrice() {
    return currentPrice;
  }
}