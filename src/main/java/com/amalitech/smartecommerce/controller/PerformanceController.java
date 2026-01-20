package com.amalitech.smartecommerce.controller;

import com.amalitech.smartecommerce.cache.ProductCache;
import com.amalitech.smartecommerce.model.Product;
import com.amalitech.smartecommerce.service.ProductService;
import com.amalitech.smartecommerce.service.ProductServiceImpl;
import com.amalitech.smartecommerce.utils.PerformanceMonitor;
import com.amalitech.smartecommerce.utils.PerformanceMonitor.PerformanceRecord;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Duration;

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

    // Controls
    @FXML private ProgressIndicator progressIndicator;

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

    private final ObservableList<PerformanceRecord> recordList = FXCollections.observableArrayList();

    private Timeline refreshTimeline;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        refreshStats();
        if (progressIndicator != null) progressIndicator.setVisible(false);

        // Start a timeline to refresh stats periodically to allow real-time monitoring
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> refreshStats()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void setupTable() {
        if (tblOperations == null) return; // table removed from FXML

        tblOperations.setItems(recordList);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        if (colTimestamp != null) {
            colTimestamp.setCellValueFactory(cellData ->
                new SimpleStringProperty(sdf.format(new Date(cellData.getValue().getTimestamp()))));
        }
        if (colOperation != null) {
            colOperation.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOperation()));
        }
        if (colType != null) {
            colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType()));
        }
        if (colDuration != null) {
            colDuration.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getDurationMs()).asObject());
        }
    }

    private void refreshStats() {
        // Update database stats safely
        if (lblDbOperations != null) lblDbOperations.setText(String.valueOf(perfMonitor.getDbOperations()));
        if (lblDbAvgTime != null) lblDbAvgTime.setText(String.format("%.2f ms", perfMonitor.getAverageDbTime()));
        if (lblDbTotalTime != null) lblDbTotalTime.setText(perfMonitor.getTotalDbTime() + " ms");

        // Update cache stats
        if (lblCacheOperations != null) lblCacheOperations.setText(String.valueOf(perfMonitor.getCacheOperations()));
        if (lblCacheAvgTime != null) lblCacheAvgTime.setText(String.format("%.2f ms", perfMonitor.getAverageCacheTime()));
        if (lblCacheHitRate != null) lblCacheHitRate.setText(String.format("%.1f%%", productCache.getHitRate()));

        // Update charts if present (no-op when charts removed)
        updateCharts();

        // Update operations log
        if (recordList != null) recordList.setAll(perfMonitor.getRecentRecords(50));
        if (tblOperations != null) tblOperations.setItems(recordList);

        // Update report
        if (txtReport != null) {
            txtReport.setText(perfMonitor.generateReport());
        } else {
            // Fallback to console logging if report area is not present
            System.out.println(perfMonitor.generateReport());
        }
    }

    /**
     * No-op for charts when chart UI elements are removed from FXML.
     */
    private void updateCharts() {
        // Previously updated a PieChart showing cache hit/miss; chart removed by user.
        // Keep this method as a safe no-op to avoid FXML binding issues.
    }

    // Helper to append to report safely
    private void appendReport(String text) {
        if (txtReport != null) {
            Platform.runLater(() -> txtReport.appendText(text));
        } else {
            System.out.print(text);
        }
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
                if (txtReport != null) txtReport.setText("Statistics reset successfully.");
            }
        });
    }

    @FXML
    public void exportReport() {
        String report = generateFullReport();

        if (txtReport != null) {
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
        } else {
            // Fallback to console
            System.out.println(report);
        }
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
        sb.append("                        END OF REPORT\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        return sb.toString();
    }

    // Optional: call this when the controller is disposed to stop the timeline
    public void stopMonitoring() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }
}
