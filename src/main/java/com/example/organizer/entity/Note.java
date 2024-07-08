package com.example.organizer.entity;

import java.time.LocalDate;

public class Note {
    private LocalDate date;
    private String text;

    public Note(LocalDate date, String text) {
        this.date = date;
        this.text = text;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
