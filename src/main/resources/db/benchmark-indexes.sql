-- This file contains SQL statements for managing indexes in the benchmark tests

-- Drop all non-primary indexes from the trades table
DROP INDEX IF EXISTS idx_symbol ON trades;
DROP INDEX IF EXISTS idx_symbol_ts ON trades;
DROP INDEX IF EXISTS idx_timestamp ON trades;

-- Reset the entire table (delete all data)
-- TRUNCATE TABLE trades;

-- Create single-column index on symbol
-- CREATE INDEX idx_symbol ON trades(symbol);

-- Create composite index on symbol and timestamp
-- CREATE INDEX idx_symbol_ts ON trades(symbol, timestamp);

-- Create timestamp index (for operations across all symbols)
-- CREATE INDEX idx_timestamp ON trades(timestamp);

-- Analyze table to update statistics (important for MySQL query optimizer)
-- ANALYZE TABLE trades;

-- Benchmark query examples:
-- 1. Find recent trades for a symbol:
--    SELECT * FROM trades WHERE symbol = 'TSLA' ORDER BY timestamp DESC LIMIT 100;

-- 2. Calculate average price for a symbol in the last hour:
--    SELECT AVG(price) FROM trades WHERE symbol = 'TSLA' AND timestamp >= NOW() - INTERVAL 1 HOUR;

-- 3. Count trades by symbol:
--    SELECT symbol, COUNT(*) FROM trades GROUP BY symbol;

-- 4. Count trades for a symbol in the last X minutes:
--    SELECT COUNT(*) FROM trades WHERE symbol = 'TSLA' AND timestamp >= NOW() - INTERVAL 60 MINUTE;

-- 5. Find min/max price for a symbol in the last X minutes:
--    SELECT MIN(price), MAX(price) FROM trades WHERE symbol = 'TSLA' AND timestamp >= NOW() - INTERVAL 60 MINUTE;

-- 6. Query top N symbols by trade volume over the last Y minutes:
--    SELECT symbol, SUM(volume) as total_volume FROM trades 
--    WHERE timestamp >= NOW() - INTERVAL 60 MINUTE 
--    GROUP BY symbol ORDER BY total_volume DESC LIMIT 5;

-- 7. Batch insert example for write performance testing:
--    INSERT INTO trades (symbol, price, volume, timestamp, order_type) VALUES 
--    ('TSLA', 245.60, 100, NOW(), 'BUY'), 
--    ('TSLA', 245.65, 50, NOW(), 'SELL'),
--    ... (more rows)

-- 8. Symbol-only query (testing single-column index advantage):
--    SELECT * FROM trades WHERE symbol = 'TSLA' LIMIT 100;
--    Note: This query demonstrates when a single-column index on symbol might 
--    perform better than a composite index, as it has no timestamp filtering or sorting.

-- Check if indexes are being used (run with actual query):
-- EXPLAIN SELECT * FROM trades WHERE symbol = 'TSLA' ORDER BY timestamp DESC LIMIT 100;
-- EXPLAIN SELECT AVG(price) FROM trades WHERE symbol = 'TSLA' AND timestamp >= NOW() - INTERVAL 1 HOUR;
-- EXPLAIN SELECT COUNT(*) FROM trades WHERE symbol = 'TSLA' AND timestamp >= NOW() - INTERVAL 60 MINUTE;
-- EXPLAIN SELECT MIN(price), MAX(price) FROM trades WHERE symbol = 'TSLA' AND timestamp >= NOW() - INTERVAL 60 MINUTE;
-- EXPLAIN SELECT symbol, SUM(volume) as total_volume FROM trades WHERE timestamp >= NOW() - INTERVAL 60 MINUTE GROUP BY symbol ORDER BY total_volume DESC LIMIT 5;
-- EXPLAIN SELECT * FROM trades WHERE symbol = 'TSLA' LIMIT 100;