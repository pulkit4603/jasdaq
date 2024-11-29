package com.pga.jasdaq.orderbook;

public class LimitOrderBook {
  public static void main(String[] args) {
    // Create an instance of the Book class
    Book book = new Book();

    // Test case: Add limit orders to the book
    System.out.println("### TEST: Adding Limit Orders ###");
    Order buyOrder1 = new Order(1, true, 100, 50); // Buy 100 shares at limit price 50
    Order buyOrder2 = new Order(2, true, 200, 55); // Buy 200 shares at limit price 55
    Order sellOrder1 = new Order(3, false, 150, 60); // Sell 150 shares at limit price 60
    Order sellOrder2 = new Order(4, false, 100, 65); // Sell 100 shares at limit price 65

    System.out.println(book.placeLimitOrder(buyOrder1));
    System.out.println(book.placeLimitOrder(buyOrder2));
    System.out.println(book.placeLimitOrder(sellOrder1));
    System.out.println(book.placeLimitOrder(sellOrder2));
    System.out.println("Order Book Snapshot after adding orders:");
    System.out.println(book.getOrderBookSnapshot());

    // Test case: Place an incoming limit order
    System.out.println("\n### TEST: Placing Limit Order ###");
    Order incomingBuyOrder = new Order(5, true, 200, 65); // Buy 100 shares at limit price 65
    System.out.println(book.placeLimitOrder(incomingBuyOrder));
    System.out.println("Order Book Snapshot after matching:");
    System.out.println(book.getOrderBookSnapshot());

    // Test case: Place a market order
    System.out.println("\n### TEST: Placing Market Order ###");
    Order marketSellOrder = new Order(6, false, 250); // Market sell order for 250 shares
    System.out.println(book.placeMarketOrder(marketSellOrder));
    System.out.println("Order Book Snapshot after placing market order:");
    System.out.println(book.getOrderBookSnapshot());

    // Test case: Cancel an order
    System.out.println("\n### TEST: Canceling an Order ###");
    book.removeOrder(2); // Cancel order with ID 2
    System.out.println("Order Book Snapshot after canceling order 2:");
    System.out.println(book.getOrderBookSnapshot());

    // Test case: Get best bid and offer
    System.out.println("\n### TEST: Best Bid and Best Offer ###");
    System.out.println("Best Bid: " + book.getBestBid());
    System.out.println("Best Offer: " + book.getBestOffer());

    // Test case: Get spread
    System.out.println("\n### TEST: Spread ###");
    System.out.println("Spread: " + book.getSpread());

    // Test case: Execute shares from an order
    System.out.println("\n### TEST: Executing Shares ###");
    book.executeOrder(1, 50); // Execute 50 shares from order with ID 1
    System.out.println("Order Book Snapshot after executing shares from order 1:");
    System.out.println(book.getOrderBookSnapshot());

    // Test case: Validate invalid order
    System.out.println("\n### TEST: Validating Invalid Order ###");
    try {
      Order invalidOrder = new Order(7, true, -100, 50); // Invalid order with negative shares
      book.validateOrder(invalidOrder);
    } catch (IllegalArgumentException e) {
      System.out.println("Validation failed: " + e.getMessage());
    }
  }
}
