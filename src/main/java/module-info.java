module com.amalitech.smartecommerce {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.amalitech.smartecommerce to javafx.fxml;
    exports com.amalitech.smartecommerce.app;
    opens com.amalitech.smartecommerce.app to javafx.fxml;
    exports com.amalitech.smartecommerce.controller;
    opens com.amalitech.smartecommerce.controller to javafx.fxml;
}