package com.pga.jasdaq.db.service;

import com.pga.jasdaq.db.entity.TradeEntity;
import com.pga.jasdaq.db.repository.TradeRepository;
import com.pga.jasdaq.orderbook.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeService.class);
    private static final String[] STOCK_SYMBOLS = {"TSLA", "AAPL", "MSFT", "AMZN", "GOOG", "META", "RELI", "HIND", "ADNI"};
    private static final String[] ORDER_TYPES = {"BUY", "SELL"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private final TradeRepository tradeRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    @Autowired
    public TradeService(TradeRepository tradeRepository, JdbcTemplate jdbcTemplate) {
        this.tradeRepository = tradeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Persist a trade to the database.
     * 
     * @param trade The trade from the matching engine
     * @param symbol The stock symbol
     * @param isBuy Whether this is a buy order
     * @return The created entity
     */
    @Transactional
    public TradeEntity saveTrade(Trade trade, String symbol, boolean isBuy) {
        long startTime = System.nanoTime();
        
        TradeEntity tradeEntity = new TradeEntity(
            symbol,
            BigDecimal.valueOf(trade.getTradePrice()).setScale(2, RoundingMode.HALF_UP),
            trade.getSharesTraded(),
            LocalDateTime.now(),
            isBuy ? "BUY" : "SELL"
        );
        
        TradeEntity savedEntity = tradeRepository.save(tradeEntity);
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        logger.debug("Trade saved to database in {} ms: {}", executionTime, tradeEntity);
        
        return savedEntity;
    }
    
    /**
     * Get the last N trades for a symbol.
     */
    public List<TradeEntity> getLastTrades(String symbol, int count) {
        long startTime = System.nanoTime();
        
        List<TradeEntity> trades = tradeRepository.findBySymbolOrderByTimestampDesc(
            symbol, PageRequest.of(0, count)
        );
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;
        
        logger.debug("Retrieved last {} trades for {} in {} ms", count, symbol, executionTime);
        
        return trades;
    }
    
    /**
     * Calculate average price for a symbol in the last X minutes.
     */
    public BigDecimal getAveragePrice(String symbol, int minutes) {
        long startTime = System.nanoTime();
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        BigDecimal avgPrice = tradeRepository.calculateAveragePriceInTimeFrame(symbol, cutoffTime);
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;
        
        logger.debug("Calculated average price for {} in last {} minutes: {} (took {} ms)", 
                    symbol, minutes, avgPrice, executionTime);
        
        return avgPrice != null ? avgPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    
    /**
     * Count total trades for a symbol in the last X minutes.
     */
    public long countTradesInTimeFrame(String symbol, int minutes) {
        long startTime = System.nanoTime();
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        long tradeCount = tradeRepository.countTradesInTimeFrame(symbol, cutoffTime);
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;
        
        logger.debug("Counted {} trades for {} in last {} minutes (took {} ms)",
                tradeCount, symbol, minutes, executionTime);
        
        return tradeCount;
    }
    
    /**
     * Find minimum and maximum trade price for a symbol over a time window.
     */
    public Map<String, BigDecimal> getMinMaxPrice(String symbol, int minutes) {
        long startTime = System.nanoTime();
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        Object[] result = tradeRepository.findMinMaxPriceInTimeFrame(symbol, cutoffTime);
        
        Map<String, BigDecimal> minMax = new HashMap<>();
        if (result != null && result.length == 2) {
            minMax.put("min", result[0] != null ? ((BigDecimal) result[0]).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            minMax.put("max", result[1] != null ? ((BigDecimal) result[1]).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        } else {
            minMax.put("min", BigDecimal.ZERO);
            minMax.put("max", BigDecimal.ZERO);
        }
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;
        
        logger.debug("Found min/max prices for {} in last {} minutes: min={}, max={} (took {} ms)",
                symbol, minutes, minMax.get("min"), minMax.get("max"), executionTime);
        
        return minMax;
    }
    
    /**
     * Query top N symbols by trade volume over the last Y minutes.
     */
    public List<Map<String, Object>> getTopSymbolsByVolume(int minutes, int limit) {
        long startTime = System.nanoTime();
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
        List<Object[]> results = tradeRepository.findTopSymbolsByVolumeInTimeFrame(
                cutoffTime, PageRequest.of(0, limit));
        
        List<Map<String, Object>> topSymbols = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> symbolData = new HashMap<>();
            symbolData.put("symbol", row[0]);
            symbolData.put("totalVolume", row[1]);
            topSymbols.add(symbolData);
        }
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;
        
        logger.debug("Found top {} symbols by volume in last {} minutes (took {} ms)",
                limit, minutes, executionTime);
        
        return topSymbols;
    }
    
    /**
     * Insert a batch of trades for performance testing.
     */
    @Transactional
    public Map<String, Object> insertBatchTrades(int count, String symbol) {
        long startTime = System.nanoTime();
        
        // Build the batch insert SQL
        StringBuilder sql = new StringBuilder(
            "INSERT INTO trades (symbol, price, volume, timestamp, order_type) VALUES ");
        
        for (int i = 0; i < count; i++) {
            BigDecimal price = BigDecimal.valueOf(20 + 980 * random.nextDouble())
                .setScale(2, RoundingMode.HALF_UP);
            int volume = 10 + random.nextInt(990);
            
            // Random timestamp in last 24 hours
            LocalDateTime timestamp = LocalDateTime.now()
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));
                
            String orderType = ORDER_TYPES[random.nextInt(ORDER_TYPES.length)];
            
            sql.append(String.format("('%s', %s, %d, '%s', '%s')", 
                symbol, price, volume, timestamp, orderType));
            
            if (i < count - 1) {
                sql.append(", ");
            }
        }
        
        jdbcTemplate.execute(sql.toString());
        
        long endTime = System.nanoTime();
        long executionTimeMs = (endTime - startTime) / 1_000_000;
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("executionTimeMs", executionTimeMs);
        result.put("tradesPerSecond", count * 1000.0 / executionTimeMs);
        
        logger.debug("Inserted batch of {} trades for {} in {} ms ({} trades/sec)", 
            count, symbol, executionTimeMs, result.get("tradesPerSecond"));
        
        return result;
    }
    
    /**
     * Reset the trades table by truncating it.
     */
    @Transactional
    public void resetTable() {
        jdbcTemplate.execute("TRUNCATE TABLE trades");
        logger.info("Trades table has been reset");
    }
    
    /**
     * Apply a single-column index on symbol.
     */
    public void applySingleColumnIndex() {
        dropAllIndexes();
        jdbcTemplate.execute("CREATE INDEX idx_symbol ON trades(symbol)");
        logger.info("Applied single-column index on symbol");
    }
    
    /**
     * Apply a composite index on symbol and timestamp.
     */
    public void applyCompositeIndex() {
        dropAllIndexes();
        jdbcTemplate.execute("CREATE INDEX idx_symbol_ts ON trades(symbol, timestamp)");
        logger.info("Applied composite index on symbol and timestamp");
    }
    
    /**
     * Drop all indexes on the trades table.
     */
    public void dropAllIndexes() {
        // Query to get all indexes except PRIMARY
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
            "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
            "WHERE TABLE_SCHEMA = 'stockmarket' AND TABLE_NAME = 'trades' AND INDEX_NAME != 'PRIMARY'"
        );
        
        for (Map<String, Object> index : indexes) {
            String indexName = (String) index.get("INDEX_NAME");
            jdbcTemplate.execute("DROP INDEX IF EXISTS " + indexName + " ON trades");
            logger.info("Dropped index: {}", indexName);
        }
    }
    
    /**
     * Generate and insert a bulk number of random trades.
     */
    @Transactional
    public Map<String, Object> generateBulkTrades(int count) {
        long startTime = System.nanoTime();
        
        // First delete all existing trades
        resetTable();
        
        // Insert in batches for better performance
        int batchSize = 5000;
        int totalBatches = (count + batchSize - 1) / batchSize;
        
        for (int batch = 0; batch < totalBatches; batch++) {
            int batchCount = Math.min(batchSize, count - batch * batchSize);
            if (batchCount <= 0) break;
            
            // Create batch of random trades
            StringBuilder sql = new StringBuilder(
                "INSERT INTO trades (symbol, price, volume, timestamp, order_type) VALUES ");
            
            for (int i = 0; i < batchCount; i++) {
                String symbol = STOCK_SYMBOLS[random.nextInt(STOCK_SYMBOLS.length)];
                BigDecimal price = BigDecimal.valueOf(20 + 980 * random.nextDouble())
                    .setScale(2, RoundingMode.HALF_UP);
                int volume = 10 + random.nextInt(990);
                
                // Random timestamp in last 7 days
                LocalDateTime timestamp = LocalDateTime.now().minusDays(random.nextInt(7))
                    .minusHours(random.nextInt(24))
                    .minusMinutes(random.nextInt(60));
                    
                String orderType = ORDER_TYPES[random.nextInt(ORDER_TYPES.length)];
                
                sql.append(String.format("('%s', %s, %d, '%s', '%s')", 
                    symbol, price, volume, timestamp, orderType));
                
                if (i < batchCount - 1) {
                    sql.append(", ");
                }
            }
            
            jdbcTemplate.execute(sql.toString());
            logger.info("Inserted batch {} of {} ({} trades)", batch + 1, totalBatches, batchCount);
        }
        
        long endTime = System.nanoTime();
        long executionTimeMs = (endTime - startTime) / 1_000_000;
        
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("executionTimeMs", executionTimeMs);
        result.put("tradesPerSecond", count * 1000.0 / executionTimeMs);
        
        logger.info("Generated {} random trades in {} ms ({} trades/sec)", 
            count, executionTimeMs, result.get("tradesPerSecond"));
        
        return result;
    }
    
    /**
     * Run a benchmark with both index types.
     */
    public Map<String, Object> runFullBenchmark(int tradeCount, String symbol, int lastTradesCount, 
                                               int timeWindowMinutes, int batchSize, int topSymbolsCount) {
        Map<String, Object> results = new HashMap<>();
        Map<String, Long> singleColumnResults = new HashMap<>();
        Map<String, Long> compositeIndexResults = new HashMap<>();
        
        // Generate data
        generateBulkTrades(tradeCount);
        
        // Test with single column index
        applySingleColumnIndex();
        singleColumnResults.put("getLastTrades", benchmarkLastTrades(symbol, lastTradesCount));
        singleColumnResults.put("getAveragePrice", benchmarkAveragePrice(symbol, timeWindowMinutes));
        singleColumnResults.put("countTrades", benchmarkCountTrades(symbol, timeWindowMinutes));
        singleColumnResults.put("getMinMaxPrice", benchmarkMinMaxPrice(symbol, timeWindowMinutes));
        singleColumnResults.put("topSymbolsByVolume", benchmarkTopSymbolsByVolume(timeWindowMinutes, topSymbolsCount));
        singleColumnResults.put("insertBatchTrades", benchmarkInsertBatchTrades(batchSize, symbol));
        singleColumnResults.put("getTradesBySymbolOnly", benchmarkSymbolOnlyQuery(symbol, lastTradesCount));
        
        // Test with composite index
        applyCompositeIndex();
        compositeIndexResults.put("getLastTrades", benchmarkLastTrades(symbol, lastTradesCount));
        compositeIndexResults.put("getAveragePrice", benchmarkAveragePrice(symbol, timeWindowMinutes));
        compositeIndexResults.put("countTrades", benchmarkCountTrades(symbol, timeWindowMinutes));
        compositeIndexResults.put("getMinMaxPrice", benchmarkMinMaxPrice(symbol, timeWindowMinutes));
        compositeIndexResults.put("topSymbolsByVolume", benchmarkTopSymbolsByVolume(timeWindowMinutes, topSymbolsCount));
        compositeIndexResults.put("insertBatchTrades", benchmarkInsertBatchTrades(batchSize, symbol));
        compositeIndexResults.put("getTradesBySymbolOnly", benchmarkSymbolOnlyQuery(symbol, lastTradesCount));
        
        results.put("tradeCount", tradeCount);
        results.put("singleColumnIndex", singleColumnResults);
        results.put("compositeIndex", compositeIndexResults);
        
        // Write results to CSV
        try {
            logBenchmarkResultsToCSV(results);
        } catch (IOException e) {
            logger.error("Failed to write benchmark results to CSV", e);
        }
        
        return results;
    }
    
    /**
     * Benchmark the getLastTrades method.
     */
    private long benchmarkLastTrades(String symbol, int count) {
        long startTime = System.nanoTime();
        
        getLastTrades(symbol, count);
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
    
    /**
     * Benchmark the getAveragePrice method.
     */
    private long benchmarkAveragePrice(String symbol, int minutes) {
        long startTime = System.nanoTime();
        
        getAveragePrice(symbol, minutes);
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
    
    /**
     * Benchmark the countTradesInTimeFrame method.
     */
    private long benchmarkCountTrades(String symbol, int minutes) {
        long startTime = System.nanoTime();
        
        countTradesInTimeFrame(symbol, minutes);
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
    
    /**
     * Benchmark the getMinMaxPrice method.
     */
    private long benchmarkMinMaxPrice(String symbol, int minutes) {
        long startTime = System.nanoTime();
        
        getMinMaxPrice(symbol, minutes);
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
    
    /**
     * Benchmark the getTopSymbolsByVolume method.
     */
    private long benchmarkTopSymbolsByVolume(int minutes, int limit) {
        long startTime = System.nanoTime();
        
        getTopSymbolsByVolume(minutes, limit);
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
    
    /**
     * Benchmark the insertBatchTrades method.
     */
    private long benchmarkInsertBatchTrades(int count, String symbol) {
        long startTime = System.nanoTime();
        
        insertBatchTrades(count, symbol);
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
    
    /**
     * Benchmark the getTradesBySymbolOnly method.
     */
    private long benchmarkSymbolOnlyQuery(String symbol, int count) {
        long startTime = System.nanoTime();
        
        getTradesBySymbolOnly(symbol, count);
        
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }
    
    /**
     * Log benchmark results to a CSV file.
     */
    private void logBenchmarkResultsToCSV(Map<String, Object> results) throws IOException {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String fileName = "benchmark-reports/db_benchmark_" + timestamp + ".csv";
        
        // Ensure directory exists
        new java.io.File("benchmark-reports").mkdirs();
        
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Benchmark,SingleColumnIndex,CompositeIndex\n");
            
            @SuppressWarnings("unchecked")
            Map<String, Long> singleColumnResults = (Map<String, Long>) results.get("singleColumnIndex");
            
            @SuppressWarnings("unchecked")
            Map<String, Long> compositeIndexResults = (Map<String, Long>) results.get("compositeIndex");
            
            // Ensure all metrics are included in the CSV
            String[] operations = {
                "getLastTrades", 
                "getAveragePrice", 
                "countTrades", 
                "getMinMaxPrice", 
                "topSymbolsByVolume", 
                "insertBatchTrades",
                "getTradesBySymbolOnly"
            };
            
            for (String operation : operations) {
                if (singleColumnResults.containsKey(operation) && compositeIndexResults.containsKey(operation)) {
                    writer.write(String.format("%s,%d,%d\n", 
                        operation, 
                        singleColumnResults.get(operation),
                        compositeIndexResults.get(operation)));
                }
            }
        }
        
        logger.info("Benchmark results written to {}", fileName);
    }
    
    /**
     * Get trades for a symbol only, without any timestamp sorting or filtering.
     * This method is designed to test pure symbol-only query performance.
     */
    public List<TradeEntity> getTradesBySymbolOnly(String symbol, int count) {
        long startTime = System.nanoTime();
        
        List<TradeEntity> trades = tradeRepository.findBySymbolOnly(
            symbol, PageRequest.of(0, count)
        );
        
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1_000_000;
        
        logger.debug("Retrieved {} trades for {} by symbol-only query in {} ms", 
                    trades.size(), symbol, executionTime);
        
        return trades;
    }
}