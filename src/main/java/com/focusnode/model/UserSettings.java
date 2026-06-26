package com.focusnode.model;

public class UserSettings {
    private int userId;
    private String theme;
    private int dailyFocusGoalMinutes;
    private String language;
    private String geminiApiKey;
    private String defaultStoragePath;

    private Boolean googleDriveSyncEnabled;

    public UserSettings() {}

    public UserSettings(int userId, String theme, int dailyFocusGoalMinutes, String language, String geminiApiKey, String defaultStoragePath, Boolean googleDriveSyncEnabled) {
        this.userId = userId;
        this.theme = theme;
        this.dailyFocusGoalMinutes = dailyFocusGoalMinutes;
        this.language = language;
        this.geminiApiKey = geminiApiKey;
        this.defaultStoragePath = defaultStoragePath;
        this.googleDriveSyncEnabled = googleDriveSyncEnabled;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getDailyFocusGoalMinutes() { return dailyFocusGoalMinutes; }
    public void setDailyFocusGoalMinutes(int dailyFocusGoalMinutes) { this.dailyFocusGoalMinutes = dailyFocusGoalMinutes; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }

    public String getDefaultStoragePath() { return defaultStoragePath; }
    public void setDefaultStoragePath(String defaultStoragePath) { this.defaultStoragePath = defaultStoragePath; }

    public Boolean getGoogleDriveSyncEnabled() { return googleDriveSyncEnabled; }
    public void setGoogleDriveSyncEnabled(Boolean googleDriveSyncEnabled) { this.googleDriveSyncEnabled = googleDriveSyncEnabled; }
}
