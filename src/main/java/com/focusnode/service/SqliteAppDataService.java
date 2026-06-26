package com.focusnode.service;

import com.focusnode.model.CategoryBreakdown;
import com.focusnode.model.FocusPoint;
import com.focusnode.model.Note;
import com.focusnode.model.TagUsageItem;
import com.focusnode.model.Task;
import com.focusnode.repository.*;

import java.time.LocalTime;
import java.util.List;

public class SqliteAppDataService implements AppDataService {

    private TaskRepository taskRepo = new TaskRepository();
    private NoteRepository noteRepo = new NoteRepository();
    private TagUsageRepository tagRepo = new TagUsageRepository();
    private FocusPointRepository focusRepo = new FocusPointRepository();
    private CategoryBreakdownRepository categoryRepo = new CategoryBreakdownRepository();
    private FocusSessionRepository focusSessionRepo = new FocusSessionRepository();
    private ReviewItemRepository reviewItemRepo = new ReviewItemRepository();

    @Override
    public String getUserName() {
        return "User";
    }

    @Override
    public String getGreetingMessage() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            return "Good morning, " + getUserName() + "! 👋";
        } else if (hour < 18) {
            return "Good afternoon, " + getUserName() + "! 👋";
        } else {
            return "Good evening, " + getUserName() + "! 👋";
        }
    }

    @Override
    public List<Task> getTasks() {
        return taskRepo.findAll();
    }

    @Override
    public void saveTask(Task task) {
        if (task.getId() <= 0) {
            taskRepo.add(task);
        } else {
            taskRepo.update(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        taskRepo.update(task);
    }

    @Override
    public void deleteTask(Task task) {
        taskRepo.delete(task.getId());
    }

    @Override
    public List<Note> getNotes() {
        return noteRepo.findAll();
    }

    @Override
    public void saveNote(Note note) {
        if (note.getId() <= 0) {
            noteRepo.add(note);
        } else {
            noteRepo.update(note);
        }
    }

    @Override
    public void updateNote(Note note) {
        noteRepo.update(note);
    }

    @Override
    public void deleteNote(Note note) {
        noteRepo.delete(note.getId());
    }

    @Override
    public List<TagUsageItem> getTagUsage() {
        return tagRepo.findAll();
    }

    @Override
    public List<FocusPoint> getWeeklyFocus() {
        return focusRepo.findAll();
    }

    @Override
    public List<CategoryBreakdown> getCategoryBreakdown() {
        return categoryRepo.findAll();
    }

    @Override
    public List<com.focusnode.model.FocusSession> getFocusSessions() {
        return focusSessionRepo.findAll();
    }

    @Override
    public void saveFocusSession(com.focusnode.model.FocusSession session) {
        focusSessionRepo.add(session);
    }

    @Override
    public List<com.focusnode.model.ReviewItem> getReviewItems() {
        return reviewItemRepo.findAll();
    }

    @Override
    public void saveReviewItem(com.focusnode.model.ReviewItem item) {
        if (item.getId() <= 0) {
            // we don't have add method for ReviewItemRepo yet, but for now we only need update
            reviewItemRepo.update(item);
        } else {
            reviewItemRepo.update(item);
        }
    }

    @Override
    public com.focusnode.model.DashboardMetrics getDashboardMetrics() {
        com.focusnode.model.DashboardMetrics metrics = new com.focusnode.model.DashboardMetrics();
        
        List<com.focusnode.model.FocusSession> sessions = focusSessionRepo.findAll();
        List<Task> tasks = taskRepo.findAll();
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate startOfThisWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        java.time.LocalDate startOfLastWeek = startOfThisWeek.minusDays(7);
        java.time.LocalDate endOfLastWeek = startOfThisWeek.minusDays(1);
        
        int focusMinutesThisWeek = 0;
        int focusMinutesLastWeek = 0;
        int focusSessionsThisWeek = 0;
        int focusSessionsLastWeek = 0;
        
        int morningSessions = 0;
        int totalSessionsAllTime = sessions.size();
        
        java.util.Map<String, Integer> dailyMins = new java.util.HashMap<>();
        java.util.Map<String, Integer> dailySessionsThisMonth = new java.util.HashMap<>();
        java.util.Map<String, Integer> categoryMins = new java.util.HashMap<>();
        java.util.Map<String, Integer> sessionsPerDayOfWeek = new java.util.HashMap<>();
        
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String d : days) dailyMins.put(d, 0);
        
        int totalSessionMins = 0;
        
        for (com.focusnode.model.FocusSession s : sessions) {
            if (s.getActualMinutes() <= 0) continue; // Only count sessions with actual time
            
            java.time.LocalDateTime start = s.getStartedAt();
            if (start == null) continue;
            java.time.LocalDate date = start.toLocalDate();
            
            totalSessionMins += s.getActualMinutes();
            
            if (start.getHour() < 12) {
                morningSessions++;
            }
            
            String dayOfWeek = start.getDayOfWeek().name();
            dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
            sessionsPerDayOfWeek.put(dayOfWeek, sessionsPerDayOfWeek.getOrDefault(dayOfWeek, 0) + 1);
            
            if (date.getYear() == today.getYear() && date.getMonth() == today.getMonth()) {
                String dStr = String.valueOf(date.getDayOfMonth());
                dailySessionsThisMonth.put(dStr, dailySessionsThisMonth.getOrDefault(dStr, 0) + 1);
            }
            
            if (!date.isBefore(startOfThisWeek)) {
                // This week
                focusMinutesThisWeek += s.getActualMinutes();
                focusSessionsThisWeek++;
                
                dailyMins.put(dayOfWeek, dailyMins.getOrDefault(dayOfWeek, 0) + s.getActualMinutes());
                
                // Try to find category from task
                String cat = "Uncategorized";
                if (s.getTaskId() > 0) {
                    for (Task t : tasks) {
                        if (t.getId() == s.getTaskId()) {
                            cat = t.getCategory();
                            break;
                        }
                    }
                }
                categoryMins.put(cat, categoryMins.getOrDefault(cat, 0) + s.getActualMinutes());
            } else if (!date.isBefore(startOfLastWeek) && !date.isAfter(endOfLastWeek)) {
                // Last week
                focusMinutesLastWeek += s.getActualMinutes();
                focusSessionsLastWeek++;
            }
        }
        
        // Compute task completion rate
        int tasksDueThisWeek = 0;
        int tasksCompletedThisWeek = 0;
        int tasksDueLastWeek = 0;
        int tasksCompletedLastWeek = 0;
        
        for (Task t : tasks) {
            java.time.LocalDate dateToUse = null;
            if (t.getDueDate() != null) {
                dateToUse = t.getDueDate().toLocalDate();
            } else if (t.getCreatedAt() != null) {
                dateToUse = t.getCreatedAt().toLocalDate();
            }
            if (dateToUse == null) continue;
            
            if (!dateToUse.isBefore(startOfThisWeek)) {
                tasksDueThisWeek++;
                if (t.isCompleted()) tasksCompletedThisWeek++;
            } else if (!dateToUse.isBefore(startOfLastWeek) && !dateToUse.isAfter(endOfLastWeek)) {
                tasksDueLastWeek++;
                if (t.isCompleted()) tasksCompletedLastWeek++;
            }
        }
        
        double taskCompRateThisWeek = tasksDueThisWeek > 0 ? (double) tasksCompletedThisWeek / tasksDueThisWeek : 0.0;
        double taskCompRateLastWeek = tasksDueLastWeek > 0 ? (double) tasksCompletedLastWeek / tasksDueLastWeek : 0.0;
        
        // Focus score
        double scoreThisWeek = focusSessionsThisWeek > 0 ? ((double) focusMinutesThisWeek / focusSessionsThisWeek) / 25.0 * 10.0 : 0.0;
        if (scoreThisWeek > 10.0) scoreThisWeek = 10.0;
        double scoreLastWeek = focusSessionsLastWeek > 0 ? ((double) focusMinutesLastWeek / focusSessionsLastWeek) / 25.0 * 10.0 : 0.0;
        if (scoreLastWeek > 10.0) scoreLastWeek = 10.0;
        
        // Streak
        int currentStreak = 0;
        java.time.LocalDate checkDate = today;
        java.util.Set<java.time.LocalDate> focusDays = new java.util.HashSet<>();
        for (com.focusnode.model.FocusSession s : sessions) {
            if (s.getActualMinutes() > 0 && s.getStartedAt() != null) {
                focusDays.add(s.getStartedAt().toLocalDate());
            }
        }
        
        if (!focusDays.contains(today) && focusDays.contains(today.minusDays(1))) {
            checkDate = today.minusDays(1); // Allow streak to continue if they haven't focused yet today
        }
        
        while (focusDays.contains(checkDate)) {
            currentStreak++;
            checkDate = checkDate.minusDays(1);
        }
        
        int streakLastWeek = 0; // Simplified
        
        String bestDay = "";
        int maxSessions = -1;
        for (java.util.Map.Entry<String, Integer> e : sessionsPerDayOfWeek.entrySet()) {
            if (e.getValue() > maxSessions) {
                maxSessions = e.getValue();
                bestDay = e.getKey();
            }
        }
        
        metrics.setTotalFocusMinutesThisWeek(focusMinutesThisWeek);
        metrics.setTotalFocusMinutesLastWeek(focusMinutesLastWeek);
        metrics.setFocusSessionsThisWeek(focusSessionsThisWeek);
        metrics.setFocusSessionsLastWeek(focusSessionsLastWeek);
        metrics.setTaskCompletionRateThisWeek(taskCompRateThisWeek);
        metrics.setTaskCompletionRateLastWeek(taskCompRateLastWeek);
        metrics.setCurrentStreak(currentStreak);
        metrics.setStreakLastWeek(streakLastWeek);
        metrics.setFocusScoreThisWeek(scoreThisWeek);
        metrics.setFocusScoreLastWeek(scoreLastWeek);
        
        metrics.getDailyFocusMinutesThisWeek().putAll(dailyMins);
        metrics.getCategoryFocusMinutesThisWeek().putAll(categoryMins);
        metrics.getDailyFocusSessionsThisMonth().putAll(dailySessionsThisMonth);
        
        metrics.setTotalSessionsAllTime(totalSessionsAllTime);
        metrics.setAvgSessionMinutes(totalSessionsAllTime > 0 ? totalSessionMins / totalSessionsAllTime : 0);
        metrics.setMorningFocusPercent(totalSessionsAllTime > 0 ? ((double) morningSessions / totalSessionsAllTime) * 100 : 0);
        metrics.setBestDayOfWeek(bestDay);
        
        return metrics;
    }
}
