package com.focusnode.service;

import com.focusnode.model.CategoryBreakdown;
import com.focusnode.model.FocusPoint;
import com.focusnode.model.Note;
import com.focusnode.model.TagUsageItem;
import com.focusnode.model.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAppDataService implements AppDataService {

    private final List<Task> tasks;
    private final List<Note> notes;
    private final List<TagUsageItem> tagUsage;
    private final List<FocusPoint> weeklyFocus;
    private final List<CategoryBreakdown> categoryBreakdown;
    private final List<com.focusnode.model.FocusSession> focusSessions;
    private final List<com.focusnode.model.ReviewItem> reviewItems;

    public InMemoryAppDataService() {
        this.tasks = new ArrayList<>(List.of(
                new Task(1, 1, "Finish reading Markdown guide", "", Task.Status.COMPLETED, Task.Priority.HIGH, LocalDate.now().plusDays(0).atStartOfDay(), 60, 60, List.of("Study"), List.of(), java.time.LocalDateTime.now(), false),
                new Task(2, 1, "Build LAN connection module", "", Task.Status.COMPLETED, Task.Priority.HIGH, LocalDate.now().minusDays(1).atStartOfDay(), 90, 90, List.of("Project"), List.of(), java.time.LocalDateTime.now(), false),
                new Task(3, 1, "Review knowledge base UI", "", Task.Status.IN_PROGRESS, Task.Priority.MEDIUM, LocalDate.now().plusDays(0).atStartOfDay(), 45, 20, List.of("Review"), List.of(), java.time.LocalDateTime.now(), false),
                new Task(4, 1, "Study Java Socket programming", "", Task.Status.IN_PROGRESS, Task.Priority.HIGH, LocalDate.now().plusDays(1).atStartOfDay(), 120, 0, List.of("Study"), List.of(), java.time.LocalDateTime.now(), false),
                new Task(5, 1, "Prepare project presentation", "", Task.Status.PENDING, Task.Priority.MEDIUM, LocalDate.now().plusDays(2).atStartOfDay(), 60, 0, List.of("Work"), List.of(), java.time.LocalDateTime.now(), false),
                new Task(6, 1, "Exercise 30 minutes", "", Task.Status.PENDING, Task.Priority.LOW, LocalDate.now().plusDays(2).atStartOfDay(), 30, 0, List.of("Health"), List.of(), java.time.LocalDateTime.now(), false),
                new Task(7, 1, "Read AI research papers", "", Task.Status.PENDING, Task.Priority.MEDIUM, LocalDate.now().plusDays(3).atStartOfDay(), 50, 0, List.of("Study"), List.of(), java.time.LocalDateTime.now(), false),
                new Task(8, 1, "Write daily journal", "", Task.Status.PENDING, Task.Priority.LOW, LocalDate.now().plusDays(3).atStartOfDay(), 20, 0, List.of("Personal"), List.of(), java.time.LocalDateTime.now(), false)
        ));

        this.notes = new ArrayList<Note>(List.of(
                new Note(1, 1, 1, null, "Java Socket Programming", "Notes about TCP/IP, ServerSocket, Socket, and data communication...", "Tech", List.of("#java", "#network"), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), false),
                new Note(2, 1, 1, null, "TCP vs UDP", "Comparison of TCP and UDP protocols with use cases.", "Tech", List.of("#network", "#study"), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), false),
                new Note(3, 1, 2, null, "Focus System Mindmap", "Visual map of my productivity and focus system.", "Personal", List.of("#study", "#productivity"), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), false),
                new Note(4, 1, 1, null, "Java NIO Basics", "Understanding Non-blocking I/O in Java with examples.", "Tech", List.of("#java", "#advanced"), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), false),
                new Note(5, 1, 3, null, "LAN Focus Hub Roadmap", "Roadmap and milestones for LAN Focus Hub module.", "Project", List.of("#project", "#network"), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), false),
                new Note(6, 1, 2, null, "Pomodoro Technique Guide", "Detailed guide to using Pomodoro for deep work.", "Personal", List.of("#study", "#focus"), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), false)
        ));

        this.tagUsage = List.of(
                new TagUsageItem("Study", 28, "#10B981"),
                new TagUsageItem("Project", 24, "#3B82F6"),
                new TagUsageItem("Java", 20, "#F59E0B"),
                new TagUsageItem("Network", 16, "#8B5CF6"),
                new TagUsageItem("Others", 12, "#CBD5E1")
        );

        this.weeklyFocus = List.of(
                new FocusPoint("Mon", 200),
                new FocusPoint("Tue", 255),
                new FocusPoint("Wed", 330),
                new FocusPoint("Thu", 365),
                new FocusPoint("Fri", 290),
                new FocusPoint("Sat", 170),
                new FocusPoint("Sun", 175)
        );

        this.categoryBreakdown = List.of(
                new CategoryBreakdown("Study", 45, "#10B981"),
                new CategoryBreakdown("Project", 30, "#60A5FA"),
                new CategoryBreakdown("Reading", 15, "#A78BFA"),
                new CategoryBreakdown("Other", 10, "#FBBF24")
        );

        this.focusSessions = new ArrayList<>();
        this.reviewItems = new ArrayList<>();
    }

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
        return tasks;
    }

    @Override
    public void saveTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    @Override
    public void updateTask(Task task) {
        int index = tasks.indexOf(task);
        if (index != -1) {
            tasks.set(index, task);
        }
    }

    @Override
    public void deleteTask(Task task) {
        tasks.remove(task);
    }

    @Override
    public List<Note> getNotes() {
        return notes;
    }

    @Override
    public void saveNote(Note note) {
        if (!notes.contains(note)) {
            notes.add(note);
        }
    }

    @Override
    public void updateNote(Note note) {
        int index = notes.indexOf(note);
        if (index != -1) {
            notes.set(index, note);
        }
    }

    @Override
    public void deleteNote(Note note) {
        notes.remove(note);
    }

    @Override
    public List<TagUsageItem> getTagUsage() {
        return tagUsage;
    }

    @Override
    public List<FocusPoint> getWeeklyFocus() {
        return weeklyFocus;
    }

    @Override
    public List<CategoryBreakdown> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    @Override
    public List<com.focusnode.model.FocusSession> getFocusSessions() {
        return focusSessions;
    }

    @Override
    public void saveFocusSession(com.focusnode.model.FocusSession session) {
        if (!focusSessions.contains(session)) {
            focusSessions.add(session);
        }
    }

    @Override
    public List<com.focusnode.model.ReviewItem> getReviewItems() {
        return reviewItems;
    }

    @Override
    public void saveReviewItem(com.focusnode.model.ReviewItem item) {
        if (!reviewItems.contains(item)) {
            reviewItems.add(item);
        }
    }

    @Override
    public com.focusnode.model.DashboardMetrics getDashboardMetrics() {
        return new com.focusnode.model.DashboardMetrics();
    }
}
