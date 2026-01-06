package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.service.ProductService;
import com.amalitech.smartecommerce.service.ProductServiceImpl;
import com.amalitech.smartecommerce.utils.PerformanceMonitor;
import com.amalitech.smartecommerce.utils.PerformanceMonitor.PerformanceRecord;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for performance monitoring view.
 * Demonstrates caching, indexing concepts, and performance measurement.
 */
public class PerformanceController implements Initializable {

    // Database performance labels
    @FXML private Label lblDbOperations;
    @FXML private Label lblDbAvgTime;
    @FXML private Label lblDbTotalTime;

    // Cache performance labels
    @FXML private Label lblCacheOperations;
    @FXML private Label lblCacheAvgTime;
    @FXML private Label lblCacheHitRate;

    // Optimization result
    @FXML private Label lblSpeedupFactor;

    // Charts
    @FXML private BarChart<String, Number> chartComparison;
    @FXML private PieChart chartCacheRatio;

    // Operations log table
    @FXML private TableView<PerformanceRecord> tblOperations;
    @FXML private TableColumn<PerformanceRecord, String> colTimestamp;
    @FXML private TableColumn<PerformanceRecord, String> colOperation;
    @FXML private TableColumn<PerformanceRecord, String> colType;
    @FXML private TableColumn<PerformanceRecord, Long> colDuration;

    // Report text area
    @FXML private TextArea txtReport;

    private final PerformanceMonitor perfMonitor = PerformanceMonitor.getInstance();
    private final ProductCache productCache = ProductCache.getInstance();
    private final ProductService productService = new ProductServiceImpl();

