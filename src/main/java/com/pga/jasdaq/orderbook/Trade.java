package com.pga.jasdaq.orderbook;

import java.time.Instant;

public class Trade {
  int buyOrderId;
  int sellOrderId;
  int sharesTraded;
  int tradePrice;
  long timestamp; // Epoch milliseconds

  public Trade(int buyOrderId, int sellOrderId, int sharesTraded, int tradePrice) {
    this.buyOrderId = buyOrderId;
    this.sellOrderId = sellOrderId;
    this.sharesTraded = sharesTraded;
    this.tradePrice = tradePrice;
    this.timestamp = Instant.now().toEpochMilli(); // Set trade execution time
  }

  public int getTradePrice() {
    return tradePrice;
  }

  public int getSharesTraded() {
    return sharesTraded;
  }

  public int getBuyOrderId() {
    return buyOrderId;
  }

  public int getSellOrderId() {
    return sellOrderId;
  }

  @Override
  public String toString() {
    return String.format("Trade{buyOrderId=%d, sellOrderId=%d, shares=%d, price=%d, timestamp=%d}",
        buyOrderId, sellOrderId, sharesTraded, tradePrice, timestamp);
  }
}
