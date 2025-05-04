package com.pga.jasdaq.db.repository;

import com.pga.jasdaq.db.entity.TradeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    /**
     * Find the last N trades for a specific symbol, ordered by timestamp in descending order.
     *
     * @param symbol The stock symbol
     * @param pageable Pagination parameters
     * @return List of trades
     */
    List<TradeEntity> findBySymbolOrderByTimestampDesc(String symbol, Pageable pageable);
    
    /**
     * Calculate the average price for a symbol within a specific time range.
     *
     * @param symbol The stock symbol
     * @param startTime The start time for the range
     * @param endTime The end time for the range
     * @return The average price
     */
    @Query("SELECT AVG(t.price) FROM TradeEntity t WHERE t.symbol = :symbol " +
           "AND t.timestamp BETWEEN :startTime AND :endTime")
    BigDecimal calculateAveragePrice(
            @Param("symbol") String symbol,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    /**
     * Calculate the average price for a symbol in the last X minutes.
     *
     * @param symbol The stock symbol
     * @param cutoffTime The time X minutes ago
     * @return The average price
     */
    @Query("SELECT AVG(t.price) FROM TradeEntity t WHERE t.symbol = :symbol " +
           "AND t.timestamp >= :cutoffTime")
    BigDecimal calculateAveragePriceInTimeFrame(
            @Param("symbol") String symbol,
            @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Count the number of trades for a specific symbol.
     *
     * @param symbol The stock symbol
     * @return The count of trades
     */
    long countBySymbol(String symbol);
    
    /**
     * Count the number of trades for a symbol in the last X minutes.
     *
     * @param symbol The stock symbol
     * @param cutoffTime The time X minutes ago
     * @return The count of trades
     */
    @Query("SELECT COUNT(t) FROM TradeEntity t WHERE t.symbol = :symbol " +
           "AND t.timestamp >= :cutoffTime")
    long countTradesInTimeFrame(
            @Param("symbol") String symbol, 
            @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find the minimum and maximum price for a symbol in the last X minutes.
     *
     * @param symbol The stock symbol
     * @param cutoffTime The time X minutes ago
     * @return An object array containing [min, max]
     */
    @Query("SELECT MIN(t.price), MAX(t.price) FROM TradeEntity t WHERE t.symbol = :symbol " +
           "AND t.timestamp >= :cutoffTime")
    Object[] findMinMaxPriceInTimeFrame(
            @Param("symbol") String symbol,
            @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Query top N symbols by trade volume over the last Y minutes.
     *
     * @param cutoffTime The time Y minutes ago
     * @param pageable Pagination parameters for limiting to top N results
     * @return List of symbols and their total volume
     */
    @Query("SELECT t.symbol, SUM(t.volume) as totalVolume FROM TradeEntity t " +
           "WHERE t.timestamp >= :cutoffTime " +
           "GROUP BY t.symbol ORDER BY totalVolume DESC")
    List<Object[]> findTopSymbolsByVolumeInTimeFrame(
            @Param("cutoffTime") LocalDateTime cutoffTime,
            Pageable pageable);
    
    /**
     * Delete all trades for a specific symbol.
     *
     * @param symbol The stock symbol
     */
    void deleteBySymbol(String symbol);

    /**
     * Find trades by symbol only, without any sorting or additional filters.
     * This query is optimized for testing the performance of single-column index on symbol.
     *
     * @param symbol The stock symbol
     * @param pageable Pagination parameters
     * @return List of trades
     */
    @Query("SELECT t FROM TradeEntity t WHERE t.symbol = :symbol")
    List<TradeEntity> findBySymbolOnly(@Param("symbol") String symbol, Pageable pageable);
}