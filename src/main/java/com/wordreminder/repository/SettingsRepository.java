package com.wordreminder.repository;

import com.wordreminder.data.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class SettingsRepository {
    private final Database database;

    public SettingsRepository(Database database) {
        this.database = database;
    }

    public int getDailyGoal() throws SQLException {
        int goal = getInt("daily_goal", 10);
        return Math.max(1, Math.min(100, goal));
    }

    public void setDailyGoal(int goal) throws SQLException {
        if (goal < 1 || goal > 100) throw new IllegalArgumentException("Günlük hedef 1–100 arasında olmalı.");
        set("daily_goal", Integer.toString(goal));
    }

    public int getReminderMinutes() throws SQLException {
        return Math.max(1, Math.min(180, getInt("reminder_minutes", 10)));
    }

    public void setReminderMinutes(int minutes) throws SQLException {
        if (minutes < 1 || minutes > 180) {
            throw new IllegalArgumentException("Hatırlatma aralığı 1–180 dakika arasında olmalı.");
        }
        set("reminder_minutes", Integer.toString(minutes));
    }

    public boolean isStartWithWindows() throws SQLException {
        return Boolean.parseBoolean(get("start_with_windows", "false"));
    }

    public void setStartWithWindows(boolean enabled) throws SQLException {
        set("start_with_windows", Boolean.toString(enabled));
    }

    public boolean isStartMinimized() throws SQLException {
        return Boolean.parseBoolean(get("start_minimized", "true"));
    }

    public void setStartMinimized(boolean enabled) throws SQLException {
        set("start_minimized", Boolean.toString(enabled));
    }

    public String getReminderLevel() throws SQLException {
        return get("reminder_level", "ALL");
    }

    public void setReminderLevel(String level) throws SQLException {
        set("reminder_level", normalizeFilter(level));
    }

    public String getReminderCategory() throws SQLException {
        return get("reminder_category", "ALL");
    }

    public void setReminderCategory(String category) throws SQLException {
        set("reminder_category", normalizeFilter(category));
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? "ALL" : value.trim().toUpperCase();
    }

    private int getInt(String key, int fallback) throws SQLException {
        try {
            return Integer.parseInt(get(key, Integer.toString(fallback)));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String get(String key, String fallback) throws SQLException {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString(1) : fallback;
            }
        }
    }

    private void set(String key, String value) throws SQLException {
        String sql = """
                INSERT INTO app_settings(setting_key, setting_value) VALUES(?, ?)
                ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        }
    }
}
