package com.focusnode.service;

import com.focusnode.model.CategoryBreakdown;
import com.focusnode.model.FocusPoint;
import com.focusnode.model.Note;
import com.focusnode.model.TagUsageItem;
import com.focusnode.model.Task;
import com.focusnode.model.FocusSession;
import com.focusnode.model.ReviewItem;
import com.focusnode.repository.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Collections;
import com.focusnode.util.EventBus;

public class SqlAppDataService implements AppDataService {

    private final TaskRepository taskRepo = new TaskRepository();
    private final NoteRepository noteRepo = new NoteRepository();
    private final FocusSessionRepository focusSessionRepo = new FocusSessionRepository();

    private final MetricsRepository metricsRepo = new MetricsRepository();
    private final ReviewItemRepository reviewRepo = new ReviewItemRepository();

    @Override
    public String getUserName() {
        return "User"; // In a real app, query dbo.Users
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
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
    }

    @Override
    public void updateTask(Task task) {
        taskRepo.update(task);
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
    }

    @Override
    public void deleteTask(Task task) {
        taskRepo.delete(task.getId());
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
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
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
    }

    @Override
    public void updateNote(Note note) {
        noteRepo.update(note);
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
    }

    @Override
    public void deleteNote(Note note) {
        noteRepo.delete(note.getId());
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
    }

    @Override
    public List<TagUsageItem> getTagUsage() {
        return metricsRepo.getTagUsage(1); // Default UserId = 1
    }

    @Override
    public List<FocusPoint> getWeeklyFocus() {
        return metricsRepo.getWeeklyFocus(1); // Default UserId = 1
    }

    @Override
    public List<CategoryBreakdown> getCategoryBreakdown() {
        return metricsRepo.getCategoryBreakdown(1); // Default UserId = 1
    }

    @Override
    public List<FocusSession> getFocusSessions() {
        return focusSessionRepo.findAll();
    }

    @Override
    public void saveFocusSession(FocusSession session) {
        focusSessionRepo.add(session);
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
    }

    @Override
    public List<ReviewItem> getReviewItems() {
        return reviewRepo.findAll();
    }

    @Override
    public void saveReviewItem(ReviewItem item) {
        if (item.getId() <= 0) {
            reviewRepo.add(item);
        } else {
            reviewRepo.update(item);
        }
        EventBus.publish(EventBus.EventType.DATA_CHANGED);
    }

    @Override
    public com.focusnode.model.DashboardMetrics getDashboardMetrics() {
        return metricsRepo.getDashboardMetrics(1); // Default UserId = 1
    }
}
