package com.pga.jasdaq.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.pga.jasdaq.matchingengine.IMatchingEngine;
import com.pga.jasdaq.matchingengine.MatchingEngine;
import com.pga.jasdaq.orderbook.Book;
import com.pga.jasdaq.orderbook.IBook;
import com.pga.jasdaq.orderbook.Order;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(value = 1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
public class JMHMatchingBenchmark {
    
    private IMatchingEngine matchingEngine;
    private Random random;
    private int nextOrderId;
    
    @Setup
    public void setup() {
        IBook orderBook = new Book();
        this.matchingEngine = new MatchingEngine(orderBook);
        this.random = new Random();
        this.nextOrderId = 1;
        
        // Prepopulate order book
        for (int i = 0; i < 1000; i++) {
            matchingEngine.placeLimitOrder(new Order(
                nextOrderId++, true, 100, 90 + random.nextInt(10)));
            matchingEngine.placeLimitOrder(new Order(
                nextOrderId++, false, 100, 100 + random.nextInt(10)));
        }
    }
    
    @Benchmark
    public Object placeLimitOrderNoMatch() {
        return matchingEngine.placeLimitOrder(
            new Order(nextOrderId++, true, 100, 80 + random.nextInt(5)));
    }
    
    @Benchmark
    public Object placeLimitOrderWithMatch() {
        return matchingEngine.placeLimitOrder(
            new Order(nextOrderId++, true, 100, 105 + random.nextInt(5)));
    }
    
    @Benchmark
    public Object placeMarketOrder() {
        return matchingEngine.placeMarketOrder(
            new Order(nextOrderId++, random.nextBoolean(), 100, null));
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(JMHMatchingBenchmark.class.getSimpleName())
            .build();
        new Runner(opt).run();
    }
}