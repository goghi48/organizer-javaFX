package com.example.organizer.controller;

import com.example.organizer.entity.Note;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class DatabaseManager {
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static final String SETTINGS_FILE = "settings.txt";
    private static Connection connection;

    static {
        loadDatabaseSettings();
        try {
            if (getConnection() != null) {
                System.out.println("aboba");
            }
        } catch (SQLException e) {
            System.err.println("zxc");
            e.printStackTrace();
        }
    }

    private static void loadDatabaseSettings() {
        Properties properties = new Properties();
        File file = new File(SETTINGS_FILE);

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileWriter writer = new FileWriter(file)) {
                properties.setProperty("URL", "");
                properties.setProperty("USERNAME", "");
                properties.setProperty("PASSWORD", "");
                properties.store(writer, "Database settings");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        URL = properties.getProperty("URL");
        USERNAME = properties.getProperty("USERNAME");
        PASSWORD = properties.getProperty("PASSWORD");
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (URL.isEmpty() || USERNAME.isEmpty() || PASSWORD.isEmpty()) {
                throw new SQLException("Database settings are not configured properly.");
            }
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return connection;
    }

    public static void registerUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
        }
    }

    public static List<Note> getNotesForPeriod(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Note> notes = new ArrayList<>();
        String query = "SELECT date, text FROM notes WHERE user_id = ? AND date BETWEEN ? AND ? ORDER BY date";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(startDate));
            statement.setDate(3, Date.valueOf(endDate));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                LocalDate date = resultSet.getDate("date").toLocalDate();
                String text = resultSet.getString("text");
                notes.add(new Note(date, text));
            }
        }
        return notes;
    }

    public static int authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return -1;
    }

    public static boolean checkUserExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public static void addNoteForUser(int userId, LocalDate date, String text) throws SQLException {
        String query = "INSERT INTO notes (user_id, date, text) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(date));
            statement.setString(3, text);
            statement.executeUpdate();
        }
    }

    public static void deleteNoteForUser(int userId, LocalDate date, String text) throws SQLException {
        String sql = "DELETE FROM notes WHERE user_id = ? AND date = ? AND text = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setDate(2, Date.valueOf(date));
            statement.setString(3, text);
            statement.executeUpdate();
        }
    }

    public static Map<LocalDate, List<String>> getNotesForUser(int userId) throws SQLException {
        Map<LocalDate, List<String>> notesMap = new HashMap<>();
        String sql = "SELECT date, text FROM notes WHERE user_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    LocalDate date = resultSet.getDate("date").toLocalDate();
                    String note = resultSet.getString("text");
                    notesMap.computeIfAbsent(date, k -> new ArrayList<>()).add(note);
                }
            }
        }
        return notesMap;
    }
}
