package com.focusnode.service;

import com.focusnode.model.CategoryBreakdown;
import com.focusnode.model.FocusPoint;
import com.focusnode.model.Note;
import com.focusnode.model.TagUsageItem;
import com.focusnode.model.Task;

import java.util.List;

public interface AppDataService {
    String getUserName();
    String getGreetingMessage();
    List<Task> getTasks();
    void saveTask(Task task);
    void updateTask(Task task);
    void deleteTask(Task task);
    List<Note> getNotes();
    void saveNote(Note note);
    void updateNote(Note note);
    void deleteNote(Note note);
    List<TagUsageItem> getTagUsage();
    List<FocusPoint> getWeeklyFocus();
    List<CategoryBreakdown> getCategoryBreakdown();
    List<com.focusnode.model.FocusSession> getFocusSessions();
    void saveFocusSession(com.focusnode.model.FocusSession session);
    List<com.focusnode.model.ReviewItem> getReviewItems();
    void saveReviewItem(com.focusnode.model.ReviewItem item);
    com.focusnode.model.DashboardMetrics getDashboardMetrics();
}
