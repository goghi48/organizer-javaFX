package com.example.organizer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateNoteController {
    @FXML
    private Text dateLabel;
    @FXML
    private TextField noteField;
    @FXML
    private VBox notesContainer;
    private LocalDate selectedDate;
    private Controller mainController;
    private int userId;

    private static final Map<LocalDate, List<String>> notesMap = new HashMap<>();

    public void setDate(LocalDate date) {
        this.selectedDate = date;
        dateLabel.setText(date.toString());
        loadNotes();
    }

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        loadUserNotes();
    }

    public void setNotesForDate(List<String> notes) {
        notesMap.put(selectedDate, notes);
        loadNotes();
    }

    @FXML
    protected void onAddNoteButtonClick() throws SQLException {
        String note = noteField.getText();
        if (!note.isEmpty()) {
            notesMap.computeIfAbsent(selectedDate, k -> new ArrayList<>()).add(note);
            noteField.clear();
            loadNotes();
            mainController.notesCache.putIfAbsent(selectedDate, new ArrayList<>());
            mainController.notesCache.get(selectedDate).add(note);
            mainController.updateStatus();
            DatabaseManager.addNoteForUser(userId, selectedDate, note);
        }
    }

    @FXML
    protected void onDeleteNoteButtonClick(ActionEvent event) throws SQLException {
        String note = ((Button) event.getSource()).getText();
        notesMap.getOrDefault(selectedDate, new ArrayList<>()).remove(note);
        loadNotes();
        mainController.notesCache.getOrDefault(selectedDate, new ArrayList<>()).remove(note);
        if (mainController.notesCache.get(selectedDate).isEmpty()) {
            mainController.notesCache.remove(selectedDate);
        }
        mainController.updateStatus();
        DatabaseManager.deleteNoteForUser(userId, selectedDate, note);
    }

    private void loadNotes() {
        notesContainer.getChildren().clear();
        List<String> notes = notesMap.getOrDefault(selectedDate, new ArrayList<>());
        for (String note : notes) {
            HBox noteBox = new HBox(10);
            Text noteText = new Text(note);
            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(e -> {
                notes.remove(note);
                loadNotes();
                mainController.notesCache.getOrDefault(selectedDate, new ArrayList<>()).remove(note);
                if (mainController.notesCache.get(selectedDate).isEmpty()) {
                    mainController.notesCache.remove(selectedDate);
                }
                mainController.updateStatus();
                try {
                    DatabaseManager.deleteNoteForUser(userId, selectedDate, note);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });
            noteBox.getChildren().addAll(noteText, deleteButton);
            notesContainer.getChildren().add(noteBox);
        }
    }

    private void loadUserNotes() {
        try {
            notesMap.putAll(DatabaseManager.getNotesForUser(userId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}