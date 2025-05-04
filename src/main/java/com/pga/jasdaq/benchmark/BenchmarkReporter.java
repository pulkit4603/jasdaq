package com.pga.jasdaq.benchmark;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class BenchmarkReporter {

    private static final String REPORT_DIRECTORY = "benchmark-reports/";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    public static void writeReport(Map<String, Object> results) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String filename = REPORT_DIRECTORY + "benchmark_" + timestamp + ".csv";
        
        try {
            // Create directory if it doesn't exist
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(REPORT_DIRECTORY));
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                // Write header
                writer.write("Metric,Value\n");
                
                // Write all results
                for (Map.Entry<String, Object> entry : results.entrySet()) {
                    writer.write(entry.getKey() + "," + entry.getValue() + "\n");
                }
                
                System.out.println("Benchmark report written to: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Failed to write benchmark report: " + e.getMessage());
        }
    }
    
    public static void writeLatencyDistribution(String testName, List<Long> latencies) {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String filename = REPORT_DIRECTORY + testName + "_distribution_" + timestamp + ".csv";
        
        try {
            // Create directory if it doesn't exist
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(REPORT_DIRECTORY));
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                // Write header
                writer.write("Latency_ns\n");
                
                // Write all latencies
                for (Long latency : latencies) {
                    writer.write(latency + "\n");
                }
                
                System.out.println("Latency distribution written to: " + filename);
            }
        } catch (IOException e) {
            System.err.println("Failed to write latency distribution: " + e.getMessage());
        }
    }
}