    private ObservableList<PerformanceRecord> recordList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        refreshStats();
    }

    private void setupTable() {
        tblOperations.setItems(recordList);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        colTimestamp.setCellValueFactory(cellData ->
            new SimpleStringProperty(sdf.format(new Date(cellData.getValue().getTimestamp()))));
        colOperation.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getOperation()));
        colType.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getType()));
        colDuration.setCellValueFactory(cellData ->
            new SimpleLongProperty(cellData.getValue().getDurationMs()).asObject());
    }

    private void refreshStats() {
        // Update database stats
        lblDbOperations.setText(String.valueOf(perfMonitor.getDbOperations()));
        lblDbAvgTime.setText(String.format("%.2f ms", perfMonitor.getAverageDbTime()));
        lblDbTotalTime.setText(perfMonitor.getTotalDbTime() + " ms");

        // Update cache stats
        lblCacheOperations.setText(String.valueOf(perfMonitor.getCacheOperations()));
        lblCacheAvgTime.setText(String.format("%.2f ms", perfMonitor.getAverageCacheTime()));
        lblCacheHitRate.setText(String.format("%.1f%%", productCache.getHitRate()));

        // Update speedup factor
        double speedup = perfMonitor.getSpeedupFactor();
        lblSpeedupFactor.setText(String.format("%.1fx", speedup > 0 ? speedup : 1.0));

        // Update charts
        updateCharts();

        // Update operations log
        recordList.setAll(perfMonitor.getRecentRecords(50));

        // Update report
        txtReport.setText(perfMonitor.generateReport());
    }

    private void updateCharts() {
        // Bar chart: DB vs Cache comparison
        chartComparison.getData().clear();

        XYChart.Series<String, Number> dbSeries = new XYChart.Series<>();
        dbSeries.setName("Database");
        dbSeries.getData().add(new XYChart.Data<>("Avg Time", perfMonitor.getAverageDbTime()));
        dbSeries.getData().add(new XYChart.Data<>("Total Time", perfMonitor.getTotalDbTime()));

        XYChart.Series<String, Number> cacheSeries = new XYChart.Series<>();
        cacheSeries.setName("Cache");
        cacheSeries.getData().add(new XYChart.Data<>("Avg Time", perfMonitor.getAverageCacheTime()));
        cacheSeries.getData().add(new XYChart.Data<>("Total Time", perfMonitor.getTotalCacheTime()));

        chartComparison.getData().addAll(dbSeries, cacheSeries);

        // Pie chart: Cache hit/miss ratio
        chartCacheRatio.getData().clear();
        long hits = productCache.getCacheHits();
        long misses = productCache.getCacheMisses();

        if (hits > 0 || misses > 0) {
            chartCacheRatio.getData().add(new PieChart.Data("Hits (" + hits + ")", hits));
            chartCacheRatio.getData().add(new PieChart.Data("Misses (" + misses + ")", misses));
        } else {
            chartCacheRatio.getData().add(new PieChart.Data("No Data", 1));
        }
    }

    @FXML
    public void runBenchmark() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Run Benchmark");
        confirm.setHeaderText("Performance Benchmark");
        confirm.setContentText("This will run multiple database and cache operations to measure performance. Continue?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                performBenchmark();
            }
        });
    }

    private void performBenchmark() {
        txtReport.setText("Running benchmark...\n");

        int iterations = 10;

        // Benchmark database operations
        txtReport.appendText("\n=== DATABASE BENCHMARK ===\n");
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            List<Product> products = productService.getAllProducts();
            long duration = (System.nanoTime() - start) / 1_000_000;
            perfMonitor.recordDbOperation("Benchmark: Get All Products", duration);
            txtReport.appendText(String.format("DB Iteration %d: %d ms (%d products)\n", i + 1, duration, products.size()));
        }

        // Benchmark cache operations
        txtReport.appendText("\n=== CACHE BENCHMARK ===\n");
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            List<Product> products = productCache.getAll();
            long duration = (System.nanoTime() - start) / 1_000_000;
            perfMonitor.recordCacheOperation("Benchmark: Get All Products (Cache)", duration);
            txtReport.appendText(String.format("Cache Iteration %d: %d ms (%d products)\n", i + 1, duration, products.size()));
        }

        // Benchmark search operations
        txtReport.appendText("\n=== SEARCH BENCHMARK ===\n");
        String[] searchTerms = {"phone", "book", "laptop", "test", "product"};

        for (String term : searchTerms) {
            // Database search
            long dbStart = System.nanoTime();
            List<Product> dbResults = productService.searchProductsByName(term);
            long dbDuration = (System.nanoTime() - dbStart) / 1_000_000;
            perfMonitor.recordDbOperation("Benchmark: Search '" + term + "'", dbDuration);

            // Cache search
            long cacheStart = System.nanoTime();
            List<Product> cacheResults = productCache.searchByName(term);
            long cacheDuration = (System.nanoTime() - cacheStart) / 1_000_000;
            perfMonitor.recordCacheOperation("Benchmark: Search '" + term + "' (Cache)", cacheDuration);

            txtReport.appendText(String.format("Search '%s': DB=%dms (%d results), Cache=%dms (%d results)\n",
                term, dbDuration, dbResults.size(), cacheDuration, cacheResults.size()));
        }

        // Benchmark sorting
        txtReport.appendText("\n=== SORTING BENCHMARK ===\n");

        // Cache sorting (using QuickSort)
        long sortStart = System.nanoTime();
        List<Product> sorted = productCache.getAllSortedByName(true);
        long sortDuration = (System.nanoTime() - sortStart) / 1_000_000;
        perfMonitor.recordCacheOperation("Benchmark: QuickSort Products", sortDuration);
        txtReport.appendText(String.format("QuickSort (Cache): %d ms (%d products)\n", sortDuration, sorted.size()));

        // Refresh stats after benchmark
        refreshStats();

        txtReport.appendText("\n" + perfMonitor.generateReport());
    }

    @FXML
    public void resetStats() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Statistics");
        confirm.setHeaderText("Reset Performance Statistics");
        confirm.setContentText("This will clear all performance data. Continue?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                perfMonitor.reset();
                productCache.resetStats();
                refreshStats();
                txtReport.setText("Statistics reset successfully.");
            }
        });
    }

    @FXML
    public void exportReport() {
        String report = generateFullReport();

        TextArea textArea = new TextArea(report);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(600);
        textArea.setPrefHeight(500);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Performance Report");
        alert.setHeaderText("Full Performance Analysis Report");
        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private String generateFullReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║         SMART E-COMMERCE PERFORMANCE ANALYSIS REPORT         ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");

        sb.append("Generated: ").append(new Date()).append("\n\n");

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("1. EXECUTIVE SUMMARY\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        double speedup = perfMonitor.getSpeedupFactor();
        sb.append(String.format("• Cache provides %.1fx speedup over direct database queries\n", speedup > 0 ? speedup : 1.0));
        sb.append(String.format("• Cache hit rate: %.1f%%\n", productCache.getHitRate()));
        sb.append(String.format("• Total time saved: %d ms\n\n",
            perfMonitor.getTotalDbTime() - perfMonitor.getTotalCacheTime()));

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("2. DATABASE PERFORMANCE\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append(String.format("• Total Operations: %d\n", perfMonitor.getDbOperations()));
        sb.append(String.format("• Total Time: %d ms\n", perfMonitor.getTotalDbTime()));
        sb.append(String.format("• Average Time: %.2f ms per operation\n\n", perfMonitor.getAverageDbTime()));

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("3. CACHE PERFORMANCE\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append(String.format("• Total Operations: %d\n", perfMonitor.getCacheOperations()));
        sb.append(String.format("• Total Time: %d ms\n", perfMonitor.getTotalCacheTime()));
        sb.append(String.format("• Average Time: %.2f ms per operation\n", perfMonitor.getAverageCacheTime()));
        sb.append(String.format("• Cache Hits: %d\n", productCache.getCacheHits()));
        sb.append(String.format("• Cache Misses: %d\n", productCache.getCacheMisses()));
        sb.append(String.format("• Hit Rate: %.1f%%\n", productCache.getHitRate()));
        sb.append(String.format("• Cached Items: %d\n\n", productCache.getSize()));

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("4. OPTIMIZATION TECHNIQUES USED\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        sb.append("• In-Memory Caching:\n");
        sb.append("  - HashMap for O(1) primary key lookups (mirrors DB primary index)\n");
        sb.append("  - Secondary index maps for category-based lookups\n");
        sb.append("  - Token-based index for name search optimization\n\n");

        sb.append("• Sorting Algorithms:\n");
        sb.append("  - QuickSort implementation for product name sorting\n");
        sb.append("  - Average complexity: O(n log n)\n\n");

        sb.append("• Search Algorithms:\n");
        sb.append("  - Hash-based token lookup for fast partial matching\n");
        sb.append("  - Binary search available for exact name matching\n\n");

        sb.append("• Database Indexing:\n");
        sb.append("  - Primary key indexes on all tables\n");
        sb.append("  - Foreign key indexes for join optimization\n");
        sb.append("  - Additional indexes on frequently queried columns\n\n");

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("5. RECOMMENDATIONS\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        if (productCache.getHitRate() < 50) {
            sb.append("⚠ Low cache hit rate - consider:\n");
            sb.append("  - Increasing cache refresh frequency\n");
            sb.append("  - Pre-loading commonly accessed data\n\n");
        } else {
            sb.append("✓ Good cache hit rate - caching strategy is effective\n\n");
        }

        if (perfMonitor.getAverageDbTime() > 100) {
            sb.append("⚠ High average DB query time - consider:\n");
            sb.append("  - Adding additional database indexes\n");
            sb.append("  - Optimizing query structure\n");
            sb.append("  - Increasing connection pool size\n\n");
        } else {
            sb.append("✓ Database query times are acceptable\n\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("                        END OF REPORT\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        return sb.toString();
    }
}

