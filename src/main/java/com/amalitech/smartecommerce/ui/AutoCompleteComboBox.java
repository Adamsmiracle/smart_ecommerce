package com.amalitech.smartecommerce.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;

import java.util.function.Predicate;

/**
 * Small reusable autocomplete helper for ComboBox.
 * - Uses a FilteredList to avoid mutating the original source list.
 * - Works with editable ComboBox and filters as the user types.
 * - Keeps selected item intact when choosing from the drop-down.
 */
public class AutoCompleteComboBox {

    /**
     * Bind an editable ComboBox to a sourceItems list and provide a predicate to match items.
     * The ComboBox items will be a FilteredList view backed by the original list.
     */
    public static <T> void makeAutoComplete(ComboBox<T> comboBox, ObservableList<T> sourceItems, java.util.function.Function<T, String> toStringFn) {
        if (comboBox == null || sourceItems == null) return;

        FilteredList<T> filtered = new FilteredList<>(sourceItems, s -> true);
        comboBox.setItems(filtered);
        comboBox.setEditable(true);

        // Visual rendering: show a friendly string
        comboBox.setCellFactory(lv -> new ListCell<>() { @Override protected void updateItem(T item, boolean empty) { super.updateItem(item, empty); setText(empty || item==null ? null : toStringFn.apply(item)); }});
        comboBox.setButtonCell(new ListCell<>() { @Override protected void updateItem(T item, boolean empty) { super.updateItem(item, empty); setText(empty || item==null ? null : toStringFn.apply(item)); }});

        TextField editor = comboBox.getEditor();

        // Keep track of the last successful selection
        comboBox.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                // Update editor text to exact name
                Platform.runLater(() -> editor.setText(toStringFn.apply(newV)));
            }
        });

        editor.textProperty().addListener((obs, oldText, newText) -> {
            final String lower = newText == null ? "" : newText.toLowerCase();
            filtered.setPredicate(item -> {
                if (lower.isEmpty()) return true;
                String candidate = toStringFn.apply(item);
                return candidate != null && candidate.toLowerCase().contains(lower);
            });
            if (!comboBox.isShowing()) comboBox.show();
        });
    }
}

