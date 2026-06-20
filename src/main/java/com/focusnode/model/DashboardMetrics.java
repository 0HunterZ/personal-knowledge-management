package com.focusnode.model;

public class DashboardMetrics {
    private int totalFocusMinutesThisWeek;
    private int totalFocusMinutesLastWeek;
    
    private int focusSessionsThisWeek;
    private int focusSessionsLastWeek;
    
    private double taskCompletionRateThisWeek;
    private double taskCompletionRateLastWeek;
    
    private int currentStreak;
    private int streakLastWeek;
    
    private double focusScoreThisWeek;
    private double focusScoreLastWeek;
    
    // Maps for charts
    private java.util.Map<String, Integer> dailyFocusMinutesThisWeek = new java.util.HashMap<>();
    private java.util.Map<String, Integer> categoryFocusMinutesThisWeek = new java.util.HashMap<>();
    private java.util.Map<String, Integer> dailyFocusSessionsThisMonth = new java.util.HashMap<>(); // day of month -> count

    // Productivity insights
    private int avgSessionMinutes;
    private double morningFocusPercent; // % of focus before 12 PM
    private String bestDayOfWeek = "";
    private int totalSessionsAllTime;

    public DashboardMetrics() {}

    // Getters
    public int getTotalFocusMinutesThisWeek() { return totalFocusMinutesThisWeek; }
    public int getTotalFocusMinutesLastWeek() { return totalFocusMinutesLastWeek; }
    public int getFocusSessionsThisWeek() { return focusSessionsThisWeek; }
    public int getFocusSessionsLastWeek() { return focusSessionsLastWeek; }
    public double getTaskCompletionRateThisWeek() { return taskCompletionRateThisWeek; }
    public double getTaskCompletionRateLastWeek() { return taskCompletionRateLastWeek; }
    public int getCurrentStreak() { return currentStreak; }
    public int getStreakLastWeek() { return streakLastWeek; }
    public double getFocusScoreThisWeek() { return focusScoreThisWeek; }
    public double getFocusScoreLastWeek() { return focusScoreLastWeek; }
    
    public java.util.Map<String, Integer> getDailyFocusMinutesThisWeek() { return dailyFocusMinutesThisWeek; }
    public java.util.Map<String, Integer> getCategoryFocusMinutesThisWeek() { return categoryFocusMinutesThisWeek; }
    public java.util.Map<String, Integer> getDailyFocusSessionsThisMonth() { return dailyFocusSessionsThisMonth; }

    public int getAvgSessionMinutes() { return avgSessionMinutes; }
    public double getMorningFocusPercent() { return morningFocusPercent; }
    public String getBestDayOfWeek() { return bestDayOfWeek; }
    public int getTotalSessionsAllTime() { return totalSessionsAllTime; }

    // Setters
    public void setTotalFocusMinutesThisWeek(int v) { this.totalFocusMinutesThisWeek = v; }
    public void setTotalFocusMinutesLastWeek(int v) { this.totalFocusMinutesLastWeek = v; }
    public void setFocusSessionsThisWeek(int v) { this.focusSessionsThisWeek = v; }
    public void setFocusSessionsLastWeek(int v) { this.focusSessionsLastWeek = v; }
    public void setTaskCompletionRateThisWeek(double v) { this.taskCompletionRateThisWeek = v; }
    public void setTaskCompletionRateLastWeek(double v) { this.taskCompletionRateLastWeek = v; }
    public void setCurrentStreak(int v) { this.currentStreak = v; }
    public void setStreakLastWeek(int v) { this.streakLastWeek = v; }
    public void setFocusScoreThisWeek(double v) { this.focusScoreThisWeek = v; }
    public void setFocusScoreLastWeek(double v) { this.focusScoreLastWeek = v; }
    public void setAvgSessionMinutes(int v) { this.avgSessionMinutes = v; }
    public void setMorningFocusPercent(double v) { this.morningFocusPercent = v; }
    public void setBestDayOfWeek(String v) { this.bestDayOfWeek = v; }
    public void setTotalSessionsAllTime(int v) { this.totalSessionsAllTime = v; }
}
