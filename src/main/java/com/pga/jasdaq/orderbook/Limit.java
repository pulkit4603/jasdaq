package com.pga.jasdaq.orderbook;

class Limit {
  int limitPrice;
  int size;
  int totalVolume;
  Order headOrder;
  Order tailOrder;

  public Limit(int limitPrice) {
    this.limitPrice = limitPrice;
    this.size = 0;
    this.totalVolume = 0;
  }

  public void addOrder(Order order) {
    if (headOrder == null) {
      headOrder = order;
      tailOrder = order;
    } else {
      tailOrder.nextOrder = order;
      order.prevOrder = tailOrder;
      tailOrder = order;
    }
    order.parentLimit = this;
    size++;
    totalVolume += order.shares;
  }

  public void removeOrder(Order order) {
    if (order.prevOrder != null) {
      order.prevOrder.nextOrder = order.nextOrder;
    } else {
      headOrder = order.nextOrder;
    }
    if (order.nextOrder != null) {
      order.nextOrder.prevOrder = order.prevOrder;
    } else {
      tailOrder = order.prevOrder;
    }
    size--;
    totalVolume -= order.shares;
  }
}
