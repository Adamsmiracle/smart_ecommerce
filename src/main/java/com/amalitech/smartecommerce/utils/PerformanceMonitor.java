package com.amalitech.smartecommerce.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance measurement utility for tracking and comparing query execution times.
 */
public class PerformanceMonitor {
    private static PerformanceMonitor instance;

    private final List<PerformanceRecord> records;
    private long totalDbTime = 0;
    private long totalCacheTime = 0;
    private int dbOperations = 0;
    private int cacheOperations = 0;

    private PerformanceMonitor() {
        this.records = new ArrayList<>();
    }

    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }

    /**
     * Record a database operation timing.
     */
    public void recordDbOperation(String operation, long durationMs) {
        records.add(new PerformanceRecord(operation, "DATABASE", durationMs));
        totalDbTime += durationMs;
        dbOperations++;
    }

    /**
     * Record a cache operation timing.
     */
    public void recordCacheOperation(String operation, long durationMs) {
        records.add(new PerformanceRecord(operation, "CACHE", durationMs));
        totalCacheTime += durationMs;
        cacheOperations++;
    }

    /**
     * Measure execution time of a database operation.
     */
    public <T> T measureDbOperation(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        T result = supplier.get();
        long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms
        recordDbOperation(operation, duration);
        return result;
    }

    /**
     * Measure execution time of a cache operation.
     */
    public <T> T measureCacheOperation(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        T result = supplier.get();
        long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms
        recordCacheOperation(operation, duration);
        return result;
    }

    public double getAverageDbTime() {
        return dbOperations > 0 ? (double) totalDbTime / dbOperations : 0;
    }

    public double getAverageCacheTime() {
        return cacheOperations > 0 ? (double) totalCacheTime / cacheOperations : 0;
    }

    public double getSpeedupFactor() {
        double avgDb = getAverageDbTime();
        double avgCache = getAverageCacheTime();
        return avgCache > 0 ? avgDb / avgCache : 0;
    }

    public long getTotalDbTime() { return totalDbTime; }
    public long getTotalCacheTime() { return totalCacheTime; }
    public int getDbOperations() { return dbOperations; }
    public int getCacheOperations() { return cacheOperations; }

    public List<PerformanceRecord> getRecords() {
        return new ArrayList<>(records);
    }

    public List<PerformanceRecord> getRecentRecords(int count) {
        int start = Math.max(0, records.size() - count);
        return new ArrayList<>(records.subList(start, records.size()));
    }

    public void reset() {
        records.clear();
        totalDbTime = 0;
        totalCacheTime = 0;
        dbOperations = 0;
        cacheOperations = 0;
    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFORMANCE REPORT ===\n\n");

        sb.append("DATABASE OPERATIONS:\n");
        sb.append(String.format("  Total Operations: %d\n", dbOperations));
        sb.append(String.format("  Total Time: %d ms\n", totalDbTime));
        sb.append(String.format("  Average Time: %.2f ms\n\n", getAverageDbTime()));

        sb.append("CACHE OPERATIONS:\n");
        sb.append(String.format("  Total Operations: %d\n", cacheOperations));
        sb.append(String.format("  Total Time: %d ms\n", totalCacheTime));
        sb.append(String.format("  Average Time: %.2f ms\n\n", getAverageCacheTime()));

        sb.append("OPTIMIZATION RESULTS:\n");
        sb.append(String.format("  Speedup Factor: %.2fx faster with cache\n", getSpeedupFactor()));
        sb.append(String.format("  Time Saved: %d ms\n", totalDbTime - totalCacheTime));

        return sb.toString();
    }

    public static class PerformanceRecord {
        private final String operation;
        private final String type;
        private final long durationMs;
        private final long timestamp;

        public PerformanceRecord(String operation, String type, long durationMs) {
            this.operation = operation;
            this.type = type;
            this.durationMs = durationMs;
            this.timestamp = System.currentTimeMillis();
        }

        public String getOperation() { return operation; }
        public String getType() { return type; }
        public long getDurationMs() { return durationMs; }
        public long getTimestamp() { return timestamp; }
    }
}

