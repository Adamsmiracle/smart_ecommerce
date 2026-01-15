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
    requires java.sql;
    requires java.dotenv;
    requires jbcrypt;
    requires jjwt.api;
    requires jakarta.validation;
    requires org.hibernate.validator;
    requires annotations;
//    requires com.amalitech.smartecommerce;

    opens com.amalitech.smartecommerce to javafx.fxml;
    exports com.amalitech.smartecommerce.app;
    opens com.amalitech.smartecommerce.app to javafx.fxml;
    exports com.amalitech.smartecommerce.controller;
    opens com.amalitech.smartecommerce.controller to javafx.fxml;
    opens com.amalitech.smartecommerce.model to javafx.base;
    exports com.amalitech.smartecommerce.model;
    exports com.amalitech.smartecommerce.service;
    exports com.amalitech.smartecommerce.dao;
    exports com.amalitech.smartecommerce.cache;
    exports com.amalitech.smartecommerce.utils;
    exports com.amalitech.smartecommerce.exception;
    exports com.amalitech.smartecommerce.dto;
    opens com.amalitech.smartecommerce.dto to org.hibernate.validator;
}
