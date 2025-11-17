module FoundationCode {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    
    requires org.junit.jupiter.api;
    requires org.junit.platform.commons;
	requires javafx.swing;
    
    exports application;
    
    // Open to both JavaFX and JUnit
    opens application to javafx.fxml, org.junit.platform.commons, org.junit.jupiter.api;

    exports databasePart1;
    exports logic;
    exports model;
    exports pages;
}



