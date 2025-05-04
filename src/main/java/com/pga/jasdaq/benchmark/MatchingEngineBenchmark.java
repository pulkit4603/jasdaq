package com.pga.jasdaq.benchmark;

import com.pga.jasdaq.matchingengine.IMatchingEngine;
import com.pga.jasdaq.matchingengine.MatchingEngine;
import com.pga.jasdaq.orderbook.Book;
import com.pga.jasdaq.orderbook.IBook;
import com.pga.jasdaq.orderbook.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatchingEngineBenchmark {

    private final IMatchingEngine matchingEngine;
    private final Random random = new Random();
    private int nextOrderId = 1;
    
    public MatchingEngineBenchmark() {
        IBook orderBook = new Book();
        this.matchingEngine = new MatchingEngine(orderBook);
    }
    
    /**
     * Measures average latency for processing a single order
     */
    public BenchmarkResult measureOrderLatency(int numOrders) {
        List<Long> latencies = new ArrayList<>(numOrders);
        
        for (int i = 0; i < numOrders; i++) {
            Order order = generateRandomOrder();
            long startTime = System.nanoTime();
            
            if (order.isMarketOrder()) {
                matchingEngine.placeMarketOrder(order);
            } else {
                matchingEngine.placeLimitOrder(order);
            }
            
            long endTime = System.nanoTime();
            latencies.add(endTime - startTime);
        }
        
        return new BenchmarkResult(latencies);
    }
    
    /**
     * Measures throughput (orders/second) under load
     */
    public double measureOrderThroughput(int numOrders, int batchSize) {
        List<Order> orders = new ArrayList<>(numOrders);
        
        // Pre-generate orders to avoid generation overhead during test
        for (int i = 0; i < numOrders; i++) {
            orders.add(generateRandomOrder());
        }
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < numOrders; i += batchSize) {
            int endIndex = Math.min(i + batchSize, numOrders);
            
            for (int j = i; j < endIndex; j++) {
                Order order = orders.get(j);
                
                if (order.isMarketOrder()) {
                    matchingEngine.placeMarketOrder(order);
                } else {
                    matchingEngine.placeLimitOrder(order);
                }
            }
        }
        
        long endTime = System.nanoTime();
        double totalSeconds = (endTime - startTime) / 1_000_000_000.0;
        
        return numOrders / totalSeconds;
    }
    
    /**
     * Tests matching performance under realistic book conditions
     */
    public BenchmarkResult runFullMatchingScenario(int numOrders, double matchingRatio) {
        // First populate the book with limit orders
        int initialOrders = numOrders / 3;
        for (int i = 0; i < initialOrders; i++) {
            Order order = generateLimitOrder(true, 100, 90 + random.nextInt(20));
            matchingEngine.placeLimitOrder(order);
            
            order = generateLimitOrder(false, 100, 90 + random.nextInt(20));
            matchingEngine.placeLimitOrder(order);
        }
        
        // Now measure matching performance
        List<Long> latencies = new ArrayList<>(numOrders - initialOrders * 2);
        for (int i = 0; i < numOrders - initialOrders * 2; i++) {
            boolean shouldMatch = random.nextDouble() < matchingRatio;
            Order order;
            
            if (shouldMatch) {
                // Create order likely to match
                boolean isBuy = random.nextBoolean();
                int price = isBuy ? 110 : 90;
                order = generateLimitOrder(isBuy, 100, price);
            } else {
                // Create order unlikely to match
                boolean isBuy = random.nextBoolean();
                int price = isBuy ? 90 : 110;
                order = generateLimitOrder(isBuy, 100, price);
            }
            
            long startTime = System.nanoTime();
            matchingEngine.placeLimitOrder(order);
            long endTime = System.nanoTime();
            
            latencies.add(endTime - startTime);
        }
        
        return new BenchmarkResult(latencies);
    }
    
    private Order generateRandomOrder() {
        boolean isBuy = random.nextBoolean();
        int shares = 100 * (1 + random.nextInt(10));
        
        if (random.nextDouble() < 0.2) {  // 20% market orders
            return new Order(nextOrderId++, isBuy, shares, null);
        } else {
            int price = 90 + random.nextInt(20);  // Price between 90 and 110
            return new Order(nextOrderId++, isBuy, shares, price);
        }
    }
    
    private Order generateLimitOrder(boolean isBuy, int shares, int price) {
        return new Order(nextOrderId++, isBuy, shares, price);
    }
    
    public static class BenchmarkResult {
        private final List<Long> latencies;
        
        public BenchmarkResult(List<Long> latencies) {
            this.latencies = latencies;
        }
        
        public double getAverageLatencyNs() {
            return latencies.stream().mapToLong(l -> l).average().orElse(0);
        }
        
        public double getAverageLatencyMs() {
            return getAverageLatencyNs() / 1_000_000;
        }
        
        public long getP99LatencyNs() {
            List<Long> sorted = new ArrayList<>(latencies);
            sorted.sort(Long::compare);
            int index = (int) Math.ceil(sorted.size() * 0.99) - 1;
            return sorted.get(index);
        }
        
        public double getP99LatencyMs() {
            return getP99LatencyNs() / 1_000_000;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Results (samples: %d):\n" +
                "  Avg latency: %.3f ms\n" +
                "  P99 latency: %.3f ms", 
                latencies.size(),
                getAverageLatencyMs(),
                getP99LatencyMs()
            );
        }
    }
}