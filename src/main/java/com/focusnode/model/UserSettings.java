package com.focusnode.model;

public class UserSettings {
    private int userId;
    private String theme;
    private int dailyFocusGoalMinutes;
    private String language;

    public UserSettings() {}

    public UserSettings(int userId, String theme, int dailyFocusGoalMinutes, String language) {
        this.userId = userId;
        this.theme = theme;
        this.dailyFocusGoalMinutes = dailyFocusGoalMinutes;
        this.language = language;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public int getDailyFocusGoalMinutes() { return dailyFocusGoalMinutes; }
    public void setDailyFocusGoalMinutes(int dailyFocusGoalMinutes) { this.dailyFocusGoalMinutes = dailyFocusGoalMinutes; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
