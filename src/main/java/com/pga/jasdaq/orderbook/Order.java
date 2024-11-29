package com.pga.jasdaq.orderbook;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class Order {
  public int idNumber;
  public boolean isBuy; // true for buy, false for sell
  public boolean isMarketOrder; // true for market orders, false for limit orders
  public int shares;
  public int limit; // Ignored for market orders
  public long entryTime;
  public long eventTime;
  public Order nextOrder;
  public Order prevOrder;
  public Limit parentLimit;

  // Default constructor for Jackson
  public Order() {
  }

  /**
   * Unified constructor to handle both limit and market orders.
   * 
   * @param idNumber The order ID.
   * @param isBuy    True for buy orders, false for sell orders.
   * @param shares   The number of shares.
   * @param limit    The limit price for limit orders (ignored for market orders).
   */
  @JsonCreator
  public Order(
      @JsonProperty("idNumber") int idNumber,
      @JsonProperty("isBuy") boolean isBuy,
      @JsonProperty("shares") int shares,
      @JsonProperty("limit") Integer limit) { // Use Integer to allow null for market orders
    this.idNumber = idNumber;
    this.isBuy = isBuy;
    this.shares = shares;

    if (limit == null) {
      this.isMarketOrder = true; // Market order
      this.limit = isBuy ? Integer.MAX_VALUE : Integer.MIN_VALUE; // Sentinel value for market orders
    } else {
      this.isMarketOrder = false; // Limit order
      this.limit = limit;
    }

    this.entryTime = Instant.now().toEpochMilli();
  }

  // Getters and Setters
  @JsonProperty("idNumber")
  public int getIdNumber() {
    return idNumber;
  }

  public void setIdNumber(int idNumber) {
    this.idNumber = idNumber;
  }

  @JsonProperty("isBuy")
  public boolean isBuy() {
    return isBuy;
  }

  public void setBuy(boolean isBuy) {
    this.isBuy = isBuy;
  }

  @JsonProperty("isMarketOrder")
  public boolean isMarketOrder() {
    return isMarketOrder;
  }

  public void setMarketOrder(boolean isMarketOrder) {
    this.isMarketOrder = isMarketOrder;
  }

  @JsonProperty("shares")
  public int getShares() {
    return shares;
  }

  public void setShares(int shares) {
    this.shares = shares;
  }

  @JsonProperty("limit")
  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  @JsonProperty("entryTime")
  public long getEntryTime() {
    return entryTime;
  }

  public void setEntryTime(long entryTime) {
    this.entryTime = entryTime;
  }

  @JsonProperty("eventTime")
  public long getEventTime() {
    return eventTime;
  }

  public void setEventTime(long eventTime) {
    this.eventTime = eventTime;
  }

  @JsonProperty("nextOrder")
  public Order getNextOrder() {
    return nextOrder;
  }

  public void setNextOrder(Order nextOrder) {
    this.nextOrder = nextOrder;
  }

  @JsonProperty("prevOrder")
  public Order getPrevOrder() {
    return prevOrder;
  }

  public void setPrevOrder(Order prevOrder) {
    this.prevOrder = prevOrder;
  }

  @JsonProperty("parentLimit")
  public Limit getParentLimit() {
    return parentLimit;
  }

  public void setParentLimit(Limit parentLimit) {
    this.parentLimit = parentLimit;
  }

  @Override
  public String toString() {
    return String.format(
        "Order{id=%d, type=%s, side=%s, shares=%d, limit=%d, entryTime=%d}",
        idNumber,
        isMarketOrder ? "MARKET" : "LIMIT",
        isBuy ? "BUY" : "SELL",
        shares,
        limit,
        entryTime);
  }
}
