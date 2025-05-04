package com.pga.jasdaq.db.controller;

import com.pga.jasdaq.db.entity.TradeEntity;
import com.pga.jasdaq.db.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades/db")
public class TradeController {

    private final TradeService tradeService;

    @Autowired
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/last/{symbol}")
    public ResponseEntity<List<TradeEntity>> getLastTrades(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(tradeService.getLastTrades(symbol, count));
    }

    @GetMapping("/average/{symbol}")
    public ResponseEntity<BigDecimal> getAveragePrice(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "60") int minutes) {
        return ResponseEntity.ok(tradeService.getAveragePrice(symbol, minutes));
    }

    @GetMapping("/count/{symbol}")
    public ResponseEntity<Long> countTrades(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "60") int minutes) {
        return ResponseEntity.ok(tradeService.countTradesInTimeFrame(symbol, minutes));
    }

    @GetMapping("/minmax/{symbol}")
    public ResponseEntity<Map<String, BigDecimal>> getMinMaxPrice(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "60") int minutes) {
        return ResponseEntity.ok(tradeService.getMinMaxPrice(symbol, minutes));
    }

    @GetMapping("/top-volume")
    public ResponseEntity<List<Map<String, Object>>> getTopSymbolsByVolume(
            @RequestParam(defaultValue = "60") int minutes,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(tradeService.getTopSymbolsByVolume(minutes, limit));
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateTrades(
            @RequestParam(defaultValue = "100000") int count) {
        return ResponseEntity.ok(tradeService.generateBulkTrades(count));
    }

    @PostMapping("/batch-insert")
    public ResponseEntity<Map<String, Object>> insertBatchTrades(
            @RequestParam(defaultValue = "5000") int count,
            @RequestParam(defaultValue = "TSLA") String symbol) {
        return ResponseEntity.ok(tradeService.insertBatchTrades(count, symbol));
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetTable() {
        tradeService.resetTable();
        return ResponseEntity.ok("Trades table reset successfully");
    }

    @PostMapping("/index/single")
    public ResponseEntity<String> applySingleColumnIndex() {
        tradeService.applySingleColumnIndex();
        return ResponseEntity.ok("Single-column index applied successfully");
    }

    @PostMapping("/index/composite")
    public ResponseEntity<String> applyCompositeIndex() {
        tradeService.applyCompositeIndex();
        return ResponseEntity.ok("Composite index applied successfully");
    }

    @DeleteMapping("/index")
    public ResponseEntity<String> dropAllIndexes() {
        tradeService.dropAllIndexes();
        return ResponseEntity.ok("All indexes dropped successfully");
    }

    @PostMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> runBenchmark(
            @RequestParam(defaultValue = "100000") int tradeCount,
            @RequestParam(defaultValue = "TSLA") String symbol,
            @RequestParam(defaultValue = "100") int lastTradesCount,
            @RequestParam(defaultValue = "60") int timeWindowMinutes,
            @RequestParam(defaultValue = "5000") int batchSize,
            @RequestParam(defaultValue = "5") int topSymbolsCount) {
        return ResponseEntity.ok(tradeService.runFullBenchmark(
            tradeCount, symbol, lastTradesCount, timeWindowMinutes, batchSize, topSymbolsCount));
    }

    @GetMapping("/symbol-only/{symbol}")
    public ResponseEntity<List<TradeEntity>> getTradesBySymbolOnly(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "100") int count) {
        return ResponseEntity.ok(tradeService.getTradesBySymbolOnly(symbol, count));
    }
}