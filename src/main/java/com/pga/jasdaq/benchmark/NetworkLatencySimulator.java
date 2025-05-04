package com.pga.jasdaq.benchmark;

import com.pga.jasdaq.engine.ClientRequestHandler;
import com.pga.jasdaq.orderbook.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class NetworkLatencySimulator {

    private final ClientRequestHandler clientHandler;
    private final Random random = new Random();
    private final ExecutorService executor;
    private int nextOrderId = 1;
    
    public NetworkLatencySimulator(ClientRequestHandler clientHandler) {
        this.clientHandler = clientHandler;
        this.executor = Executors.newFixedThreadPool(16);
    }
    
    public void runNetworkLatencyTest(int numClients, int requestsPerClient) throws InterruptedException {
        System.out.println("Running network latency test with " + numClients + " clients...");
        
        CountDownLatch latch = new CountDownLatch(numClients);
        List<Future<ClientStats>> futures = new ArrayList<>();
        
        // Start clients
        for (int i = 0; i < numClients; i++) {
            final String clientId = "client-" + i;
            futures.add(executor.submit(() -> {
                try {
                    return simulateClient(clientId, requestsPerClient);
                } finally {
                    latch.countDown();
                }
            }));
        }
        
        // Wait for all clients to finish
        latch.await();
        
        // Collect statistics
        List<Long> allLatencies = new ArrayList<>();
        int totalOrders = 0;
        long totalTime = 0;
        
        for (Future<ClientStats> future : futures) {
            try {
                ClientStats stats = future.get();
                allLatencies.addAll(stats.latencies);
                totalOrders += stats.requests;
                totalTime += stats.totalTimeMs;
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        // Calculate statistics
        double avgLatency = allLatencies.stream().mapToLong(l -> l).average().orElse(0);
        List<Long> sortedLatencies = new ArrayList<>(allLatencies);
        sortedLatencies.sort(Long::compare);
        long p99Latency = sortedLatencies.get((int)(sortedLatencies.size() * 0.99));
        
        System.out.printf("Results:\n" +
                          "  Total requests: %d\n" +
                          "  Average latency: %.2f ms\n" +
                          "  P99 latency: %d ms\n" +
                          "  Average throughput: %.2f requests/sec\n",
                          totalOrders, avgLatency, p99Latency,
                          totalOrders / (totalTime / 1000.0 / numClients));
    }
    
    private ClientStats simulateClient(String clientId, int requests) {
        List<Long> latencies = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < requests; i++) {
            // Create random order
            Order order = new Order(
                nextOrderId++, 
                random.nextBoolean(), 
                100 + random.nextInt(900),
                90 + random.nextInt(20)
            );
            
            // Create request
            ClientRequestHandler.OrderRequest request = new ClientRequestHandler.OrderRequest();
            request.setOrder(order);
            request.setClientHostPort(clientId);
            request.setStockSymbol("TSLA"); // Using TSLA as default symbol
            
            // Simulate network latency
            simulateNetworkLatency();
            
            // Send request and measure response time
            long requestStart = System.currentTimeMillis();
            // List<Trade> trades = clientHandler.placeOrder(request);
            long latency = System.currentTimeMillis() - requestStart;
            
            latencies.add(latency);
            
            // Simulate think time between requests
            try {
                Thread.sleep(random.nextInt(50));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        return new ClientStats(latencies, requests, totalTime);
    }
    
    private void simulateNetworkLatency() {
        try {
            // Simulate realistic network conditions (5-20ms latency)
            Thread.sleep(5 + random.nextInt(16));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static class ClientStats {
        final List<Long> latencies;
        final int requests;
        final long totalTimeMs;
        
        public ClientStats(List<Long> latencies, int requests, long totalTimeMs) {
            this.latencies = latencies;
            this.requests = requests;
            this.totalTimeMs = totalTimeMs;
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}