package com.focusnode.model;

public class PomodoroPreset {
    private int id;
    private Integer userId; // Nullable for system defaults
    private String name;
    private int focusTimeMinutes;
    private int shortBreakMinutes;
    private int longBreakMinutes;
    private int longBreakInterval;

    public PomodoroPreset() {}

    public PomodoroPreset(int id, Integer userId, String name, int focusTimeMinutes, int shortBreakMinutes, int longBreakMinutes, int longBreakInterval) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.focusTimeMinutes = focusTimeMinutes;
        this.shortBreakMinutes = shortBreakMinutes;
        this.longBreakMinutes = longBreakMinutes;
        this.longBreakInterval = longBreakInterval;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getFocusTimeMinutes() { return focusTimeMinutes; }
    public void setFocusTimeMinutes(int focusTimeMinutes) { this.focusTimeMinutes = focusTimeMinutes; }

    public int getShortBreakMinutes() { return shortBreakMinutes; }
    public void setShortBreakMinutes(int shortBreakMinutes) { this.shortBreakMinutes = shortBreakMinutes; }

    public int getLongBreakMinutes() { return longBreakMinutes; }
    public void setLongBreakMinutes(int longBreakMinutes) { this.longBreakMinutes = longBreakMinutes; }

    public int getLongBreakInterval() { return longBreakInterval; }
    public void setLongBreakInterval(int longBreakInterval) { this.longBreakInterval = longBreakInterval; }
}
