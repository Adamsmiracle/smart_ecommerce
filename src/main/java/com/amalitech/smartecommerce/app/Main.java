package com.amalitech.smartecommerce.app;

import com.amalitech.smartecommerce.utils.DBInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Ensure schema exists (idempotent; runs only when tables are missing)
        DBInitializer.ensureSchemaCreated();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/amalitech/smartecommerce/login-view.fxml"));
        Parent loginView = fxmlLoader.load();
        ScrollPane scrollPane = new ScrollPane(loginView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        // Add a dedicated style class so we can target the application viewport in CSS
        scrollPane.getStyleClass().add("app-viewport");
        // Prefer vertical scrolling; avoid horizontal overflow causing layout issues
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        Scene scene = new Scene(scrollPane, 500, 650); // Changed loginView to scrollPane

        // Load stylesheet
        String css = Main.class.getResource("/com/amalitech/smartecommerce/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Smart E-Commerce - Login");
        stage.setScene(scene);
        stage.setMinWidth(450);
        stage.setMinHeight(400);
        stage.setResizable(true);
        stage.show();
    }
}
