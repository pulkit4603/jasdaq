package com.pga.jasdaq.benchmark;

import com.pga.jasdaq.matchingengine.IMatchingEngine;
import com.pga.jasdaq.matchingengine.MatchingEngine;
import com.pga.jasdaq.orderbook.Book;
import com.pga.jasdaq.orderbook.IBook;
import com.pga.jasdaq.orderbook.Order;

import java.util.*;
import java.util.concurrent.*;

public class IndustryScenarios {

    private final IMatchingEngine matchingEngine;
    private int nextOrderId = 1;
    private final Random random = new Random();
    
    public IndustryScenarios() {
        IBook orderBook = new Book();
        this.matchingEngine = new MatchingEngine(orderBook);
    }
    
    /**
     * Market Open Scenario - High volume of orders after market opens
     */
    public IndustryScenariosReport.ScenarioResult testMarketOpenScenario(int orderCount) throws InterruptedException {
        System.out.println("Running Market Open Scenario...");
        
        // Pregenerate orders
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < orderCount; i++) {
            boolean isBuy = random.nextBoolean();
            int price = 90 + random.nextInt(20);
            orders.add(new Order(nextOrderId++, isBuy, 100 + random.nextInt(900), price));
        }
        
        // Process with high concurrency simulation
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(4); // Reduced thread count to 4
        
        // Submit in bursts to simulate market open pattern
        for (int i = 0; i < orders.size(); i += 100) {
            int endIndex = Math.min(i + 100, orders.size());
            List<Order> batch = orders.subList(i, endIndex);
            
            // Submit each order in the batch
            List<Future<?>> futures = new ArrayList<>();
            for (Order order : batch) {
                futures.add(executor.submit(() -> matchingEngine.placeLimitOrder(order)));
            }
            
            // Wait for batch to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            
            // Small pause between batches
            Thread.sleep(1);
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        long duration = System.currentTimeMillis() - startTime;
        double throughput = orders.size() * 1000.0 / duration;
        System.out.printf("Processed %d orders in %d ms (%.2f orders/sec)%n", 
            orders.size(), duration, throughput);
        
        return new IndustryScenariosReport.ScenarioResult(orders.size(), throughput);
    }
    
    /**
     * Flash Crash Scenario - High volume of market orders in short time
     */
    public IndustryScenariosReport.ScenarioResult testFlashCrashScenario() {
        System.out.println("Running Flash Crash Scenario...");
        
        // First establish normal book
        for (int i = 0; i < 2000; i++) {
            matchingEngine.placeLimitOrder(new Order(
                nextOrderId++, true, 100, 90 + random.nextInt(10)));
            matchingEngine.placeLimitOrder(new Order(
                nextOrderId++, false, 100, 100 + random.nextInt(10)));
        }
        
        System.out.println("Order book before crash: " + matchingEngine.getOrderBookSnapshot());
        
        // Execute flash crash - massive sell-off
        int orderCount = 5000; // Reduced count to avoid hanging
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < orderCount; i++) {
            // 80% market sell orders
            if (random.nextDouble() < 0.8) {
                matchingEngine.placeMarketOrder(new Order(nextOrderId++, false, 
                    100 + random.nextInt(500), null));
            } else {
                matchingEngine.placeLimitOrder(new Order(nextOrderId++, false, 
                    100 + random.nextInt(500), 85 + random.nextInt(10)));
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        double throughput = orderCount * 1000.0 / duration;
        System.out.printf("Flash crash: %d orders in %d ms (%.2f orders/sec)%n", 
            orderCount, duration, throughput);
        
        return new IndustryScenariosReport.ScenarioResult(orderCount, throughput);
    }
    
    /**
     * High-Frequency Trading Scenario - Rapid order placement and cancellation
     */
    public IndustryScenariosReport.ScenarioResult testHFTScenario() {
        System.out.println("Running HFT Scenario...");
        
        // Queue to track orders that can be cancelled
        Queue<Integer> orderIds = new LinkedList<>();
        
        long startTime = System.currentTimeMillis();
        int operations = 0;
        
        // Run for 3 seconds (reduced from 5)
        while (System.currentTimeMillis() - startTime < 3000) {
            operations++;
            
            if (orderIds.size() > 100 && random.nextDouble() < 0.4) {
                // Cancel an existing order
                Integer orderId = orderIds.poll();
                matchingEngine.cancelOrder(orderId);
            } else {
                // Place a new order
                boolean isBuy = random.nextBoolean();
                int price = 95 + random.nextInt(10);
                Order order = new Order(nextOrderId, isBuy, 
                    10 + random.nextInt(90), price);
                matchingEngine.placeLimitOrder(order);
                orderIds.add(nextOrderId);
                nextOrderId++;
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        double throughput = operations * 1000.0 / duration;
        System.out.printf("HFT scenario: %d operations in %d ms (%.2f ops/sec)%n",
            operations, duration, throughput);
        
        return new IndustryScenariosReport.ScenarioResult(operations, throughput);
    }
    
    public static void main(String[] args) throws InterruptedException {
        IndustryScenarios scenarios = new IndustryScenarios();
        
        // Get order counts from arguments if provided
        int marketOpenOrderCount = 10000; // Default reduced values
        
        if (args.length > 0) {
            try {
                marketOpenOrderCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid market open order count, using default: " + marketOpenOrderCount);
            }
        }
        
        scenarios.testMarketOpenScenario(marketOpenOrderCount);
        scenarios.testFlashCrashScenario();
        scenarios.testHFTScenario();
    }
}