module com.robotvacuum {

    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    
    exports com.robotvacuum;
    exports com.robotvacuum.config;
    exports com.robotvacuum.controller;
    exports com.robotvacuum.model;
    exports com.robotvacuum.model.enums;
    exports com.robotvacuum.service;
    exports com.robotvacuum.view;
    
}