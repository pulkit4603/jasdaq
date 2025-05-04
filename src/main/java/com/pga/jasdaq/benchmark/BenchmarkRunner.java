package com.pga.jasdaq.benchmark;

public class BenchmarkRunner {
    
    public static void main(String[] args) {
        MatchingEngineBenchmark benchmark = new MatchingEngineBenchmark();
        
        // Warm up the JVM
        System.out.println("Warming up JVM...");
        benchmark.measureOrderLatency(10000);
        
        // Single order latency test
        System.out.println("\n== Single Order Latency Test ==");
        MatchingEngineBenchmark.BenchmarkResult latencyResult = benchmark.measureOrderLatency(50000);
        System.out.println(latencyResult);
        
        // Throughput test
        System.out.println("\n== Throughput Test ==");
        int numOrders = 100000;
        int batchSize = 1000;
        double ordersPerSecond = benchmark.measureOrderThroughput(numOrders, batchSize);
        System.out.printf("Throughput: %.2f orders/second\n", ordersPerSecond);
        
        // Full matching scenario
        System.out.println("\n== Full Matching Scenario (50% match rate) ==");
        MatchingEngineBenchmark.BenchmarkResult matchingResult = 
            benchmark.runFullMatchingScenario(50000, 0.5);
        System.out.println(matchingResult);
        
        // High match rate scenario
        System.out.println("\n== High Matching Scenario (90% match rate) ==");
        MatchingEngineBenchmark.BenchmarkResult highMatchResult = 
            benchmark.runFullMatchingScenario(50000, 0.9);
        System.out.println(highMatchResult);
    }
}