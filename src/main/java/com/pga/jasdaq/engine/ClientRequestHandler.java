package com.pga.jasdaq.engine;

import com.pga.jasdaq.orderbook.Order;
import com.pga.jasdaq.orderbook.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class ClientRequestHandler {

  private final IStockMarketEngine stockMarketEngine;

  @Autowired
  public ClientRequestHandler(IStockMarketEngine stockMarketEngine) {
    this.stockMarketEngine = stockMarketEngine;
  }

  /**
   * Endpoint to place an order (limit or market).
   *
   * @param orderRequest The order request details submitted by the client.
   * @return List of trades executed as a result of the order placement.
   */
  @PostMapping("/place")
  public List<Trade> placeOrder(@RequestBody OrderRequest orderRequest) {
    Order order = orderRequest.getOrder();
    String clientId = orderRequest.getClientHostPort();
    String stockSymbol = orderRequest.getStockSymbol();

    // Log the request
    System.out.println("Placing order for clientId: " + clientId + ", stockSymbol: " + stockSymbol);

    // Call stock market engine to place the order
    return stockMarketEngine.placeOrder(order, stockSymbol, clientId);
  }

  /**
   * Endpoint to cancel an order.
   *
   * @param cancelRequest The cancel request details submitted by the client.
   * @return Confirmation message.
   */
  @PostMapping("/cancel")
  public String cancelOrder(@RequestBody CancelRequest cancelRequest) {
    int orderId = cancelRequest.getOrderId();
    String stockSymbol = cancelRequest.getStockSymbol();

    stockMarketEngine.cancelOrder(orderId, stockSymbol);
    return "Order " + orderId + " for stock " + stockSymbol + " successfully canceled.";
  }

  /**
   * Endpoint to get a snapshot of the order book.
   *
   * @param snapshotRequest The snapshot request details submitted by the client.
   * @return The current state of the order book.
   */
  @PostMapping("/snapshot")
  public String getOrderBookSnapshot(@RequestBody SnapshotRequest snapshotRequest) {
    String stockSymbol = snapshotRequest.getStockSymbol();
    return stockMarketEngine.getOrderBookSnapshot(stockSymbol);
  }

  /**
   * Endpoint to return a hello world message.
   *
   * @return A simple "Hello, World!" message.
   */
  @GetMapping("/sayhi")
  public String helloWorld() {
    return "Hello, World!";
  }

  // Request classes for encapsulating request data
  public static class OrderRequest {
    private Order order;
    private String clientHostPort;
    private String stockSymbol;

    // Getters and setters
    public Order getOrder() {
      return order;
    }

    public void setOrder(Order order) {
      this.order = order;
    }

    public String getClientHostPort() {
      return clientHostPort;
    }

    public void setClientHostPort(String clientHostPort) {
      this.clientHostPort = clientHostPort;
    }

    public String getStockSymbol() {
      return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
      this.stockSymbol = stockSymbol;
    }
  }

  public static class CancelRequest {
    private int orderId;
    private String stockSymbol;

    // Getters and setters
    public int getOrderId() {
      return orderId;
    }

    public void setOrderId(int orderId) {
      this.orderId = orderId;
    }

    public String getStockSymbol() {
      return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
      this.stockSymbol = stockSymbol;
    }
  }

  public static class SnapshotRequest {
    private String stockSymbol;

    // Getters and setters
    public String getStockSymbol() {
      return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
      this.stockSymbol = stockSymbol;
    }
  }
}
