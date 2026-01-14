package com.amalitech.smartecommerce.app;

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
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/amalitech/smartecommerce/login-view.fxml"));
        Parent loginView = fxmlLoader.load();
        ScrollPane scrollPane = new ScrollPane(loginView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
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




