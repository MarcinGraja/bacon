module bacon{
    requires javafx.fxml;
    requires javafx.controls;
    requires org.jgrapht.core;
    requires okhttp3;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    opens bacon.model;
    opens bacon.controller;
    opens bacon.view;
    opens bacon;
}