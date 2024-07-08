module com.example.organizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.organizer to javafx.fxml;
    exports com.example.organizer;
    exports com.example.organizer.controller;
    opens com.example.organizer.controller to javafx.fxml;
    exports com.example.organizer.entity;
    opens com.example.organizer.entity to javafx.fxml;
}