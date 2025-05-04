package com.pga.jasdaq.benchmark;

import java.util.HashMap;
import java.util.Map;

public class IndustryScenariosReport {
    
    public static class ScenarioResult {
        public final int orderCount;
        public final double throughput;
        
        public ScenarioResult(int orderCount, double throughput) {
            this.orderCount = orderCount;
            this.throughput = throughput;
        }
    }
    
    public static void main(String[] args) {
        try {
            Map<String, Object> results = new HashMap<>();
            IndustryScenarios scenarios = new IndustryScenarios();
            
            // Run Market Open Scenario
            System.out.println("\n=== Market Open Scenario ===");
            long startTime = System.currentTimeMillis();
            int orderCount = 50000; // Default value, can be overridden by args
            
            // Check if we have arguments for order count
            if (args.length > 0) {
                try {
                    orderCount = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid order count argument, using default: " + orderCount);
                }
            }
            
            ScenarioResult marketOpenResult = scenarios.testMarketOpenScenario(orderCount);
            long duration = System.currentTimeMillis() - startTime;
            
            results.put("MarketOpen_Duration_ms", duration);
            results.put("MarketOpen_OrderCount", marketOpenResult.orderCount);
            results.put("MarketOpen_Throughput_ops_sec", marketOpenResult.throughput);
            
            // Run Flash Crash Scenario
            System.out.println("\n=== Flash Crash Scenario ===");
            startTime = System.currentTimeMillis();
            ScenarioResult flashCrashResult = scenarios.testFlashCrashScenario();
            duration = System.currentTimeMillis() - startTime;
            
            results.put("FlashCrash_Duration_ms", duration);
            results.put("FlashCrash_OrderCount", flashCrashResult.orderCount);
            results.put("FlashCrash_Throughput_ops_sec", flashCrashResult.throughput);
            
            // Run HFT Scenario
            System.out.println("\n=== HFT Scenario ===");
            startTime = System.currentTimeMillis();
            ScenarioResult hftResult = scenarios.testHFTScenario();
            duration = System.currentTimeMillis() - startTime;
            
            results.put("HFT_Duration_ms", duration);
            results.put("HFT_OrderCount", hftResult.orderCount);
            results.put("HFT_Throughput_ops_sec", hftResult.throughput);
            
            // Report results
            System.out.println("\n=== Benchmark Results Summary ===");
            System.out.printf("Market Open: %d orders processed at %.2f orders/sec\n", 
                    marketOpenResult.orderCount, marketOpenResult.throughput);
            System.out.printf("Flash Crash: %d orders processed at %.2f orders/sec\n", 
                    flashCrashResult.orderCount, flashCrashResult.throughput);
            System.out.printf("HFT Trading: %d operations at %.2f ops/sec\n", 
                    hftResult.orderCount, hftResult.throughput);
            
            // Write to report file
            BenchmarkReporter.writeReport(results);
            
        } catch (Exception e) {
            System.err.println("Error during benchmark execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}