package com.example.organizer.controller;

import com.example.organizer.entity.Note;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    @FXML
    private Label monthLabel;
    @FXML
    private GridPane calendarGrid;
    @FXML
    private Label currentDateTimeLabel;
    @FXML
    private ComboBox<String> periodComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea notesTextArea;
    @FXML
    private Button showNotesButton;

    private YearMonth currentYearMonth;
    private Image noteImage;
    private int userId;
    Map<LocalDate, List<String>> notesCache = new HashMap<>();


    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        noteImage = new Image(getClass().getResourceAsStream("/com/example/organizer/images/hasNote.png"));
        loadAllNotes();
        updateCalendar();
        startDateTimeUpdate();
        periodComboBox.setOnAction(event -> onPeriodComboBoxAction());
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                endDatePicker.setValue(newValue);
                endDatePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(date.isBefore(newValue));
                    }
                });
            }
        });
    }

    private void loadAllNotes() {
        try {
            notesCache = DatabaseManager.getNotesForUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startDateTimeUpdate() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            currentDateTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    protected void onPrevMonthButtonClick() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    protected void onNextMonthButtonClick() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    public void updateStatus() {
        for (Node node : calendarGrid.getChildren()) {
            StackPane stackPane = (StackPane) node;
            Button dateButton = (Button) stackPane.getChildren().get(0);

            LocalDate date = currentYearMonth.atDay(Integer.parseInt(dateButton.getText()));
            boolean hasNotes = notesCache.containsKey(date);

            if (hasNotes) {
                boolean hasImage = false;
                for (Node child : stackPane.getChildren()) {
                    if (child instanceof ImageView) {
                        hasImage = true;
                        break;
                    }
                }
                if (!hasImage) {
                    ImageView imageView = new ImageView(noteImage);
                    imageView.setFitHeight(10);
                    imageView.setFitWidth(10);
                    imageView.setOpacity(0.5);
                    stackPane.getChildren().add(imageView);
                    StackPane.setAlignment(imageView, Pos.BOTTOM_RIGHT);
                }
            } else {
                stackPane.getChildren().removeIf(child -> child instanceof ImageView);
            }
        }
    }


    private void updateCalendar(YearMonth yearMonth) {
        monthLabel.setText(yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        calendarGrid.getChildren().clear();

        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        int daysInMonth = yearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            StackPane stackPane = new StackPane();
            Button dateButton = new Button(String.valueOf(day));
            dateButton.setOnAction(e -> openDateNoteView(date));

            stackPane.getChildren().add(dateButton);

            if (notesCache.containsKey(date)) {
                ImageView imageView = new ImageView(noteImage);
                imageView.setFitHeight(10);
                imageView.setFitWidth(10);
                imageView.setOpacity(0.5);
                stackPane.getChildren().add(imageView);
                StackPane.setAlignment(imageView, Pos.BOTTOM_RIGHT);
            }

            calendarGrid.add(stackPane, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }
    public void updateCalendar() {
        updateCalendar(currentYearMonth);
    }

    private void openDateNoteView(LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("date-note.fxml"));
            Parent root = loader.load();
            DateNoteController controller = loader.getController();
            controller.setDate(date);
            controller.setMainController(this);
            controller.setUserId(userId);

            List<String> notesForDate = notesCache.getOrDefault(date, new ArrayList<>());
            controller.setNotesForDate(notesForDate);

            Stage stage = new Stage();
            stage.setTitle("Notes for " + date.toString());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onPeriodComboBoxAction() {
        String selectedPeriod = periodComboBox.getValue();
        if ("За выбранный период".equals(selectedPeriod)) {
            startDatePicker.setVisible(true);
            endDatePicker.setVisible(true);
            showNotesButton.setVisible(true);
        } else {
            startDatePicker.setVisible(false);
            endDatePicker.setVisible(false);
            showNotesButton.setVisible(false);
            displayNotesForPeriod(selectedPeriod);
        }
    }


    @FXML
    public void onShowNotesButtonClick() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate != null && endDate != null) {
            displayNotesForPeriod("За выбранный период", startDate, endDate);
        }
    }



    private void displayNotesForPeriod(String period) {
        LocalDate startDate;
        LocalDate endDate;
        LocalDate now = LocalDate.now();
        switch (period) {
            case "Сегодня":
                startDate = now;
                endDate = now;
                break;
            case "За неделю":
                startDate = now.with(DayOfWeek.MONDAY);
                endDate = now.with(DayOfWeek.SUNDAY);
                break;
            case "За месяц":
                startDate = now.withDayOfMonth(1);
                endDate = now.withDayOfMonth(now.lengthOfMonth());
                break;
            default:
                return;
        }
        displayNotesForPeriod("За выбранный период", startDate, endDate);
    }


    private void displayNotesForPeriod(String period, LocalDate startDate, LocalDate endDate) {
        try {
            List<Note> notes = DatabaseManager.getNotesForPeriod(userId, startDate, endDate);
            StringBuilder notesText = new StringBuilder();

            for (Note note : notes) {
                notesText.append(note.getDate()).append(": ").append(note.getText()).append("\n");
            }

            notesTextArea.setText(notesText.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setUserId(int userId) {
        this.userId = userId;
        loadAllNotes();
        updateCalendar();
    }
}