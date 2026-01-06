package com.amalitech.smartecommerce.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/amalitech/smartecommerce/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 650);

        // Load stylesheet
        String css = Main.class.getResource("/com/amalitech/smartecommerce/styles.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Smart E-Commerce - Login");
        stage.setScene(scene);
        stage.setMinWidth(450);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.show();
    }
}
