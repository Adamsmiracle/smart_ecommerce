package com.amalitech.smartecommerce.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Performance measurement utility for tracking and comparing query execution times.
 */
public class PerformanceMonitor {
    private static PerformanceMonitor instance;

    private final List<PerformanceRecord> records;
    // store internal totals in microseconds to preserve precision for very fast ops
    private long totalDbTimeMicros = 0;
    private long totalCacheTimeMicros = 0;
    private int dbOperations = 0;
    private int cacheOperations = 0;

    // Thread-local flag to indicate that the current thread is already measuring a cache operation
    // This prevents double-counting when callers use measureCacheOperation() around cache method calls.
    private final ThreadLocal<Boolean> inCacheMeasurement = ThreadLocal.withInitial(() -> false);

    private PerformanceMonitor() {
        this.records = new ArrayList<>();
    }

    public static PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }

    /**
     * Record a database operation timing (duration in microseconds).
     */
    public void recordDbOperation(String operation, long durationMicros) {
        records.add(new PerformanceRecord(operation, "DATABASE", durationMicros));
        totalDbTimeMicros += durationMicros;
        dbOperations++;
    }

    /**
     * Record a cache operation timing (duration in microseconds).
     */
    public void recordCacheOperation(String operation, long durationMicros) {
        records.add(new PerformanceRecord(operation, "CACHE", durationMicros));
        totalCacheTimeMicros += durationMicros;
        cacheOperations++;
    }

    /**
     * Measure execution time of a database operation and record in microseconds.
     */
    public <T> T measureDbOperation(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        T result = supplier.get();
        long durationMicros = (System.nanoTime() - start) / 1000; // microseconds
        recordDbOperation(operation, durationMicros);
        return result;
    }

    /**
     * Measure execution time of a cache operation and record in microseconds.
     */
    public <T> T measureCacheOperation(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        // mark that we are in an explicit cache measurement for this thread
        inCacheMeasurement.set(true);
        try {
            T result = supplier.get();
            long durationMicros = (System.nanoTime() - start) / 1000; // microseconds
            recordCacheOperation(operation, durationMicros);
            return result;
        } finally {
            inCacheMeasurement.set(false);
        }
    }

    /**
     * Helper for cache-accessing code to record a cache operation when it is not already
     * wrapped by an outer measureCacheOperation call. This avoids double-counting.
     * The duration will be recorded as 0 microseconds for internal notifications.
     */
    public void recordInternalCacheOperation(String operation) {
        if (inCacheMeasurement.get() != null && inCacheMeasurement.get()) {
            // already counted by outer measurement, skip internal recording
            return;
        }
        // record with zero duration to indicate internal access
        recordCacheOperation(operation, 0);
    }

    /**
     * Average DB time in milliseconds (can be fractional).
     */
    public double getAverageDbTime() {
        return dbOperations > 0 ? (double) totalDbTimeMicros / dbOperations / 1000.0 : 0;
    }

    /**
     * Average cache time in milliseconds (can be fractional).
     */
    public double getAverageCacheTime() {
        return cacheOperations > 0 ? (double) totalCacheTimeMicros / cacheOperations / 1000.0 : 0;
    }

    public double getSpeedupFactor() {
        double avgDb = getAverageDbTime();
        double avgCache = getAverageCacheTime();
        return avgCache > 0 ? avgDb / avgCache : 0;
    }

    // New helper: compute average duration for records matching an operation substring and type (returns ms)
    public double getAverageTimeForOperation(String operationContains, String type) {
        if (operationContains == null || type == null) return 0;
        long totalMicros = 0;
        int count = 0;
        for (PerformanceRecord r : records) {
            if (r.getType().equalsIgnoreCase(type) && r.getOperation().contains(operationContains)) {
                totalMicros += r.getDurationMicros();
                count++;
            }
        }
        return count > 0 ? (double) totalMicros / count / 1000.0 : 0;
    }

    // New helper: compute speedup (db/cache) for a specific operation substring
    public double getSpeedupFactorForOperation(String operationContains) {
        double avgDb = getAverageTimeForOperation(operationContains, "DATABASE");
        double avgCache = getAverageTimeForOperation(operationContains, "CACHE");
        return avgCache > 0 ? avgDb / avgCache : 0;
    }

    // Expose totals (in milliseconds) for compatibility
    public long getTotalDbTime() { return totalDbTimeMicros / 1000; }
    public long getTotalCacheTime() { return totalCacheTimeMicros / 1000; }
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
        totalDbTimeMicros = 0;
        totalCacheTimeMicros = 0;
        dbOperations = 0;
        cacheOperations = 0;
        inCacheMeasurement.remove();
    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFORMANCE REPORT ===\n\n");

        sb.append("DATABASE OPERATIONS:\n");
        sb.append(String.format("  Total Operations: %d\n", dbOperations));
        sb.append(String.format("  Total Time: %d ms\n", getTotalDbTime()));
        sb.append(String.format("  Average Time: %.2f ms\n\n", getAverageDbTime()));

        sb.append("CACHE OPERATIONS:\n");
        sb.append(String.format("  Total Operations: %d\n", cacheOperations));
        sb.append(String.format("  Total Time: %d ms\n", getTotalCacheTime()));
        sb.append(String.format("  Average Time: %.2f ms\n\n", getAverageCacheTime()));

        sb.append("OPTIMIZATION RESULTS:\n");
        sb.append(String.format("  Speedup Factor: %.2fx faster with cache\n", getSpeedupFactor()));
        sb.append(String.format("  Time Saved: %d ms\n", getTotalDbTime() - getTotalCacheTime()));

        return sb.toString();
    }

    public static class PerformanceRecord {
        private final String operation;
        private final String type;
        // duration in microseconds to maintain precision for very fast ops
        private final long durationMicros;
        private final long timestamp;

        public PerformanceRecord(String operation, String type, long durationMicros) {
            this.operation = operation;
            this.type = type;
            this.durationMicros = durationMicros;
            this.timestamp = System.currentTimeMillis();
        }

        public String getOperation() { return operation; }
        public String getType() { return type; }
        // keep existing method name but return milliseconds (rounded)
        public long getDurationMs() { return durationMicros / 1000; }
        // new accessor for raw microseconds
        public long getDurationMicros() { return durationMicros; }
        public long getTimestamp() { return timestamp; }
    }
}
